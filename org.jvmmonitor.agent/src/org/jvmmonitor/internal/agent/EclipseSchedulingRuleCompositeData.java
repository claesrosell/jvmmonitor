/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.agent;

import java.beans.ConstructorProperties;
import java.util.List;

/**
 * The composite data containing eclipse scheduling rule data.
 */
public class EclipseSchedulingRuleCompositeData {

    private final int[][] graph;

    private final List<String> locks;

    private final List<String> lockThreads;

    @ConstructorProperties({ "graph", "locks", "lockThreads" })
    public EclipseSchedulingRuleCompositeData(int[][] graph, List<String> locks, List<String> lockThreads) {
        this.graph = graph;
        this.locks = locks;
        this.lockThreads = lockThreads;
    }

    /**
     * Get the matrix of threads and locks. The integer value means how many times
     * the thread acquired the lock, and "-1" means that the thread is waiting for
     * the lock.
     * 
     * @return the matrix of threads and locks
     */
    public int[][] getGraph() {
        return graph;
    }

    /**
     * Gets the list of job scheduling rule names given by <tt>IJobSchedulingRule.toString()</tt>.
     * 
     * @return the list of job scheduling rule names
     */
    public List<String> getLocks() {
        return locks;
    }

    /**
     * Gets the list of thread names given by <tt>Thread.getName()</tt>.
     * 
     * @return the list of thread names
     */
    public List<String> getLockThreads() {
        return lockThreads;
    }
}
