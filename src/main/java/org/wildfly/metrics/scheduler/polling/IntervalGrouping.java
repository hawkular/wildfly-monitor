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

import org.wildfly.metrics.scheduler.config.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class IntervalGrouping implements TaskGrouping {

    @Override
    public List<TaskGroup> apply(final List<Task> tasks) {

        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return new Long(t1.getInterval().millis()).compareTo(t2.getInterval().millis());
            }
        });


        List<TaskGroup> groups = new ArrayList<>();
        Interval interval = tasks.get(0).getInterval();
        TaskGroup taskGroup = new TaskGroup(interval);
        groups.add(taskGroup);

        for (Task task : tasks) {

            if(!task.getInterval().equals(interval)) {
                // new group
                interval = task.getInterval();
                groups.add(new TaskGroup(task.getInterval()));
            }

            groups.get(groups.size()-1).addTask(task);
        }

        return groups;
    }
}
