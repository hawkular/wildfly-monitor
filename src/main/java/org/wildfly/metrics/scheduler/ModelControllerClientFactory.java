package org.wildfly.metrics.scheduler;

import org.jboss.as.controller.client.ModelControllerClient;

/**
 * @author Heiko Braun
 * @since 17/10/14
 */
public interface ModelControllerClientFactory {
    ModelControllerClient createClient();
}
