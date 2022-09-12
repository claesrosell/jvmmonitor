/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import static org.jvmmonitor.core.IPreferenceConstants.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.jvmmonitor.core.Activator;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.IHost;
import org.jvmmonitor.core.IJvmAttachHandler;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.tools.Tools;
import org.jvmmonitor.tools.ToolsException;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * The JVM attach handler that contributes to the extension point
 * <tt>org.jvmmonitor.core.jvmAttachHandler</tt>.
 */
public class JvmAttachHandler implements IJvmAttachHandler,
        IPreferenceChangeListener {

    /** The properties key for Java command. */
    private static final String JAVA_COMMAND_KEY = "sun.java.command"; //$NON-NLS-1$

    /** The delimiter for Java executable options. */
    private static final String JAVA_OPTIONS_DELIMITER = " -"; //$NON-NLS-1$

    /** The jmap class that has main method for jmap. */
    private static final String JMAP_CLASS_NAME = "sun.tools.jmap.JMap"; //$NON-NLS-1$

    /** The local host. */
    private IHost localhost;

    /** The timer. */
    Timer timer;

    /*
     * @see IJvmAttachHandler#setHost(IHost)
     */
    @Override
    public void setHost(IHost host) {
        this.localhost = host;
        InstanceScope.INSTANCE.getNode(PREFERENCES_ID).addPreferenceChangeListener(this);

        startMonitoring();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
        if (SEARCH_JVM_PERIOD.equals(event.getKey())) {
            startMonitoring();
        }
    }

    /**
     * Starts monitoring.
     */
    private void startMonitoring() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    updatesActiveJvms();
                } catch (Throwable t) {
                    Activator.log(IStatus.ERROR,
                            Messages.updateTimerCanceledMsg, t);
                    timer.cancel();
                }
            }
        };

        long period = InstanceScope.INSTANCE.getNode(PREFERENCES_ID).getLong(SEARCH_JVM_PERIOD,
                DEFAULT_SEARCH_JVM_PERIOD);
        timer.schedule(timerTask, 0, period);
    }

    /**
     * Updates the active JVMs.
     *
     * @throws JvmCoreException
     */
    void updatesActiveJvms() throws JvmCoreException {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();

        Map<Integer, VirtualMachineDescriptor> activeJvms = new HashMap<>();
        for (VirtualMachineDescriptor vm: vms) {
            activeJvms.put(Integer.valueOf(vm.id()), vm);
        }

        // add JVMs
        List<IActiveJvm> previousVms = localhost.getActiveJvms();
        for (Entry<Integer, VirtualMachineDescriptor> entry : activeJvms.entrySet()) {
            if (containJvm(previousVms, entry.getKey())) {
                continue;
            }

            addActiveJvm(entry.getKey(), entry.getValue());
        }

        // remove JVMs
        for (IActiveJvm jvm : previousVms) {
            Integer pid = jvm.getPid();
            if (!activeJvms.keySet().contains(pid)) {
                localhost.removeJvm(pid);
            }
        }
    }

    /**
     * Checks if the given list of JVMs contains the given pid.
     *
     * @param jvms
     *            The list of active JVMs
     * @param pid
     *            The pid
     * @return True if the given list of JVMs contains the given pid
     */
    private static boolean containJvm(List<IActiveJvm> jvms, int pid) {
        for (IActiveJvm jvm : jvms) {
            if (jvm.getPid() == pid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the active JVM.
     *
     * @param pid
     *            The pid
     * @param vm
     *            the virtual machine descriptor
     */
    private void addActiveJvm(int pid, VirtualMachineDescriptor vm) {

        // don't monitor jmap which may be started by JVM Monitor as a workaround for now
        if (vm.displayName().contains(JMAP_CLASS_NAME)) {
            return;
        }

        String mainClass = null;
        String localConnectorAddress = null;
        String stateMessage = null;

        boolean isCurrentJvm = ManagementFactory.getRuntimeMXBean().getName().startsWith(pid + "@"); //$NON-NLS-1$
        mainClass = getMainClass(vm, pid, isCurrentJvm);
        try {
            localConnectorAddress = Tools.getLocalConnectorAddress(pid);
        } catch (ToolsException e) {
            stateMessage = e.getMessage();
            String message = NLS.bind(Messages.getLocalConnectorAddressFailedMsg, pid);
            Activator.log(IStatus.WARNING, message, e);
        }

        try {
            localhost.addLocalActiveJvm(pid, mainClass, localConnectorAddress,
                    stateMessage);
        } catch (JvmCoreException e) {
            String message = NLS.bind(Messages.connectTargetJvmFailedMsg, pid);
            Activator.log(IStatus.WARNING, message, e);
        }
    }

    private static String getMainClass(VirtualMachineDescriptor vm, int pid, boolean isCurrentJvm) {

        String javaCommand = ""; //$NON-NLS-1$
        if (isCurrentJvm) {
            // the target JVM is current JVM where JVM Monitor is running
            javaCommand = getCurrentJavaCommand();
        } else {
            javaCommand = getJavaCommand(pid);
        }

        /*
         * javaCommand contains Java executable options that are sorted so that the main class or jar comes first.
         */
        return javaCommand.split(JAVA_OPTIONS_DELIMITER)[0];
    }

    private static String getCurrentJavaCommand() {
        Object value = System.getProperty(JAVA_COMMAND_KEY);
        if (value != null) {
            return value.toString();
        }
        return "";
    }

    private static String getJavaCommand(int pid) {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(String.valueOf(pid));
            Properties props = virtualMachine.getSystemProperties();
            Object value = props.get(JAVA_COMMAND_KEY);
            if (value != null) {
                return value.toString();
            }
        } catch (AttachNotSupportedException e) {
            String message = NLS.bind(Messages.getMainClassNameFailed, pid);
            Activator.log(IStatus.ERROR, message, e);
        } catch (IOException e) {
            // JVM may be gone in the meantime
        }finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return "";//$NON-NLS-1$
    }

}
