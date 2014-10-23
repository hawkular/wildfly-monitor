package org.rhq.wfly.monitor.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import org.rhq.wfly.monitor.service.RhqMetricsService;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * StepHandler for removing a metric
 * @author Heiko W. Rupp
 */
public class InputRemove extends AbstractRemoveStepHandler {

    public static final InputRemove INSTANCE = new InputRemove();

    private InputRemove() {

    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation,
                                  ModelNode model) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));

        String metricName = address.getLastElement().getValue();
        RhqMetricsService service = (RhqMetricsService) context.getServiceRegistry(true).getService(RhqMetricsService.SERVICE_NAME).getService();
        service.removeMetric(metricName);

    }
}
