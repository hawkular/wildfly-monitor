package org.wildfly.metrics.scheduler.config;

import java.util.List;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public interface Configuration {

    public enum Diagnostics {STORAGE, CONSOLE};
    public enum Storage {RHQ, INFLUX}

    /**
     * The host controller host.
     * @return
     */
    String getHost();

    /**
     * The host controller port.
     *
     * @return
     */
    int getPort();

    /**
     * Host controller user
     * @return
     */
    String getUsername();

    /**
     * Host controller password
     * @return
     */
    String getPassword();

    /**
     * Number of threads the scheduler uses to poll for new data.
     *
     * @return
     */
    int getSchedulerThreads();

    /**
     * The resources that are to be monitored.
     * {@link org.wildfly.metrics.scheduler.config.ResourceRef}'s use relative addresses.
     * The core {@link org.wildfly.metrics.scheduler.Service} will resolve it against absolute address within a Wildfly domain.
     *
     * @return
     */
    List<ResourceRef> getResourceRefs();

    Storage getStorageAdapter();

    String getStorageUrl();

    String getStorageUser();

    String getStoragePassword();

    String getStorageDBName();

    String getStorageToken();

    Diagnostics getDiagnostics();

}
