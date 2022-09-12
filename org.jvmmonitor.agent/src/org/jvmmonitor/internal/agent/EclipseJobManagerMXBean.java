/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.agent;

import javax.management.MXBean;

/**
 * The MXBean to access the job manager in eclipse.
 */
@MXBean
public interface EclipseJobManagerMXBean {

    /** The eclipse job manager MXBean name. */
    final static String ECLIPSE_JOB_MANAGER_MXBEAN_NAME = "org.jvmmonitor:type=Eclipse Job Manager"; //$NON-NLS-1$

    /**
     * Gets the eclipse scheduling rules.
     * 
     * @return The eclipse scheduling rules
     */
    EclipseSchedulingRuleCompositeData getSchedulingRule();

    /**
     * Gets the eclipse jobs.
     * 
     * @return The eclipse jobs
     */
    EclipseJobCompositeData[] getJobs();

    /**
     * Logs the eclipse job manager data to Error Log view.
     */
    void log();
}
