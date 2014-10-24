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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of {@link ResourceRef}s with a unique id.
 *
 * @author Harald Pehl
 */
public class ConfigurationInstance implements Configuration {

    private final List<ResourceRef> resourceRefs;
    private int schedulerThreads = 2;

    private String host;
    private int port;
    private String user;
    private String password;

    private String storageUrl = null;
    private String storageUser = null;
    private String storagePassword = null;
    private String storageDb = null;
    private String storageToken = null;
    private Storage storageAdapter = Storage.INFLUX;

    private Diagnostics diagnostics = Diagnostics.CONSOLE;

    public ConfigurationInstance() {
        this("localhost", 9990, new ArrayList<ResourceRef>());
    }

    public ConfigurationInstance(String host, int port) {
        this(host, port, new ArrayList<ResourceRef>());
    }

    public ConfigurationInstance(String host, int port, final List<ResourceRef> resourceRefs) {
        this.host = host;
        this.port = port;
        this.resourceRefs = resourceRefs;
    }

    public List<ResourceRef> getResourceRefs() {
        return Collections.unmodifiableList(resourceRefs);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSchedulerThreads(int schedulerThreads) {
        this.schedulerThreads = schedulerThreads;
    }

    @Override
    public int getSchedulerThreads() {
        return schedulerThreads;
    }

    @Override
    public String getStorageUrl() {
        return this.storageUrl;
    }

    @Override
    public String getStorageUser() {
        return storageUser;
    }

    @Override
    public String getStoragePassword() {
        return storagePassword;
    }

    @Override
    public String getStorageDBName() {
        return storageDb;
    }


    @Override
    public Storage getStorageAdapter() {
        return storageAdapter;
    }

    public void setStorageAdapter(Storage storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public void setStorageUser(String storageUser) {
        this.storageUser = storageUser;
    }

    public void setStoragePassword(String storagePassword) {
        this.storagePassword = storagePassword;
    }

    public void setStorageDb(String storageDb) {
        this.storageDb = storageDb;
    }

    public void addResourceRef(ResourceRef ref) {
        resourceRefs.add(ref);
    }

    public String getStorageDb() {
        return storageDb;
    }

    public String getStorageToken() {
        return storageToken;
    }

    public void setStorageToken(String storageToken) {
        this.storageToken = storageToken;
    }

    public String getUser() {
        return user;
    }

    public Diagnostics getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }
}

