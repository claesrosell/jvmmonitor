/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import static org.jvmmonitor.internal.ui.IConstants.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.internal.ui.RefreshJob;
import org.jvmmonitor.internal.ui.actions.RefreshAction;
import org.jvmmonitor.internal.ui.actions.ToggleOrientationAction;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;
import org.jvmmonitor.internal.ui.properties.AbstractSashForm;
import org.jvmmonitor.internal.ui.properties.StackTraceViewer;
import org.jvmmonitor.ui.Activator;
import org.jvmmonitor.ui.ISharedImages;

/**
 * The page to show threads and relevant stack traces.
 */
public class ThreadsPage extends AbstractSashForm {

    private static final int[] SASH_WEIGHTS = new int[] { 55, 45 };

    private static final String LAYOUT_MENU_ID = "layout"; //$NON-NLS-1$

    private TreeViewer viewer;

    private StackTraceViewer stackTraceViewer;

    private ThreadFilteredTree filteredTree;

    private AbstractJvmPropertySection section;

    private DumpThreadsAction dumpThreadsAction;

    private RefreshAction refreshAction;

    private MenuManager layoutMenu;

    private final IActionBars actionBars;

    private Image tabImage;

    /**
     * The constructor.
     *
     * @param section
     *            The property section
     * @param tabFolder
     *            The tab folder
     * @param actionBars
     *            The action bars
     */
    public ThreadsPage(AbstractJvmPropertySection section, final CTabFolder tabFolder,
            IActionBars actionBars) {
        super(tabFolder, SASH_WEIGHTS);
        this.section = section;
        this.actionBars = actionBars;

        createSashFormControls(this, actionBars);
        setWeights(initialSashWeights);

        createActions();

        final CTabItem tabItem = section.getWidgetFactory().createTabItem(
                tabFolder, SWT.NONE);
        tabItem.setText(Messages.threadsLabel);
        tabItem.setImage(getTabImage());
        tabItem.setControl(this);

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean tabSelected = tabFolder.getSelection().equals(tabItem);
                updateLocalToolBar(tabSelected);
                updateLocalMenus(tabSelected);
            }
        });
    }

    /**
     * The constructor.
     *
     * @param parent
     *                   The parent composite
     * @param actionBars
     *                   The action bars
     */
    public ThreadsPage(Composite parent, IActionBars actionBars) {
        super(parent, SASH_WEIGHTS);
        this.actionBars = actionBars;

        createSashFormControls(this, actionBars);
        setWeights(initialSashWeights);

        createActions();
    }

    @Override
    protected void createSashFormControls(SashForm sashForm, IActionBars actionBars) {
        filteredTree = new ThreadFilteredTree(sashForm, actionBars);
        viewer = filteredTree.getViewer();
        viewer.setContentProvider(new ThreadContentProvider(viewer));
        viewer.setLabelProvider(new ThreadLabelProvider(viewer));
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection.isEmpty()) {
                    selection = null;
                }
                stackTraceViewer.setInput(selection);
                filteredTree.updateStatusLine((IStructuredSelection) event.getSelection());
            }
        });
        stackTraceViewer = new StackTraceViewer(sashForm, actionBars);
    }

    /**
     * Refreshes the appearance.
     */
    public void refresh() {
        if (section == null || !isVisible()) {
            return;
        }

        new RefreshJob(NLS.bind(Messages.refreshThreadsPageJobLabel, section.getJvm().getPid()), toString()) {

            @Override
            protected void refreshModel(IProgressMonitor monitor) {
                IActiveJvm jvm = section.getJvm();
                if (jvm != null && jvm.isConnected() && !section.isRefreshSuspended()) {
                    try {
                        jvm.getMBeanServer().refreshThreadCache();
                    } catch (JvmCoreException e) {
                        Activator.log(null, e);
                    }
                }
            }

            @Override
            protected void refreshUI() {
                IActiveJvm jvm = section.getJvm();
                boolean isConnected = jvm != null && jvm.isConnected();
                dumpThreadsAction.setEnabled(!section.hasErrorMessage());
                refreshAction.setEnabled(isConnected);

                if (!isDisposed()) {
                    refreshBackground(isConnected);
                    doRefreshUI();
                }
            }
        }.schedule();
    }

    private void doRefreshUI() {
        if (!viewer.getControl().isDisposed()) {
            viewer.refresh();

            // select the first item if no item is selected
            if (viewer.getSelection().isEmpty()) {
                TreeItem[] items = viewer.getTree().getItems();
                if (items != null && items.length > 0) {
                    viewer.getTree().select(items[0]);
                    stackTraceViewer.setInput(viewer.getSelection());
                } else {
                    stackTraceViewer.setInput(null);
                }
            }
        }
        if (!stackTraceViewer.getControl().isDisposed()) {
            stackTraceViewer.refresh();
            filteredTree.updateStatusLine((IStructuredSelection) viewer.getSelection());
        }
    }

    /**
     * Sets the input.
     *
     * @param input
     *            The input
     */
    public void setInput(Object input) {
        viewer.setInput(input);
    }

    void deactivated() {
        Job.getJobManager().cancel(toString());
    }

    private void refreshBackground(boolean isConnected) {
        IActiveJvm jvm = section.getJvm();
        boolean isRemote = jvm != null && jvm.isRemote();
        section.refreshBackground(getChildren(), isConnected && !isRemote);
    }

    void updateLocalToolBar(boolean activated) {
        IToolBarManager manager = actionBars.getToolBarManager();
        if (activated) {
            addToolBarActions(manager);
        } else {
            removeToolBarActions(manager);
        }

        manager.update(false);
        actionBars.updateActionBars();
    }

    private void updateLocalMenus(boolean activated) {
        IMenuManager manager = actionBars.getMenuManager();
        if (activated) {
            addLocalMenus(manager);
        } else {
            removeLocalMenus(manager);
        }
    }

    void addToolBarActions(IToolBarManager manager) {
        if (manager.find(SEPARATOR_ID) == null) {
            manager.add(new Separator(SEPARATOR_ID));
        }
        if (manager.find(refreshAction.getId()) == null) {
            manager.insertAfter(SEPARATOR_ID, refreshAction);
        }
        if (manager.find(dumpThreadsAction.getId()) == null) {
            manager.insertAfter(SEPARATOR_ID, dumpThreadsAction);
        }
    }

    void removeToolBarActions(IToolBarManager manager) {
        manager.remove(SEPARATOR_ID);
        manager.remove(refreshAction.getId());
        manager.remove(dumpThreadsAction.getId());
    }

    void addLocalMenus(IMenuManager manager) {
        if (manager.find(layoutMenu.getId()) == null) {
            manager.add(layoutMenu);
            for (ToggleOrientationAction action : getOrientationActions()) {
                if (layoutMenu.find(action.getId()) == null) {
                    layoutMenu.add(action);
                }
            }
        }
    }

    void removeLocalMenus(IMenuManager manager) {
        manager.remove(layoutMenu);
    }

    private void createActions() {
        refreshAction = new RefreshAction(section);
        dumpThreadsAction = new DumpThreadsAction(section);
        layoutMenu = new MenuManager(Messages.layoutLabel, LAYOUT_MENU_ID);
    }

    private Image getTabImage() {
        if (tabImage == null || tabImage.isDisposed()) {
            tabImage = Activator.getImageDescriptor(
                    ISharedImages.THREAD_IMG_PATH).createImage();
        }
        return tabImage;
    }
}
