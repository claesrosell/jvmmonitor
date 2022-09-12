/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core;

/**
 * The eclipse job element.
 */
public interface IEclipseJobElement {

    /**
     * Gets the job name.
     * 
     * @return The job name
     */
    public String getName();

    /**
     * Gets the job class name.
     * 
     * @return The job class name
     */
    public String getClassName();

    /**
     * Gets the job state.
     * 
     * @return The job state
     */
    public String getState();

    /**
     * Gets the state indicating if the job is canceled.
     * 
     * @return The state indicating if the job is canceled
     */
    public boolean isCanceled();

    /**
     * Gets the thread where the job runs.
     * 
     * @return The thread
     */
    public String getThread();

    /**
     * Gets the scheduling rule with which the job runs.
     * 
     * @return The scheduling rule
     */
    public String getSchedulingRule();
}
