/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jvmmonitor.core.IActiveJvm;
import org.jvmmonitor.core.IEclipseJobElement;
import org.jvmmonitor.core.IThreadElement;
import org.jvmmonitor.internal.ui.IHelpContextIds;
import org.jvmmonitor.internal.ui.RefreshJob;
import org.jvmmonitor.internal.ui.properties.AbstractJvmPropertySection;
import org.jvmmonitor.internal.ui.properties.memory.Messages;

/**
 * The thread section.
 */
public class ThreadsSection extends AbstractJvmPropertySection {

    private int defaultTabHeight;

    private ThreadsPage threadsPage;

    private EclipseJobsPage jobsPage;

    private CTabFolder tabFolder;

    /*
     * @see AbstractPropertySection#refresh()
     */
    @Override
    public void refresh() {
        if (!isSectionActivated) {
            return;
        }

        threadsPage.refresh();
        jobsPage.refresh();
    }

    /*
     * @see AbstractJvmPropertySection#createControls(Composite)
     */
    @Override
    protected void createControls(Composite parent) {
        tabFolder = getWidgetFactory().createTabFolder(parent,
                SWT.BOTTOM | SWT.FLAT);

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearStatusLine();
            }
        });

        threadsPage = new ThreadsPage(this, tabFolder, getActionBars());
        jobsPage = new EclipseJobsPage(this, tabFolder, getActionBars());

        defaultTabHeight = tabFolder.getTabHeight();
        tabFolder.setTabHeight(0);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.THREADS_PAGE);
    }

    /*
     * @see AbstractJvmPropertySection#setInput(IWorkbenchPart, ISelection,
     * IActiveJvm, IActiveJvm)
     */
    @Override
    protected void setInput(IWorkbenchPart part, ISelection selection,
            final IActiveJvm newJvm, IActiveJvm oldJvm) {
        updateTabHeight(newJvm);

        threadsPage.setInput(new IThreadInput() {
            @Override
            public IThreadElement[] getThreadListElements() {
                return newJvm.getMBeanServer().getThreadCache();
            }
        });

        jobsPage.setInput(new IEclipseJobInput() {
            @Override
            public IEclipseJobElement[] getEclipseJobElements() {
                return newJvm.getMBeanServer().getEclipseJobCache();
            }
        });
    }

    /*
     * @see AbstractJvmPropertySection#addToolBarActions(IToolBarManager)
     */
    @Override
    protected void addToolBarActions(IToolBarManager manager) {
        if (tabFolder.getSelectionIndex() == 0) {
            threadsPage.addToolBarActions(manager);
        } else {
            jobsPage.addToolBarActions(manager);
        }
    }

    /*
     * @see AbstractJvmPropertySection#removeToolBarActions(IToolBarManager)
     */
    @Override
    protected void removeToolBarActions(IToolBarManager manager) {
        if (tabFolder.getSelectionIndex() == 0) {
            threadsPage.removeToolBarActions(manager);
        } else {
            jobsPage.removeToolBarActions(manager);
        }
    }

    /*
     * @see AbstractJvmPropertySection#addLocalMenus(IMenuManager)
     */
    @Override
    protected void addLocalMenus(IMenuManager manager) {
        if (tabFolder.getSelectionIndex() == 0) {
            threadsPage.addLocalMenus(manager);
        } else {
            jobsPage.addLocalMenus(manager);
        }
    }

    /*
     * @see AbstractJvmPropertySection#removeLocalMenus(IMenuManager)
     */
    @Override
    protected void removeLocalMenus(IMenuManager manager) {
        if (tabFolder.getSelectionIndex() == 0) {
            threadsPage.removeLocalMenus(manager);
        } else {
            jobsPage.removeLocalMenus(manager);
        }
    }

    /*
     * @see AbstractJvmPropertySection#activateSection()
     */
    @Override
    protected void activateSection() {
        super.activateSection();
        threadsPage.updateLocalToolBar(tabFolder.getSelectionIndex() == 0);
        jobsPage.updateLocalToolBar(tabFolder.getSelectionIndex() == 1);
    }

    /*
     * @see AbstractJvmPropertySection#deactivateSection()
     */
    @Override
    protected void deactivateSection() {
        super.deactivateSection();
        threadsPage.deactivated();
        jobsPage.deactivated();
    }

    private void updateTabHeight(final IActiveJvm jvm) {
        new RefreshJob(NLS.bind(Messages.refreshMemorySectionJobLabel,
                jvm.getPid()), toString()) {
            private boolean isSupported;

            @Override
            protected void refreshModel(IProgressMonitor monitor) {
                isSupported = jvm.getSWTResourceMonitor().isSupported();
            }

            @Override
            protected void refreshUI() {
                int tabHeight;
                if (isSupported) {
                    tabHeight = defaultTabHeight;
                } else {
                    tabHeight = 0;
                    tabFolder.setSelection(0);
                }
                tabFolder.setTabHeight(tabHeight);
                tabFolder.layout();
            }
        }.schedule();
    }
}
