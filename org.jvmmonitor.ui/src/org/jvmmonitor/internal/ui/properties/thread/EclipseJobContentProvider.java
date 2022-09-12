/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * The eclipse job content provider.
 */
public class EclipseJobContentProvider implements ITreeContentProvider {

    private final TreeViewer eclipseJobViewer;

    /**
     * The constructor.
     *
     * @param eclipseJobViewer
     *            The eclipse job viewer
     */
    public EclipseJobContentProvider(TreeViewer eclipseJobViewer) {
        this.eclipseJobViewer = eclipseJobViewer;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Object input = eclipseJobViewer.getInput();
        if (input instanceof IEclipseJobInput) {
            return ((IEclipseJobInput) input).getEclipseJobElements();
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return false;
    }

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
    }
}
