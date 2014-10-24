package org.wildfly.metrics.scheduler.storage;

import org.wildfly.metrics.scheduler.polling.Task;

/**
 * @author Heiko Braun
 * @since 24/10/14
 */
public interface KeyResolution {
    String resolve(Task task);
}
