/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.eclipse.swt.SWT;

/**
 * The eclipse job column.
 */
public enum EclipseJobColumn {

    /** The job name. */
    JOB(Messages.jobColumnLabel, 300, SWT.LEFT, Messages.jobColumnToolTip),

    /** The job class name. */
    CLASS(Messages.classColumnLabel, 300, SWT.LEFT, Messages.classColumnToolTip),

    /** The state state. */
    STATE(Messages.stateColumnLabel, 130, SWT.LEFT, Messages.stateColumnToolTip),

    /** The progress monitor state. */
    PROGRESS_MONITOR_STATE(Messages.progressMonitorStateColumnLabel, 160, SWT.LEFT, Messages.progressMonitorStateColumnToolTip),

    /** The thread. */
    THREAD(Messages.threadColumnLabel, 300, SWT.LEFT, Messages.threadColumnToolTip),

    /** The scheduling rule. */
    SCHEDULING_RULE(Messages.schedulingRuleColumnLabel, 300, SWT.LEFT, Messages.schedulingRuleColumnToolTip);

    /** The label. */
    public final String label;

    /** The default column width. */
    public final int defalutWidth;

    /** The initial alignment. */
    public final int initialAlignment;

    /** The tool tip. */
    public final String toolTip;

    private EclipseJobColumn(String label, int defalutWidth, int alignment, String toolTip) {
        this.label = label;
        this.defalutWidth = defalutWidth;
        this.initialAlignment = alignment;
        this.toolTip = toolTip;
    }

    /**
     * Gets the column with given column name.
     *
     * @param columnName
     *                   The column name
     * @return The column
     */
    protected static EclipseJobColumn getColumn(String columnName) {
        for (EclipseJobColumn column : EclipseJobColumn.values()) {
            if (columnName.equals(column.label)) {
                return column;
            }
        }
        throw new IllegalStateException();
    }
}
