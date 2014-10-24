package org.rhq.wfly.monitor.service;

import org.jboss.as.controller.ControlledProcessState;
import org.jboss.as.controller.ControlledProcessStateService;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.threads.JBossThreadFactory;
import org.wildfly.metrics.scheduler.ModelControllerClientFactory;
import org.wildfly.metrics.scheduler.config.Configuration;
import org.wildfly.metrics.scheduler.config.ConfigurationInstance;
import org.wildfly.metrics.scheduler.config.Interval;
import org.wildfly.metrics.scheduler.config.ResourceRef;
import org.wildfly.security.manager.action.GetAccessControlContextAction;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.security.AccessController.doPrivileged;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A service that gathers values from the system and sends them to a RHQ Metrics Server
 * @author Heiko W. Rupp
 */
public class RhqMetricsService implements Service<RhqMetricsService> {

    private static final String LAUNCH_TYPE = "launch-type";
    private static final String DOMAIN = "domain";
    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final String RESULT = "result";

    private Interval diagnosticsInterval;
    private boolean diagnosticsEnabled = false;
    private boolean enabled = false;

    private ConfigurationInstance schedulerConfig;
    private org.wildfly.metrics.scheduler.Service schedulerService;

    private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<>();
    private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<>();
    private final InjectedValue<ControlledProcessStateService> processStateValue = new InjectedValue<>();

    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("rhq", "wildfly-monitor");
    private PropertyChangeListener serverStateListener;

    public RhqMetricsService(ModelNode config) {

        // the actual scheduler config
        this.schedulerConfig = new ConfigurationInstance();

        Property storageAdapter = config.get("storage-adapter").asPropertyList().get(0);
        List<Property> monitors = config.get("server-monitor").asPropertyList();
        Property diagnostics = config.get("diagnostics").asPropertyList().get(0);

        if(monitors.size()==1)
        {
            Property monitor = monitors.get(0);
            ModelNode monitorCfg = monitor.getValue();
            if(monitorCfg.get("enabled").asBoolean())
            {
                this.enabled = true;

                // parse storage adapter
                ModelNode storageAdapterCfg = storageAdapter.getValue();
                schedulerConfig.setStorageAdapter(Configuration.Storage.valueOf(storageAdapter.getName().toUpperCase()));
                schedulerConfig.setStorageUrl(storageAdapterCfg.get("url").asString());
                schedulerConfig.setStorageDb(storageAdapterCfg.get("db").asString());

                if(storageAdapterCfg.hasDefined("user"))
                    schedulerConfig.setStorageUser(storageAdapterCfg.get("user").asString());

                if(storageAdapterCfg.hasDefined("password"))
                    schedulerConfig.setStoragePassword(storageAdapterCfg.get("password").asString());

                if(storageAdapterCfg.hasDefined("token"))
                    schedulerConfig.setStorageToken(storageAdapterCfg.get("token").asString());

                // monitoring setup
                schedulerConfig.setSchedulerThreads(monitorCfg.get("num-threads").asInt());

                List<Property> metrics = monitorCfg.get("data-input").asPropertyList();
                for (Property metric : metrics) {
                    ModelNode metricCfg = metric.getValue();

                    Interval interval = null;
                    if (metricCfg.hasDefined("seconds")) {
                        interval = new Interval(metricCfg.get("seconds").asInt(), TimeUnit.SECONDS);
                    }
                    else if (metricCfg.hasDefined("minutes"))
                    {
                        interval = new Interval(metricCfg.get("minutes").asInt(), TimeUnit.MINUTES);
                    }
                    else if(metricCfg.hasDefined("hours"))
                    {
                        interval = new Interval(metricCfg.get("hours").asInt(), TimeUnit.HOURS);
                    }


                    ResourceRef ref = new ResourceRef(
                            metricCfg.get("resource").asString(),
                            metricCfg.get("attribute").asString(),
                            interval
                    );

                    schedulerConfig.addResourceRef(ref);

                }

                // diagnostics
                ModelNode diagnosticsCfg = diagnostics.getValue();
                schedulerConfig.setDiagnostics(Configuration.Diagnostics.valueOf(diagnostics.getName().toUpperCase()));

                if(diagnosticsCfg.hasDefined("seconds"))
                {
                    this.diagnosticsEnabled = diagnosticsCfg.get("enabled").asBoolean();
                    this.diagnosticsInterval = new Interval(diagnosticsCfg.get("seconds").asInt(), TimeUnit.SECONDS);
                }
                else if(diagnosticsCfg.hasDefined("minutes"))
                {
                    this.diagnosticsEnabled = diagnosticsCfg.get("enabled").asBoolean();
                    this.diagnosticsInterval = new Interval(diagnosticsCfg.get("minutes").asInt(), TimeUnit.MINUTES);
                }

            }
        }

    }

