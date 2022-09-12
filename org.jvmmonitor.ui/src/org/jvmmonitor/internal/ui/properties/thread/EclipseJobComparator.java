/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.jvmmonitor.core.IEclipseJobElement;

/**
 * The eclipse job comparator.
 */
public class EclipseJobComparator extends ViewerComparator {

    private int sortDirection;

    private final int columnIndex;

    /**
     * The constructor.
     *
     * @param columnIndex
     *                    the column index
     */
    public EclipseJobComparator(int columnIndex) {
        this.columnIndex = columnIndex;
        if (columnIndex == 0) {
            sortDirection = SWT.UP;
        } else {
            sortDirection = SWT.DOWN;
        }
    }

    @Override
    public int compare(Viewer treeViewer, Object e1, Object e2) {
        int result = 0;

        if (!(e1 instanceof IEclipseJobElement) || !(e2 instanceof IEclipseJobElement)
                || !(treeViewer instanceof TreeViewer)) {
            return result;
        }

        IEclipseJobElement element1 = (IEclipseJobElement) e1;
        IEclipseJobElement element2 = (IEclipseJobElement) e2;

        Tree tree = ((TreeViewer) treeViewer).getTree();
        if (columnIndex == getColumnIndex(tree, EclipseJobColumn.JOB)) {
            result = super.compare(treeViewer, element1.getName(), element2.getName());
        } else if (columnIndex == getColumnIndex(tree, EclipseJobColumn.CLASS)) {
            result = super.compare(treeViewer, element1.getClassName(), element2.getClassName());
        } else if (columnIndex == getColumnIndex(tree, EclipseJobColumn.STATE)) {
            result = super.compare(treeViewer, element1.getState(), element2.getState());
        } else if (columnIndex == getColumnIndex(tree, EclipseJobColumn.PROGRESS_MONITOR_STATE)) {
            result = Boolean.compare(element1.isCanceled(), element2.isCanceled());
        } else if (columnIndex == getColumnIndex(tree, EclipseJobColumn.THREAD)) {
            result = super.compare(treeViewer, element1.getThread(), element2.getThread());
        } else if (columnIndex == getColumnIndex(tree, EclipseJobColumn.SCHEDULING_RULE)) {
            result = super.compare(treeViewer, element1.getSchedulingRule(), element2.getSchedulingRule());
        }

        if (sortDirection == SWT.DOWN) {
            result *= -1;
        }
        return result;
    }

    void reverseSortDirection() {
        sortDirection = (sortDirection == SWT.UP) ? SWT.DOWN : SWT.UP;
    }

    int getSortDirection() {
        return sortDirection;
    }

    int getColumnIndex() {
        return columnIndex;
    }

    private static int getColumnIndex(Tree tree, EclipseJobColumn column) {
        for (int i = 0; i < tree.getColumnCount(); i++) {
            if (tree.getColumn(i).getText().equals(column.label)) {
                return i;
            }
        }
        return -1;
    }
}
