/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.commands;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * The command to invoke MXBean operation.
 */
public class InvokeOperation {

    /** The properties key for local connector address. */
    private static final String LOCAL_CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

    private String pid;

    private String mxBeanName;

    private String operationName;

    private Object[] params;

    private String[] signature;

    /**
     * @param args
     *            the arguments where command name and options are supposed to be given
     * @throws CommandException
     *             failed to execute command
     */
    public static void main(String[] args) throws CommandException {
        InvokeOperation invokeOperation = new InvokeOperation();
        invokeOperation.parseArgs(args);
        invokeOperation.invoke();
    }

    private void parseArgs(String[] args) {
        if (args.length != 5) {
            System.err.println("ERROR: illegal number of parameters");
            showUsage();
            System.exit(1);
        }

        pid = args[0];
        mxBeanName = args[1];
        operationName = args[2];
        if (args[3].contains(",")) {
            params = args[3].split(",");
        } else {
            params = new Object[0];
        }
        if (args[3].contains(",")) {
            signature = args[4].split(",");
        } else {
            signature = new String[0];
        }

        // check if pid is integer
        try {
            Integer.valueOf(pid);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: invalid pid: " + pid);
            showUsage();
            System.exit(1);
        }

        // check if parameters match with signature
        if (params.length != signature.length) {
            System.err.println("ERROR: parameters don't match with signature: " + params + ":" + signature);
            showUsage();
            System.exit(1);
        }
    }

    private void invoke() throws CommandException {
        JMXServiceURL jmxUrl = getJMXServiceURL();
        try {
            JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
            MBeanServerConnection connection = jmxc.getMBeanServerConnection();
            ObjectName objectName = new ObjectName(mxBeanName);
            connection.invoke(objectName, operationName, params, signature);
            System.out.println("operation invoked successfully");
        } catch (IOException | MalformedObjectNameException | InstanceNotFoundException | MBeanException
                | ReflectionException e) {
            throw new CommandException("ERROR: failed to invoke operation", e);
        }
    }

    private JMXServiceURL getJMXServiceURL() throws CommandException {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(pid);
            virtualMachine.startLocalManagementAgent();

            String localConnectorAddress = (String) virtualMachine.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS);
            return new JMXServiceURL(localConnectorAddress);
        } catch (IOException | AttachNotSupportedException e) {
            throw new CommandException("ERROR: failed to load agent", e);
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
        builder.append(" org.jvmmonitor.commands.InvokeOperation");
        builder.append(
                " <pid> <MXBean name> <operation name> <parameters separated by comma> <signature separated by comma>");
        System.err.println(builder.toString());
    }
}
