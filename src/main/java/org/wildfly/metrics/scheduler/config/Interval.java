/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.metrics.scheduler.config;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author Harald Pehl
 */
public class Interval{

    public final static Interval EACH_SECOND = new Interval(1, SECONDS);
    public final static Interval TWENTY_SECONDS = new Interval(20, SECONDS);
    public final static Interval EACH_MINUTE = new Interval(1, MINUTES);
    public final static Interval TWENTY_MINUTES = new Interval(20, MINUTES);
    public final static Interval EACH_HOUR = new Interval(1, HOURS);
    public final static Interval FOUR_HOURS = new Interval(4, HOURS);
    public final static Interval EACH_DAY = new Interval(24, HOURS);

    private final int val;
    private final TimeUnit unit;

    public Interval(int val, TimeUnit unit) {
        this.val = val;
        this.unit = unit;
    }

    public long millis() {
        return MILLISECONDS.convert(val, unit);
    }

    public int getVal() {
        return val;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
