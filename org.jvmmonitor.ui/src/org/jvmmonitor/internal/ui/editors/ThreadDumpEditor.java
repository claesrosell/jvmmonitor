/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.jvmmonitor.core.IEclipseJobElement;
import org.jvmmonitor.core.IThreadElement;
import org.jvmmonitor.core.dump.ThreadDumpParser;
import org.jvmmonitor.internal.ui.IHelpContextIds;
import org.jvmmonitor.internal.ui.properties.thread.EclipseJobsPage;
import org.jvmmonitor.internal.ui.properties.thread.IEclipseJobInput;
import org.jvmmonitor.internal.ui.properties.thread.IThreadInput;
import org.jvmmonitor.internal.ui.properties.thread.ThreadsPage;
import org.jvmmonitor.ui.Activator;
import org.jvmmonitor.ui.ISharedImages;
import org.xml.sax.SAXException;

/**
 * The thread dump editor.
 */
public class ThreadDumpEditor extends AbstractDumpEditor {

    private ThreadsPage threadsPage;

    private EclipseJobsPage jobsPage;

    private List<IThreadElement> threadListElements;

    private List<IEclipseJobElement> eclipseJobElements;

    private Image threadImage;

    private Image jobImage;

    /**
     * The constructor.
     */
    public ThreadDumpEditor() {
        threadListElements = new ArrayList<>();
        eclipseJobElements = new ArrayList<>();
    }

    /*
     * @see AbstractDumpEditor#createClientPages()
     */
    @Override
    protected void createClientPages() {
        createThreadsPage();
        if (!eclipseJobElements.isEmpty()) {
            createJobsPage();
        }
        addListeners();

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(getContainer(), IHelpContextIds.THREADS_DUMP_EDITOR);
    }

    /*
     * @see EditorPart#init(IEditorSite, IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        setSite(site);
        setInput(input);

        setPartName(input.getName());

        if (input instanceof IFileEditorInput) {
            String filePath = ((IFileEditorInput) input).getFile()
                    .getRawLocation().toOSString();
            parseDumpFile(filePath);
        } else if (input instanceof FileStoreEditorInput) {
            String filePath = ((FileStoreEditorInput) input).getURI().getPath();
            parseDumpFile(filePath);
        }
    }

    /*
     * @see WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        threadsPage.setFocus();
    }

    /*
     * @see AbstractDumpEditor#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (threadImage != null) {
            threadImage.dispose();
        }
        if (jobImage != null) {
            jobImage.dispose();
        }
    }

    /**
     * Gets the threads page.
     *
     * @return The threads page
     */
    protected ThreadsPage getThreadsPage() {
        return threadsPage;
    }

    private void addListeners() {
        addPageChangedListener(new IPageChangedListener() {
            @Override
            public void pageChanged(PageChangedEvent event) {
                pageSelectionChanged();
            }
        });
    }

    private void pageSelectionChanged() {
        IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
        manager.setMessage(null);

        for (IContributionItem item : manager.getItems()) {
            if (item instanceof StatusLineContributionItem) {
                ((StatusLineContributionItem) item).setText(Util.ZERO_LENGTH_STRING);
            }
        }
    }

    /**
     * Creates the threads page.
     */
    private void createThreadsPage() {
        threadsPage = new ThreadsPage(getContainer(), getEditorSite().getActionBars());
        threadsPage.setInput(new IThreadInput() {
            @Override
            public IThreadElement[] getThreadListElements() {
                return threadListElements.toArray(new IThreadElement[0]);
            }
        });
        int page = addPage(threadsPage);
        setPageText(page, Messages.threadsTabLabel);
        setPageImage(page, getThreadImage());

        threadsPage.refresh();
    }

    private void createJobsPage() {
        jobsPage = new EclipseJobsPage(getContainer(), getEditorSite().getActionBars());
        jobsPage.setInput(new IEclipseJobInput() {

            @Override
            public IEclipseJobElement[] getEclipseJobElements() {
                return eclipseJobElements.toArray(new IEclipseJobElement[0]);
            }
        });

        int page = addPage(jobsPage);
        setPageText(page, Messages.jobsTabLabel);
        setPageImage(page, getJobImage());

        threadsPage.refresh();
    }

    /**
     * Gets the thread image.
     * 
     * @return The thread image
     */
    private Image getThreadImage() {
        if (threadImage == null || threadImage.isDisposed()) {
            threadImage = Activator.getImageDescriptor(
                    ISharedImages.THREAD_IMG_PATH).createImage();
        }
        return threadImage;
    }

    private Image getJobImage() {
        if (jobImage == null || jobImage.isDisposed()) {
            jobImage = Activator.getImageDescriptor(
                    ISharedImages.RUNNING_JOB_IMG_PATH).createImage();
        }
        return jobImage;
    }

    /**
     * Parses the dump file.
     * 
     * @param filePath
     *            The file path
     */
    private void parseDumpFile(final String filePath) {

        Job job = new Job(Messages.parseThreadDumpFileJobLabel) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                final ThreadDumpParser parser = new ThreadDumpParser(new File(
                        filePath), threadListElements, eclipseJobElements, monitor);

                try {
                    parser.parse();
                } catch (ParserConfigurationException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Could not load thread dump file.", e); //$NON-NLS-1$
                } catch (SAXException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Could not load thread dump file.", e); //$NON-NLS-1$
                } catch (FileNotFoundException e) {
                    // file might have been removed
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            "Could not load thread dump file.", e); //$NON-NLS-1$
                }

                setProfileInfo(parser.getProfileInfo());
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (threadsPage != null) {
                            threadsPage.refresh();
                        }
                    }
                });

                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}
