package org.wildfly.metrics.scheduler.diagnose;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public interface Diagnostics {
    Timer getRequestTimer();
    Meter getErrorRate();
    Meter getDelayedRate();

    Meter getStorageErrorRate();
    Counter getStorageBufferSize();
}
