/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core;

/**
 * JVM attach handler.
 */
public interface IJvmAttachHandler {

    /**
     * Sets the local host.
     *
     * @param localhost
     *            The local host
     */
    void setHost(IHost localhost);

}