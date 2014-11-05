package org.rhq.wfly.monitor.extension;

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
public interface MonitorLogger extends BasicLogger {
    /**
     * A logger with the category {@code org.rhq.wfly.monitor}.
     */
    MonitorLogger LOGGER = Logger.getMessageLogger(MonitorLogger.class, "org.rhq.wfly.monitor");

}
