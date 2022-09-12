/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.jvmmonitor.core.Activator;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.IAgentLoadHandler;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.tools.Tools;
import org.jvmmonitor.tools.ToolsException;

/**
 * The agent load handler that loads the agent jar file
 * <tt>lib/jvmmonitor-agent.jar</tt> to target JVM.
 */
public class AgentLoadHandler implements IAgentLoadHandler {

    /** The bundle root path. */
    private static final String BUNDLE_ROOT_PATH = "/";

    /** The relative path for jvmmonitor agent jar file. */
    private static final String JVMMONITOR_AGENT_JAR = File.separator + "lib" + File.separator + "jvmmonitor-agent.jar";

    /** The relative path for jvmmonitor commands jar file. */
    private static final String JVMMONITOR_COMMANDS_JAR = File.separator + "lib" + File.separator
            + "jvmmonitor-commands.jar";

    /** The system property key for Java home. */
    private static final String JAVA_HOME_PROPERTY_KEY = "java.home";

    /** The main class to load agent. */
    private static final String LOAD_AGENT = "org.jvmmonitor.commands.LoadAgent";

    /** The classpath option. */
    private static final String CLASSPATH_OPTION = "-classpath";

    /** The java executable path. */
    private static final String JAVA_EXECUTABLE = "bin" + File.separator + "java";

    /** The absolute path for jvmmonitor-agent.jar. */
    private final String agentJarAbsolutePath;

    /** The absolute path for jvmmonitor-commands.jar. */
    private final String commandsJarAbsolutePath;

    /** The state indicating if agent is loaded. */
    private boolean isAgentLoaded;

    /**
     * The constructor.
     */
    public AgentLoadHandler() {
        isAgentLoaded = false;
        agentJarAbsolutePath = getAbsolutePath(JVMMONITOR_AGENT_JAR);
        commandsJarAbsolutePath = getAbsolutePath(JVMMONITOR_COMMANDS_JAR);
    }

    /*
     * @see IAgentLoadHandler#loadAgent(IActiveJvm)
     */
    @Override
    public void loadAgent(IActiveJvm jvm) throws JvmCoreException {
        if (agentJarAbsolutePath == null) {
            return;
        }

        // short term hack: load agent from a separate JVM to avoid error when loading to current JVM
        if (jvm.isCurrentJvm()) {
            try {
                loadJvmMonitorAgent(jvm.getPid());
                isAgentLoaded = true;
            } catch (JvmCoreException e) {
                Activator.log(IStatus.ERROR, NLS.bind(Messages.loadAgentFailedMsg, agentJarAbsolutePath), e);
            }
            return;
        }

        try {
            Tools.loadAgent(jvm.getPid(), agentJarAbsolutePath);
            isAgentLoaded = true;
        } catch (ToolsException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.loadAgentFailedMsg, agentJarAbsolutePath), e);
        }
    }

    /*
     * @see IAgentLoadHandler#isAgentLoaded()
     */
    @Override
    public boolean isAgentLoaded() {
        return isAgentLoaded;
    }

    /**
     * Searches the given jar file.
     */
    private static String getAbsolutePath(String jarFile) {
        URL entry = org.jvmmonitor.core.Activator.getDefault().getBundle().getEntry(BUNDLE_ROOT_PATH);
        String corePluginPath;
        try {
            corePluginPath = FileLocator.resolve(entry).getPath();
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, Messages.corePluginNoFoundMsg, new Exception());
            return null;
        }

        File corePlugin = new File(corePluginPath);

        if (!corePlugin.exists()) {
            // the bundle location is a relative path on linux
            String installationLication = Platform.getInstallLocation().getURL().getPath();
            corePlugin = new File(installationLication + corePluginPath);
            if (!corePlugin.exists()) {
                Activator.log(IStatus.ERROR, Messages.corePluginNoFoundMsg, new Exception());
                return null;
            }
        }

        File agentJar = new File(corePlugin + jarFile);
        if (!agentJar.exists()) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.jarFileNotFoundMsg, agentJar.getAbsolutePath()),
                    new Exception());
            return null;
        }

        String jarPath = agentJar.getAbsolutePath();

        Activator.log(IStatus.INFO, NLS.bind(Messages.jarFileFoundMsg, jarPath), new Exception());
        return jarPath;
    }

    private void loadJvmMonitorAgent(int pid) throws JvmCoreException {
        List<String> commandList = new ArrayList<>();
        commandList.add(System.getProperty(JAVA_HOME_PROPERTY_KEY) + File.separator + JAVA_EXECUTABLE);
        commandList.add(CLASSPATH_OPTION);
        commandList.add(commandsJarAbsolutePath);
        commandList.add(LOAD_AGENT);
        commandList.add(String.valueOf(pid));
        commandList.add(agentJarAbsolutePath);

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commandList.toArray(new String[0]));
            try {
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    throw new JvmCoreException(IStatus.ERROR, Messages.loadAgentCommandFailedMsg, new Exception());
                }
            } catch (InterruptedException e) {
                // does nothing
            }

            InputStream errorStream = process.getErrorStream();
            String result = new BufferedReader(new InputStreamReader(errorStream)).lines()
                    .collect(Collectors.joining("\n"));
            if (!result.isEmpty()) {
                throw new JvmCoreException(IStatus.ERROR, result, new Exception());
            }
        } catch (IOException e) {
            throw new JvmCoreException(IStatus.ERROR, Messages.loadAgentCommandFailedMsg, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

}
