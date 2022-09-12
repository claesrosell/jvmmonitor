/*******************************************************************************
 * Copyright (c) 2011 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.memory;

import static org.jvmmonitor.internal.ui.IConstants.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.internal.ui.RefreshJob;
import org.jvmmonitor.internal.ui.actions.PreferencesAction;
import org.jvmmonitor.internal.ui.actions.RefreshAction;
import org.jvmmonitor.internal.ui.actions.ToggleOrientationAction;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;
import org.jvmmonitor.internal.ui.properties.AbstractSashForm;
import org.jvmmonitor.internal.ui.properties.StackTraceViewer;
import org.jvmmonitor.ui.Activator;

/**
 * The SWT resource page.
 */
public class SWTResourcesPage extends AbstractSashForm {

    private static final int[] SASH_WEIGHTS = new int[] { 45, 55 };

    private static final String LAYOUT_MENU_ID = "layout"; //$NON-NLS-1$

    private SWTResourceFilteredTree resourceFilteredTree;

    private StackTraceViewer stackTraceViewer;

    private final AbstractJvmPropertySection section;

    private ClearSWTResourceAction clearSWTResourceAction;

    private RefreshAction refreshAction;

    private MenuManager layoutMenu;

    private final IActionBars actionBars;

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
    public SWTResourcesPage(AbstractJvmPropertySection section, final CTabFolder tabFolder,
            IActionBars actionBars) {
        super(tabFolder, SASH_WEIGHTS);
        this.section = section;
        this.actionBars = actionBars;

        createSashFormControls(this, actionBars);
        setWeights(initialSashWeights);

        createActions();

        final CTabItem tabItem = section.getWidgetFactory().createTabItem(
                tabFolder, SWT.NONE);
        tabItem.setText(Messages.swtResourcesLabel);
        tabItem.setControl(this);

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean tabSelected = tabFolder.getSelection().equals(tabItem);
                refreshBackground();
                updateLocalToolBar(tabSelected);
                updateLocalMenus(tabSelected);
                if (!tabSelected) {
                    resourceFilteredTree.updateStatusLine(null);
                }
            }
        });
    }

    @Override
    protected void createSashFormControls(SashForm sashForm, IActionBars actionBars) {
        resourceFilteredTree = new SWTResourceFilteredTree(sashForm, actionBars);
        TreeViewer resourceViewer = resourceFilteredTree.getViewer();
        resourceViewer.setContentProvider(new SWTResourceContentProvider(
                resourceViewer));
        resourceViewer.setLabelProvider(new SWTResourceLabelProvider());
        resourceViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        ISelection selection = event.getSelection();
                        if (selection.isEmpty()) {
                            selection = null;
                        }
                        stackTraceViewer.setInput(selection);
                    }
                });

        stackTraceViewer = new StackTraceViewer(sashForm, actionBars);
    }

    /**
     * Refreshes the appearance.
     * 
     * @param force
     *            <tt>true</tt> to force refresh
     */
    protected void refresh(final boolean force) {
        final boolean isVisible = isVisible();

        new RefreshJob(NLS.bind(Messages.refreshMemorySectionJobLabel, section.getJvm().getPid()), toString()) {
            @Override
            protected void refreshModel(IProgressMonitor monitor) {
                try {
                    IActiveJvm jvm = section.getJvm();
                    if (isVisible && jvm != null && jvm.isConnected()
                            && (!section.isRefreshSuspended() || force)
                            && jvm.getSWTResourceMonitor().isSupported()) {
                        jvm.getSWTResourceMonitor().refreshResourcesCache();
                    }
                } catch (JvmCoreException e) {
                    Activator.log(Messages.refreshHeapDataFailedMsg, e);
                }
            }

            @Override
            protected void refreshUI() {
                IActiveJvm jvm = section.getJvm();
                boolean isConnected = jvm != null && jvm.isConnected();
                refreshAction.setEnabled(isConnected);
                clearSWTResourceAction.setEnabled(isConnected);

                if (!isDisposed()) {
                    refreshBackground();
                    if (!force && section.isRefreshSuspended() || !isVisible) {
                        return;
                    }

                    doRefreshUI();
                }
            }
        }.schedule();
    }

    private void doRefreshUI() {
        TreeViewer resourceViewer = resourceFilteredTree.getViewer();
        if (!resourceViewer.getControl().isDisposed()) {
            resourceViewer.refresh();
            IActiveJvm jvm = section.getJvm();
            if (jvm != null) {
                resourceFilteredTree.updateStatusLine(jvm.getSWTResourceMonitor().getResources());
            }

            // select the first item if no item is selected
            if (resourceViewer.getSelection().isEmpty()) {
                TreeItem[] items = resourceViewer.getTree().getItems();
                if (items != null && items.length > 0) {
                    resourceViewer.getTree().select(items[0]);
                    stackTraceViewer.setInput(resourceViewer.getSelection());
                } else {
                    stackTraceViewer.setInput(null);
                }
            }
        }
        if (!stackTraceViewer.getControl().isDisposed()) {
            stackTraceViewer.refresh();
        }
    }

    /**
     * Sets the input.
     *
     * @param input
     *            The input
     */
    public void setInput(Object input) {
        if (!section.isRefreshSuspended()) {
            resourceFilteredTree.getViewer().setInput(input);
        }
    }

    void deactivated() {
        Job.getJobManager().cancel(toString());
    }

    void refreshBackground() {
        IActiveJvm jvm = section.getJvm();
        boolean isConnected = jvm != null && jvm.isConnected();
        section.refreshBackground(getChildren(), isConnected);
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
        if (manager.find(clearSWTResourceAction.getId()) == null) {
            manager.insertAfter(SEPARATOR_ID, clearSWTResourceAction);
        }
    }

    void removeToolBarActions(IToolBarManager manager) {
        manager.remove(SEPARATOR_ID);
        manager.remove(refreshAction.getId());
        manager.remove(clearSWTResourceAction.getId());
    }

    void addLocalMenus(IMenuManager manager) {
        if (manager.find(layoutMenu.getId()) == null) {
            if (manager.find(PreferencesAction.class.getName()) != null) {
                manager.insertBefore(PreferencesAction.class.getName(),
                        layoutMenu);
            } else {
                manager.add(layoutMenu);
            }

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
        clearSWTResourceAction = new ClearSWTResourceAction(this, section);
        layoutMenu = new MenuManager(Messages.layoutLabel, LAYOUT_MENU_ID);
    }
}
