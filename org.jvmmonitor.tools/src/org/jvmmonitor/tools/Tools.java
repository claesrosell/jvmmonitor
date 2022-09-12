/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * The class to access <tt>com.sun.tools</tt> in the module <tt>jdk.attach</tt>, and to execute jmap implemented at
 * <tt>sun.tools</tt>.
 */
public class Tools {

    /** The properties key for local connector address. */
    private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    /** The system property key for Java home. */
    private static final String JAVA_HOME_PROPERTY_KEY = "java.home";

    /** The jmap executable. */
    private static final String JMAP_EXECUTABLE = "bin" + File.separator + "jmap";

    /** The histo option for jmap. */
    private static final String JMAP_HISTO_OPTION = "-histo:live";

    /** The charset UTF8. */
    private static final String UTF8 = "UTF8";

    /** The buffer size to transfer heap dump data from target JVM to eclipse. */
    private static final int BUFFER_SIZE = 2048;

    /**
     * Gets the local connector address.
     *
     * @param pid
     *            The process ID
     * @return The local connector address
     * @throws ToolsException
     *             failed to get local connector address
     */
    public static String getLocalConnectorAddress(int pid) throws ToolsException {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(String.valueOf(pid));
            virtualMachine.startLocalManagementAgent();

            Properties props = virtualMachine.getAgentProperties();
            return (String) props.get(LOCAL_CONNECTOR_ADDRESS);
        } catch (IOException | AttachNotSupportedException e) {
            throw new ToolsException(e.getMessage(), e);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Loads the agent jvmmonitor-agent.jar to target JVM.
     *
     * @param pid
     *            The pid for JVM
     * @param agentJarAbsolutePath
     *            the absolute path for jvmmonitor-agent.jar
     * @throws ToolsException
     *             failed to load agent
     */
    public static void loadAgent(int pid, String agentJarAbsolutePath) throws ToolsException {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(String.valueOf(pid));
            virtualMachine.loadAgent(agentJarAbsolutePath, agentJarAbsolutePath);
        } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            throw new ToolsException(e.getMessage(), e);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Gets the heap histogram from target JVM by calling jmap with -histo option.
     * <p>
     * e.g.
     *
     * <pre>
     *  num     #instances         #bytes  class name
     * ----------------------------------------------
     *    1:         18329        2104376  &lt;constMethodKlass&gt;
     *    2:         18329        1479904  &lt;methodKlass&gt;
     *    3:          2518        1051520  [B
     *    4:         11664         989856  [C
     *    5:         11547         277128  java.lang.String
     * </pre>
     *
     * @param pid
     *            The pid for JVM
     * @param isLive
     *            True to dump only live objects
     * @return The heap histogram
     * @throws ToolsException
     *             failed to get heap histogram
     */
    public static String getHeapHistogram(int pid, boolean isLive) throws ToolsException {

        byte[] bytes = new byte[BUFFER_SIZE];
        int length;
        StringBuilder builder = new StringBuilder();

        Process process = executeJmapWithHistoOption(pid, isLive);
        try (InputStream in = process.getInputStream()) {
            while ((length = in.read(bytes)) != -1) {
                String string = new String(bytes, 0, length, UTF8);
                builder.append(string);
            }
        } catch (UnsupportedEncodingException e) {
            throw new ToolsException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ToolsException(e.getMessage(), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return builder.toString();
    }

    private static Process executeJmapWithHistoOption(int pid, boolean isLive) throws ToolsException {

        List<String> commandList = new ArrayList<>();
        commandList.add(System.getProperty(JAVA_HOME_PROPERTY_KEY) + File.separator + JMAP_EXECUTABLE);
        commandList.add(JMAP_HISTO_OPTION);
        commandList.add(String.valueOf(pid));

        try {
            return Runtime.getRuntime().exec(commandList.toArray(new String[0]));
        } catch (IOException e) {
            throw new ToolsException(e.getMessage(), e);
        }
    }

}
