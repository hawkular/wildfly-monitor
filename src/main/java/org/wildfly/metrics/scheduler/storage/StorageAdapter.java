package org.wildfly.metrics.scheduler.storage;

import org.wildfly.metrics.scheduler.config.Configuration;
import org.wildfly.metrics.scheduler.diagnose.Diagnostics;

import java.util.Set;

/**
 * @author Heiko Braun
 * @since 10/10/14
 */
public interface StorageAdapter {
    void store(Set<Sample> samples);
    void setConfiguration(Configuration config);
    void setDiagnostics(Diagnostics diag);
}
