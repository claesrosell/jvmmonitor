/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.jvmmonitor.core.IEclipseJobElement;
import org.jvmmonitor.ui.Activator;
import org.jvmmonitor.ui.ISharedImages;

/**
 * The label provider for eclipse job viewer.
 */
class EclipseJobLabelProvider extends LabelProvider implements ITableLabelProvider {

    /** The columns taken into account for filter. */
    private static final EclipseJobColumn[] COLUMNS_TAKEN_INTO_ACCOUNT_FOR_FILTER = { EclipseJobColumn.JOB,
            EclipseJobColumn.STATE, EclipseJobColumn.THREAD, EclipseJobColumn.SCHEDULING_RULE };

    private final TreeViewer treeViewer;

    private Image runningJobObjImage;

    private Image notRunningJobObjImage;

    EclipseJobLabelProvider(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IEclipseJobElement) {
            return getColumnText((IEclipseJobElement) element, columnIndex);
        }
        return super.getText(element);
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == getColumnIndex(EclipseJobColumn.JOB) && element instanceof IEclipseJobElement) {
            if ("RUNNING".equals(((IEclipseJobElement)element).getState())) {
                return getRunningJobObjImage();
            }
            return getNotRunningJobObjImage();
        }
        return null;
    }

    @Override
    public String getText(Object obj) {
        // get the text for filtering
        if (obj instanceof IEclipseJobElement) {
            StringBuffer buffer = new StringBuffer();

            for (EclipseJobColumn column : COLUMNS_TAKEN_INTO_ACCOUNT_FOR_FILTER) {
                buffer.append(getColumnText((IEclipseJobElement) obj, getColumnIndex(column))).append(' ');
            }
            return buffer.toString();
        }
        return super.getText(obj);
    }

    private String getColumnText(IEclipseJobElement element, int columnIndex) {
        if (columnIndex == getColumnIndex(EclipseJobColumn.JOB)) {
            return element.getName();
        } else if (columnIndex == getColumnIndex(EclipseJobColumn.CLASS)) {
            return element.getClassName();
        } else if (columnIndex == getColumnIndex(EclipseJobColumn.STATE)) {
            return element.getState();
        } else if (columnIndex == getColumnIndex(EclipseJobColumn.PROGRESS_MONITOR_STATE)) {
            return element.isCanceled() ? "CANCELED": "";
        } else if (columnIndex == getColumnIndex(EclipseJobColumn.THREAD)) {
            return element.getThread();
        } else if (columnIndex == getColumnIndex(EclipseJobColumn.SCHEDULING_RULE)) {
            return element.getSchedulingRule();
        }
        return ""; //$NON-NLS-1$
    }

    private int getColumnIndex(EclipseJobColumn column) {
        Tree tree = treeViewer.getTree();
        for (int i = 0; i < tree.getColumnCount(); i++) {
            if (tree.getColumn(i).getText().equals(column.label)) {
                return i;
            }
        }
        return -1;
    }

    private Image getRunningJobObjImage() {
        if (runningJobObjImage == null || runningJobObjImage.isDisposed()) {
            runningJobObjImage = Activator.getImageDescriptor(ISharedImages.RUNNING_JOB_IMG_PATH)
                    .createImage();
        }
        return runningJobObjImage;
    }

    private Image getNotRunningJobObjImage() {
        if (notRunningJobObjImage == null || notRunningJobObjImage.isDisposed()) {
            notRunningJobObjImage = Activator.getImageDescriptor(ISharedImages.NOT_RUNNING_JOB_IMG_PATH)
                    .createImage();
        }
        return notRunningJobObjImage;
    }
}
