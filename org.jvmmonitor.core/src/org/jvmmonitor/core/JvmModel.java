/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.jvmmonitor.core.JvmModelEvent.State;
import org.jvmmonitor.internal.core.ActiveJvm;
import org.jvmmonitor.internal.core.AgentLoadHandler;
import org.jvmmonitor.internal.core.HeapDumpHandler;
import org.jvmmonitor.internal.core.Host;
import org.jvmmonitor.internal.core.JvmAttachHandler;
import org.jvmmonitor.internal.core.Util;

/**
 * The JVM model that is singleton having hosts and JVMs.
 */
public class JvmModel {

    /** The shared instance of JVM model. */
    private static JvmModel jvmModel;

    /** The hosts. */
    private final List<IHost> hosts;

    /** The JVM model change listeners. */
    private final List<IJvmModelChangeListener> listeners;

    /** The JVM attach handler. */
    private final IJvmAttachHandler jvmAttachHandler;

    /** The heap dump handler. */
    private final IHeapDumpHandler heapDumpHandler;

    /** The agent load handler. */
    private final IAgentLoadHandler agentLoadHandler;

    /**
     * The constructor.
     */
    private JvmModel() {
        hosts = new CopyOnWriteArrayList<>();
        listeners = new CopyOnWriteArrayList<>();
        jvmAttachHandler = new JvmAttachHandler();
        heapDumpHandler = new HeapDumpHandler();
        agentLoadHandler = new AgentLoadHandler();

        restoreHosts();
    }

    /**
     * Gets the shared instance of JVM model.
     *
     * @return The shared instance of JVM model
     */
    public static synchronized JvmModel getInstance() {
        if (jvmModel == null) {
            jvmModel = new JvmModel();
        }
        return jvmModel;
    }

    /**
     * Gets the heap dump handler.
     *
     * @return The heap dump handler, or <tt>null</tt> if not available
     */
    public IHeapDumpHandler getHeapDumpHandler() {
        return heapDumpHandler;
    }

    /**
     * Gets the agent load handler.
     *
     * @return The agent load handler, or <tt>null</tt> if not available
     */
    public IAgentLoadHandler getAgentLoadHandler() {
        return agentLoadHandler;
    }

    /**
     * Gets the hosts.
     *
     * @return the hosts
     */
    public List<IHost> getHosts() {
        return hosts;
    }

    /**
     * Gets the host corresponding to the given host name.
     *
     * @param hostname
     *            The host name
     * @return The host, or <tt>null</tt> if not found
     */
    public IHost getHost(String hostname) {
        for (IHost host : hosts) {
            if (host.getName().equals(hostname)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Adds the host.
     *
     * @param hostname
     *            The host name
     * @return The host
     */
    public IHost addHost(String hostname) {
        IHost host = getHost(hostname);
        if (host != null) {
            return host;
        }

        host = new Host(hostname);
        hosts.add(host);

        fireJvmModelChangeEvent(new JvmModelEvent(State.HostAdded, null));
        return host;
    }

    /**
     * Adds the host and JVM. If the host corresponding to the given JMX URL
     * already exists, only JVM will be added to model.
     *
     * @param url
     *            The JMX URL
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @return The active JVM
     * @throws JvmCoreException
     *             if connecting to JVM fails
     */
    public IActiveJvm addHostAndJvm(String url, String userName,
            String password) throws JvmCoreException {
        IActiveJvm jvm = new ActiveJvm(url, userName, password);

        IHost host = jvm.getHost();
        if (host == null) {
            return jvm;
        }

        if (!hosts.contains(host)) {
            hosts.add(host);
            fireJvmModelChangeEvent(new JvmModelEvent(State.HostAdded, null));
        }
        return jvm;
    }

    /**
     * Removes the host.
     *
     * @param host
     *            The host
     */
    public void removeHost(IHost host) {
        hosts.remove(host);
        try {
            IPath hostDir = ((Host) host).getHostDir();
            Util.deleteDir(hostDir.toFile());
        } catch (JvmCoreException e) {
            Activator.log(IStatus.ERROR,
                    NLS.bind(Messages.removeHostFailedMsg, host.getName()), e);
        }
        fireJvmModelChangeEvent(new JvmModelEvent(State.HostRemoved, null));
    }

    /**
     * Adds the JVM model change listener.
     *
     * @param listener
     *            The JVM model change listener
     */
    public void addJvmModelChangeListener(IJvmModelChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the JVM model change listener.
     *
     * @param listener
     *            The JVM model change listener
     */
    public void removeJvmModelChangeListener(IJvmModelChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gets the JVM model change listeners.
     *
     * @return The JVM model change listeners
     */
    public List<IJvmModelChangeListener> getJvmModelChangeListeners() {
        return listeners;
    }

    /**
     * Fires the JVM model change event.
     *
     * @param e
     *            The JVM model changed event
     */
    public void fireJvmModelChangeEvent(JvmModelEvent e) {
        for (IJvmModelChangeListener listener : listeners) {
            listener.jvmModelChanged(e);
        }
    }

    /**
     * Restores the hosts.
     */
    private void restoreHosts() {
        IHost localhost = addHost(IHost.LOCALHOST);
        if (jvmAttachHandler != null) {
            jvmAttachHandler.setHost(localhost);
        }

        IPath baseDir = Activator.getDefault().getStateLocation();

        File[] files = baseDir.toFile().listFiles();
        if (files == null) {
            return;
        }

        for (File dir : files) {
            if (!dir.isDirectory()) {
                continue;
            }

            String hostDir = dir.getName();
            if (!hostDir.endsWith(Host.DIR_SUFFIX)) {
                continue;
            }

            IPath filePath = baseDir.append(File.separator + hostDir
                    + File.separator + Host.PROPERTIES_FILE);
            if (!filePath.toFile().exists()) {
                Util.deleteDir(dir);
                continue;
            }

            Properties props = Util.loadProperties(filePath);
            if (props == null) {
                Util.deleteDir(dir);
                continue;
            }

            String hostname = props.getProperty(Host.HOST_PROP_KEY);
            if (hostname != null) {
                addHost(hostname);
            }
        }
    }

}
