/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.commands;

import java.io.File;
import java.io.IOException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * The command to load agent.
 */
public class LoadAgent {

    private String pid;

    private String agentPath;

    /**
     * @param args
     *            the arguments where operation and options are supposed to be given
     * @throws CommandException
     *             failed to load agent
     */
    public static void main(String[] args) throws CommandException {
        LoadAgent agentLoader = new LoadAgent();
        agentLoader.parseArgs(args);
        agentLoader.loadAgent();
    }

    private void parseArgs(String[] args) {
        if (args.length != 2) {
            System.err.println("ERROR: illegal number of parameters");
            showUsage();
            System.exit(1);
        }

        pid = args[0];
        agentPath = args[1];

        // check if pid is integer
        try {
            Integer.valueOf(pid);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: invalid pid: " + pid);
            showUsage();
            System.exit(1);
        }

        // check if given agent path exist
        if (!new File(agentPath).exists()) {
            System.err.println("ERROR: agent jar doesn't exist: " + agentPath);
            showUsage();
            System.exit(1);
        }
    }

    private void loadAgent() throws CommandException {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(pid);
            virtualMachine.loadAgent(agentPath, agentPath);
            System.out.println("agent loaded successfully");
        } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            throw new CommandException("failed to load agent", e);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    throw new CommandException("ERROR: failed to detach", e);
                }
            }
        }
    }

    private static void showUsage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Usage: java -cp <path for jvmmonitor-commands.jar>");
        builder.append(" org.jvmmonitor.commands.LoadAgent");
        builder.append(" <pid> <path for agent jar to load>");
        System.err.println(builder.toString());
    }
}
