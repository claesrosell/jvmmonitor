/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.cpu;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.jvmmonitor.core.cpu.IMethodNode;

/**
 * The hot spots comparator.
 */
public class HotSpotsComparator extends ViewerComparator {

    /** The column type. */
    public enum ColumnType {

        /** The methods. */
        Methods,

        /** The time in milliseconds. */
        TimeMs,

        /** The time in percentage. */
        TimePercentage,

        /** The invocation count. */
        Count;
    }

    /** the sort direction */
    private int sortDirection;

    /** the column index */
    private final ColumnType columnType;

    /**
     * The constructor.
     * 
     * @param columnType
     *            the column type
     */
    public HotSpotsComparator(ColumnType columnType) {
        this.columnType = columnType;
        if (columnType == ColumnType.Methods) {
            sortDirection = SWT.UP;
        } else {
            sortDirection = SWT.DOWN;
        }
    }

    /*
     * @see ViewerComparator#compare(Viewer, Object, Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int result = 0;

        if (!(e1 instanceof IMethodNode) || !(e2 instanceof IMethodNode)) {
            return result;
        }

        IMethodNode method1 = (IMethodNode) e1;
        IMethodNode method2 = (IMethodNode) e2;

        if (columnType == ColumnType.Methods) {
            result = method1.getName().compareTo(method2.getName());
        } else if (columnType == ColumnType.TimeMs) {
            long time1 = method1.getSelfTime();
            long time2 = method2.getSelfTime();
            result = Long.compare(time1, time2);
        } else if (columnType == ColumnType.TimePercentage) {
            double percentage1 = method1.getSelfTimeInPercentage();
            double percentage2 = method2.getSelfTimeInPercentage();
            result = Double.compare(percentage1, percentage2);
        } else if (columnType == ColumnType.Count) {
            result = Integer.compare(method1.getInvocationCount(), method2.getInvocationCount());
        }

        if (sortDirection == SWT.DOWN) {
            result *= -1;
        }
        return result;
    }

    /**
     * Reverses the sort direction.
     */
    protected void reverseSortDirection() {
        sortDirection = (sortDirection == SWT.UP) ? SWT.DOWN : SWT.UP;
    }

    /**
     * Gets the sort direction.
     * 
     * @return the sort direction
     */
    protected int getSortDirection() {
        return sortDirection;
    }
}
