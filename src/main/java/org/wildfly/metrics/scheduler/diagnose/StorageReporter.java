package org.wildfly.metrics.scheduler.diagnose;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import org.wildfly.metrics.scheduler.config.Address;
import org.wildfly.metrics.scheduler.config.Interval;
import org.wildfly.metrics.scheduler.polling.Task;
import org.wildfly.metrics.scheduler.storage.Sample;
import org.wildfly.metrics.scheduler.storage.StorageAdapter;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author Heiko Braun
 * @since 14/10/14
 */
public class StorageReporter extends ScheduledReporter {

    private static StorageAdapter storageAdapter;
    private final Locale locale;
    private final Clock clock;
    private final DateFormat dateFormat;

    private StorageReporter(MetricRegistry registry,
                            Locale locale,
                            Clock clock,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter, StorageAdapter storageAdapter) {
        super(registry, "storage-reporter", filter, rateUnit, durationUnit);
        this.locale = locale;
        this.clock = clock;
        this.storageAdapter = storageAdapter;
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.MEDIUM,
                locale);
        dateFormat.setTimeZone(timeZone);
    }


    @Override
    public void report(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {

        if (!gauges.isEmpty()) {

            Set<Sample> samples = new HashSet<>(gauges.size());
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                Gauge<Integer> gauge = entry.getValue();

                samples.add(
                        new Sample(
                                new Task("foo", "bar", Address.apply("service=metric-scheduler"), entry.getKey(), null, Interval.EACH_SECOND),
                                gauge.getValue()
                        )
                );
            }
            storageAdapter.store(samples);
        }

        if (!counters.isEmpty()) {

            Set<Sample> samples = new HashSet<>(counters.size());
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                samples.add(
                        new Sample(
                                new Task("foo", "bar", Address.apply("service=metric-scheduler"), entry.getKey(), null, Interval.EACH_SECOND),
                                entry.getValue().getCount()
                        )
                );
            }

            storageAdapter.store(samples);

        }

        /*if (!histograms.isEmpty()) {

            Set<Sample> samples = new HashSet<>(histograms.size());
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                Histogram hist = entry.getValue();
                samples.add(
                        new Sample(
                                new Task(Address.apply("service=metric-scheduler"), entry.getKey(), null, Interval.EACH_SECOND),
                                0 // TODO
                        )
                );
            }
            storageAdapter.store(samples);
        }*/

        if (!meters.isEmpty()) {

            Set<Sample> samples = new HashSet<>(meters.size());
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                Meter meter = entry.getValue();
                samples.add(
                        new Sample(
                                new Task("foo", "bar", Address.apply("service=metric-scheduler"), entry.getKey(), null, Interval.EACH_SECOND),
                                meter.getOneMinuteRate()
                        )
                );
            }
            storageAdapter.store(samples);
        }

        if (!timers.isEmpty()) {

            Set<Sample> samples = new HashSet<>(timers.size());

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                Timer timer = entry.getValue();

                samples.add(
                        new Sample(
                                new Task("foo", "bar", Address.apply("service=metric-scheduler"), entry.getKey(), null, Interval.EACH_SECOND),
                                timer.getSnapshot().get75thPercentile()
                        )
                );
            }

            storageAdapter.store(samples);
        }

    }

    public static Builder forRegistry(MetricRegistry registry, StorageAdapter storageAdapter) {
        return new Builder(registry, storageAdapter);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private final StorageAdapter storageAdapter;
        private PrintStream output;
        private Locale locale;
        private Clock clock;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry, StorageAdapter storageAdapter) {
            this.registry = registry;
            this.storageAdapter = storageAdapter;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Write to the given {@link PrintStream}.
         *
         * @param output a {@link PrintStream} instance.
         * @return {@code this}
         */
        public Builder outputTo(PrintStream output) {
            this.output = output;
            return this;
        }

        /**
         * Format numbers for the given {@link Locale}.
         *
         * @param locale a {@link Locale}
         * @return {@code this}
         */
        public Builder formattedFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Use the given {@link TimeZone} for the time.
         *
         * @param timeZone a {@link TimeZone}
         * @return {@code this}
         */
        public Builder formattedFor(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public StorageReporter build() {
            return new StorageReporter(registry,
                    locale,
                    clock,
                    timeZone,
                    rateUnit,
                    durationUnit,
                    filter, storageAdapter
            );
        }
    }

}
