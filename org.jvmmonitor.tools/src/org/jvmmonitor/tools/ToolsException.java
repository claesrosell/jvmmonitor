/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.tools;

/**
 * The exception that is thrown when the access to <tt>com.sun.tools</tt> fails.
 */
public class ToolsException extends Exception {

    private static final long serialVersionUID = 1L;

    ToolsException(String message, Throwable e) {
        super(message, e);
    }
}