    public static ServiceController<RhqMetricsService> createService(final ServiceTarget target,
                                                                     final ServiceVerificationHandler verificationHandler,
                                                                     ModelNode config) {

        RhqMetricsService service = new RhqMetricsService(config);

        return target.addService(SERVICE_NAME, service)
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class,
                        service.serverEnvironmentValue)
                .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class,
                        service.modelControllerValue)
                .addDependency(ControlledProcessStateService.SERVICE_NAME, ControlledProcessStateService.class,
                        service.processStateValue)
                .addListener(verificationHandler)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }


    @Override
    public void start(final StartContext startContext) throws StartException {


        if (this.enabled) {


            // deferred startup: we have to wait until the server is running before we can monitor the subsystems (parallel service startup)
            ControlledProcessStateService stateService = processStateValue.getValue();
            serverStateListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ControlledProcessState.State.RUNNING.equals(evt.getNewValue())) {
                        System.out.println("<< Start monitoring subsystems >>");
                        startScheduler(startContext);
                    }
                }
            };
            stateService.addPropertyChangeListener(serverStateListener);

        }

    }

    private void startScheduler(StartContext startContext) {

        final ThreadFactory threadFactory = new JBossThreadFactory(
                new ThreadGroup("RHQ-Metrics-threads"),
                Boolean.FALSE, null, "%G - %t", null, null,
                doPrivileged(GetAccessControlContextAction.getInstance()));

        final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);
        final ModelControllerClient client = modelControllerValue.getValue().createClient(executorService);


        // create scheduler service
        schedulerService = new org.wildfly.metrics.scheduler.Service(schedulerConfig, new ModelControllerClientFactory() {
            @Override
            public ModelControllerClient createClient() {
                return  modelControllerValue.getValue().createClient(executorService);
            }
        });

        // Get the server name from the runtime model
        // TODO: this should be changed to OperationContext.readResourceFromRoot(...)
        boolean isDomainMode = getAttribute(client, LAUNCH_TYPE).equalsIgnoreCase(DOMAIN);

        String hostName = null;
        String serverName = null;

        if(isDomainMode)
        {
            hostName = getAttribute(client, HOST);
            serverName = getAttribute(client, NAME);
        }
        else
        {
            hostName = "";
            serverName = getAttribute(client, NAME);
        }

        // Create a http client
        schedulerService.start(hostName, serverName);

        // enabled diagnostics if needed
        if(diagnosticsEnabled) {
            schedulerService.reportEvery(diagnosticsInterval.getVal(), diagnosticsInterval.getUnit());
        }

    }

    @Override
    public void stop(StopContext stopContext) {

        // shutdown scheduler
        if(schedulerService!=null)
            schedulerService.stop();

        // cleanup the state listener
        if(serverStateListener!=null)
            processStateValue.getValue().removePropertyChangeListener(serverStateListener);

    }

    @Override
    public RhqMetricsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    private String getStringAttribute(ModelControllerClient client, String attributeName, PathAddress path)  {
        ModelNode result = getModelNode(client, attributeName, path);
        return result.asString();
    }

    private ModelNode getModelNode(ModelControllerClient client, String attributeName, PathAddress address){
        try {
            ModelNode request = new ModelNode();

            if (address !=null) {

                request.get(ADDRESS).set(address.toModelNode());
            }

            request.get(OP).set(READ_ATTRIBUTE_OPERATION);
            request.get(NAME).set(attributeName);
            ModelNode resultNode = client.execute(request);
            // get the inner "result" -- should check for failure and so on...
            resultNode = resultNode.get(RESULT);
            return resultNode;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAttribute(ModelControllerClient client, String attributeName) {
        try {
            ModelNode request = new ModelNode();
            request.get(ADDRESS).setEmptyList();
            request.get(OP).set(READ_ATTRIBUTE_OPERATION);
            request.get(NAME).set(attributeName);
            ModelNode resultNode = client.execute(request);
            // get the inner "result" -- should check for failure and so on...
            resultNode = resultNode.get(RESULT);
            return resultNode.asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
