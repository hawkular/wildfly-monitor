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

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class Address implements Iterable<Address.Tuple> {

    public static Address apply(String address) {
        List<String> tokens = address == null ? Collections.<String>emptyList() :
                Splitter.on(CharMatcher.anyOf("/="))
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(address);

        List<Tuple> tuples = new ArrayList<>(tokens.size() / 2 + 1);
        for (Iterator<String> iterator = tokens.iterator(); iterator.hasNext(); ) {
            String type = iterator.next();
            String name = iterator.hasNext() ? iterator.next() : "";
            tuples.add(new Tuple(type, name));
        }

        return new Address(tuples);
    }


    private final List<Tuple> tuples;

    private Address(final List<Tuple> tuples) {
        this.tuples = new ArrayList<>();
        if (tuples != null) {
            this.tuples.addAll(tuples);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Address)) { return false; }

        Address address = (Address) o;

        if (!tuples.equals(address.tuples)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return tuples.hashCode();
    }

    @Override
    public String toString() {
        return Joiner.on('/').join(tuples);
    }

    @Override
    public Iterator<Tuple> iterator() {
        return Iterators.unmodifiableIterator(tuples.iterator());
    }

    public boolean isEmpty() {return tuples.isEmpty();}

    public boolean isBalanced() {
        for (Tuple tuple : this) {
            if (tuple.getValue() == null || tuple.getValue().length() == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWith(Tuple tuple) {
        return !tuples.isEmpty() && tuples.get(0).equals(tuple);
    }


    /**
     * @author Harald Pehl
     */
    public static class Tuple {

        public static Tuple apply(String tuple) {
            if (tuple == null) {
                throw new IllegalArgumentException("Tuple must not be null");
            }
            List<String> tuples = Splitter.on('=')
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(tuple);
            if (tuples.isEmpty() || tuples.size() != 2) {
                throw new IllegalArgumentException("Malformed tuple: " + tuple);
            }
            return new Tuple(tuples.get(0), tuples.get(1));
        }

        private final String key;
        private final String value;

        private Tuple(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) { return true; }
            if (!(o instanceof Tuple)) { return false; }

            Tuple that = (Tuple) o;

            if (!value.equals(that.value)) { return false; }
            if (!key.equals(that.key)) { return false; }

            return true;
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
