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

import org.jboss.dmr.ModelNode;
import org.wildfly.metrics.scheduler.config.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a {@code read-attribute} operation of the given {@link TaskGroup}.
 *
 * @author Harald Pehl
 */
public class ReadAttributeOperationBuilder implements OperationBuilder {

    @Override
    public ModelNode createOperation(final TaskGroup group) {

        if (group.isEmpty()) {
            throw new IllegalArgumentException("Empty groups are not allowed");
        }

        ModelNode comp = new ModelNode();
        List<ModelNode> steps = new ArrayList<>();
        comp.get("address").setEmptyList();
        comp.get("operation").set("composite");
        for (Task task : group) {
            steps.add(readAttribute(task));
        }
        comp.get("steps").set(steps);

        return comp;

    }

    private ModelNode readAttribute(Task task) {
        ModelNode node = new ModelNode();
        Address address = task.getAddress();
        for (Address.Tuple tuple : address) {
            node.get("address").add(tuple.getKey(), tuple.getValue());
        }
        node.get("operation").set("read-attribute");
        node.get("name").set(task.getAttribute());
        return node;
    }
}
