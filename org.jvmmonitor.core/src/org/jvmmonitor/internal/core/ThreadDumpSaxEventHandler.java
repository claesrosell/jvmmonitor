/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.jvmmonitor.core.IEclipseJobElement;
import org.jvmmonitor.core.IThreadElement;
import org.jvmmonitor.core.dump.IProfileInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler for thread dump.
 */
public class ThreadDumpSaxEventHandler extends DefaultHandler {

    /** The progress monitor */
    private IProgressMonitor monitor;

    /** The thread list elements. */
    private List<IThreadElement> threadListElements;

    /** The eclipse job elements. */
    private List<IEclipseJobElement> eclipseJobElements;

    /** The currently parsed thread. */
    private ThreadElement currentlyParsedThread;

    /** The currently parsed job. */
    private EclipseJobElement currentlyParsedJob;
    
    /** The stack traces in currently parsed thread. */
    private List<StackTraceElement> stackTraceElements;

    /** The profile info. */
    private IProfileInfo info;

    /**
     * The constructor.
     * 
     * @param threadListElements
     *            The thread list elements
     * @param eclipseJobElements
     *            The eclipse job elements
     * @param monitor
     *            The progress monitor
     */
    public ThreadDumpSaxEventHandler(List<IThreadElement> threadListElements, List<IEclipseJobElement> eclipseJobElements,
            IProgressMonitor monitor) {
        this.monitor = monitor;
        this.threadListElements = threadListElements;
        this.eclipseJobElements = eclipseJobElements;
    }

    /*
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        // thread-profile
        if ("thread-profile".equals(name)) { //$NON-NLS-1$
            String date = attributes.getValue("date"); //$NON-NLS-1$
            String runtime = attributes.getValue("runtime"); //$NON-NLS-1$
            String mainClass = attributes.getValue("mainClass"); //$NON-NLS-1$
            String arguments = attributes.getValue("arguments"); //$NON-NLS-1$
            String comments = attributes.getValue("comments"); //$NON-NLS-1$
            info = new ProfileInfo(date, runtime, mainClass, arguments,
                    comments);
        }

        // thread
        if ("thread".equals(name)) { //$NON-NLS-1$
            String threadName = attributes.getValue("name"); //$NON-NLS-1$
            String threadState = attributes.getValue("state"); //$NON-NLS-1$
            String blockedTime = attributes.getValue("blockedTime"); //$NON-NLS-1$
            String blockedCount = attributes.getValue("blockedCount"); //$NON-NLS-1$
            String waitedTime = attributes.getValue("waitedTime"); //$NON-NLS-1$
            String waitedCount = attributes.getValue("waitedCount"); //$NON-NLS-1$
            String lockName = attributes.getValue("lock"); //$NON-NLS-1$
            String lockOwnerName = attributes.getValue("lockOwner"); //$NON-NLS-1$
            String isSuspended = attributes.getValue("suspended"); //$NON-NLS-1$
            String isDeadlocked = attributes.getValue("deadlocked"); //$NON-NLS-1$
            String cpuUsage = attributes.getValue("cpuUsage"); //$NON-NLS-1$
            String schedulingRuleName = attributes.getValue("schedulingRuleName"); //$NON-NLS-1$
            String schedulingRuleOwnerName = attributes.getValue("schedulingRuleOwnerName"); //$NON-NLS-1$
            String heldSchedulingRuleNames = attributes.getValue("heldSchedulingRuleNames"); //$NON-NLS-1$
            currentlyParsedThread = new ThreadElement(threadName, State.valueOf(threadState),
                    Long.parseLong(blockedTime), Long.parseLong(blockedCount), Long.parseLong(waitedTime),
                    Long.parseLong(waitedCount), lockName, lockOwnerName, Boolean.parseBoolean(isSuspended),
                    Boolean.parseBoolean(isDeadlocked), Double.parseDouble(cpuUsage), schedulingRuleName,
                    schedulingRuleOwnerName, parseStringSeparatedByComma(heldSchedulingRuleNames));
            stackTraceElements = new ArrayList<StackTraceElement>();
            return;
        }

        // job
        if ("job".equals(name)) { //$NON-NLS-1$
            String jobName = attributes.getValue("name"); //$NON-NLS-1$
            String className = attributes.getValue("className"); //$NON-NLS-1$
            String jobState = attributes.getValue("state"); //$NON-NLS-1$
            String isCanceled = attributes.getValue("canceled"); //$NON-NLS-1$
            String thread = attributes.getValue("thread"); //$NON-NLS-1$
            String schedulingRule = attributes.getValue("schedulingRule"); //$NON-NLS-1$
            currentlyParsedJob = new EclipseJobElement(jobName, className, jobState, Boolean.parseBoolean(isCanceled),
                    thread, schedulingRule);
            return;
        }

        // frame
        if ("frame".equals(name)) { //$NON-NLS-1$
            String className = attributes.getValue("class"); //$NON-NLS-1$
            String methodName = attributes.getValue("method"); //$NON-NLS-1$
            String fileName = attributes.getValue("file"); //$NON-NLS-1$
            String lineNumber = attributes.getValue("line"); //$NON-NLS-1$
            StackTraceElement element = new StackTraceElement(className,
                    methodName, fileName.equals("null") ? null : fileName,//$NON-NLS-1$
                    Integer.parseInt(lineNumber));
            stackTraceElements.add(element);
        }
    }

    private static String[] parseStringSeparatedByComma(String stringSeparatedByComma) {
        if (stringSeparatedByComma == null) {
            return new String[0];
        }
        return stringSeparatedByComma.split(", "); //$NON-NLS-1$
    }

    /*
     * @see DefaultHandler#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {

        // thread
        if ("thread".equals(name)) { //$NON-NLS-1$
            currentlyParsedThread.setStackTrace(stackTraceElements
                    .toArray(new StackTraceElement[0]));
            threadListElements.add(currentlyParsedThread);
        } else if ("job".equals(name)) { //$NON-NLS-1$
            eclipseJobElements.add(currentlyParsedJob);
        } 
    }

    /**
     * Gets the profile info.
     * 
     * @return The profile info
     */
    public IProfileInfo getProfileInfo() {
        return info;
    }
}
