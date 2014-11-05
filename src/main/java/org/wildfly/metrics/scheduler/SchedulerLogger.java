package org.wildfly.metrics.scheduler;

/**
 * @author Heiko Braun
 * @since 05/11/14
 */

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.MessageLogger;

/**
 * Log messages for WildFly cassandra module
 * @author Heiko Braun
 */
@MessageLogger(projectCode = "<<none>>")
public interface SchedulerLogger extends BasicLogger {
    /**
     * A logger with the category {@code org.wildfly.metrics.scheduler}.
     */
    SchedulerLogger LOGGER = Logger.getMessageLogger(SchedulerLogger.class, "org.wildfly.metrics.scheduler");

}
