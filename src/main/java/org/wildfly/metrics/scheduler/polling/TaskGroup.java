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
package org.wildfly.metrics.scheduler.polling;

import com.google.common.collect.Iterators;
import org.wildfly.metrics.scheduler.config.Interval;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

/**
 * @author Harald Pehl
 */
public class TaskGroup implements Iterable<Task> {

    private final String id; // to uniquely reference this group
    private final Interval interval; // impacts thread scheduling
    private final long offsetMillis;
    private final LinkedList<Task> tasks;

    public TaskGroup(final Interval interval) {
        this.offsetMillis = 0;
        this.id = UUID.randomUUID().toString();
        this.interval = interval;
        this.tasks = new LinkedList<>();
    }

    public void addTask(Task task) {
        verifyInterval(task);
        tasks.add(task);
    }

    public boolean addTasks(final Collection<? extends Task> collection) {
        for (Task t: collection) {
            verifyInterval(t);
        }
        return tasks.addAll(collection);
    }

    private void verifyInterval(final Task task) {
        if (task.getInterval() != interval) {
            throw new IllegalArgumentException("Wrong interval: Expected " + interval + ", but got " + task.getInterval());
        }
    }

    public int size() {return tasks.size();}

    public boolean isEmpty() {return tasks.isEmpty();}

    @Override
    public Iterator<Task> iterator() {
        return Iterators.unmodifiableIterator(tasks.iterator());
    }

    public String getId() {
        return id;
    }

    public Interval getInterval() {
        return interval;
    }

    public long getOffsetMillis() {
        return offsetMillis;
    }

    public Task getTask(int i) {
        return tasks.get(i);
    }
}
