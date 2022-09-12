/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.agent;

import java.beans.ConstructorProperties;

/**
 * The composite data containing eclipse job data.
 */
public class EclipseJobCompositeData {

    private final String name;

    private final String state;

    private final String className;

    private final boolean isCanceled;

    private final String thread;

    private final String schedulingRule;

    @ConstructorProperties({ "name", "className", "state", "thread", "schedulingRule" })
    public EclipseJobCompositeData(String name, String className, String state, boolean isCanceled, String thread,
            String schedulingRule) {
        this.name = name;
        this.state = state;
        this.className = className;
        this.isCanceled = isCanceled;
        this.thread = thread;
        this.schedulingRule = schedulingRule;
    }

    /**
     * Gets the job name.
     *
     * @return The job name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the job class name.
     *
     * @return The job class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the job state.
     *
     * @return The job state
     */
    public String getState() {
        return state;
    }

    /**
     * Gets the state indicating if the job is canceled.
     *
     * @return The state indicating if the job is canceled
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * Gets the thread where the job runs.
     *
     * @return The thread
     */
    public String getThread() {
        return thread;
    }

    /**
     * Gets the scheduling rule with which the job runs.
     *
     * @return The scheduling rule
     */
    public String getSchedulingRule() {
        return schedulingRule;
    }
}
