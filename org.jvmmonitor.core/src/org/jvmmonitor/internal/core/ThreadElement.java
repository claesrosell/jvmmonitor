/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import java.lang.Thread.State;
import java.lang.management.ThreadInfo;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.jvmmonitor.core.IThreadElement;

/**
 * The thread element.
 */
public class ThreadElement implements IThreadElement {

    /** The thread name. */
    private String threadName;

    /** The thread state. */
    private State threadState;

    /** The blocked time. */
    private long blockedTime;

    /** The blocked count. */
    private long blockedCount;

    /** The waited time. */
    private long waitedTime;

    /** The waited count. */
    private long waitedCount;

    /** The lock that this thread is waiting for. */
    private String lockName;

    /** The thread that owns the lock this thread is waiting for. */
    private String lockOwnerName;

    /** The stack trace elements. */
    private StackTraceElement[] stackTraceElements;

    /** The state indicating if thread is suspended. */
    private boolean isSuspended;

    /** The state indicating if the thread is in deadlock. */
    private boolean isDeadlocked;

    /** The CPU usage in percentage. */
    private double cpuUsage;

    /** The scheduling rule {@link ISchedulingRule} that this thread is waiting for. */
    private String schedulingRuleName;

    /** The thread that holds the scheduling rule {@link ISchedulingRule} this thread is waiting for. */
    private String schedulingRuleOwnerName;

    /** The scheduling rules {@link ISchedulingRule} that this thread holds. */
    private String[] heldSchedulingRuleNames;

    /**
     * The constructor.
     * 
     * @param threadInfo
     *            The thread info
     * @param isDeadlocked
     *            True if the thread is in deadlock
     * @param cpuUsage
     *            The CPU usage in percentage
     */
    public ThreadElement(ThreadInfo threadInfo, boolean isDeadlocked,
            double cpuUsage) {
        this.isDeadlocked = isDeadlocked;
        this.cpuUsage = cpuUsage;
        setThreadInfo(threadInfo);
    }

    /**
     * The constructor.
     * 
     * @param threadName
     *            The thread name
     * @param threadState
     *            The thread state
     * @param blockedTime
     *            The blocked time
     * @param blockedCount
     *            The blocked count
     * @param waitedTime
     *            The waited time
     * @param waitedCount
     *            The waited count
     * @param lockName
     *            The lock name
     * @param lockOwnerName
     *            The lock owner name
     * @param isSuspended
     *            True if the thread is suspended
     * @param isDeadlocked
     *            True if the thread is in deadlock
     * @param cpuUsage
     *            The CPU usage
     * @param schedulingRuleName
     *             The scheduling rule {@link ISchedulingRule} that this thread is waiting for
     * @param schedulingRuleOwnerName
     *            The name of thread that holds the scheduling rule {@link ISchedulingRule} this thread is waiting for
     * @param holdingSchedulingRules
     *             The scheduling rule {@link ISchedulingRule} that this thread holds
     */
    public ThreadElement(String threadName, State threadState, long blockedTime, long blockedCount, long waitedTime,
            long waitedCount, String lockName, String lockOwnerName, boolean isSuspended, boolean isDeadlocked,
            double cpuUsage, String schedulingRuleName, String schedulingRuleOwnerName, String[] holdingSchedulingRules) {
        this.threadName = threadName;
        this.threadState = threadState;
        this.blockedTime = blockedTime;
        this.blockedCount = blockedCount;
        this.waitedTime = waitedTime;
        this.waitedCount = waitedCount;
        this.lockName = lockName;
        this.lockOwnerName = lockOwnerName;
        this.isSuspended = isSuspended;
        this.isDeadlocked = isDeadlocked;
        this.cpuUsage = cpuUsage;
        this.schedulingRuleName = schedulingRuleName;
        this.schedulingRuleOwnerName = schedulingRuleOwnerName;
        this.heldSchedulingRuleNames = holdingSchedulingRules;
    }

    /*
     * @see IThreadListElement#getThreadName()
     */
    @Override
    public String getThreadName() {
        return threadName;
    }

    /*
     * @see IThreadListElement#getThreadState()
     */
    @Override
    public State getThreadState() {
        return threadState;
    }

    /*
     * @see IThreadListElement#getBlockedTime()
     */
    @Override
    public long getBlockedTime() {
        return blockedTime;
    }

    /*
     * @see IThreadListElement#getBlockedCount()
     */
    @Override
    public long getBlockedCount() {
        return blockedCount;
    }

    /*
     * @see IThreadListElement#getWaitedTime()
     */
    @Override
    public long getWaitedTime() {
        return waitedTime;
    }

    /*
     * @see IThreadListElement#getWaitedCount()
     */
    @Override
    public long getWaitedCount() {
        return waitedCount;
    }

    /*
     * @see IThreadListElement#getLockName()
     */
    @Override
    public String getLockName() {
        if (schedulingRuleName != null) {
            return schedulingRuleName + " (ISchedulingRule)"; //$NON-NLS-1$
        }
        return lockName == null ? "" : lockName; //$NON-NLS-1$
    }

    /*
     * @see IThreadListElement#getLockOwnerName()
     */
    @Override
    public String getLockOwnerName() {
        if (lockOwnerName != null) {
            return lockOwnerName;
        }
        return schedulingRuleOwnerName == null ? "" : schedulingRuleOwnerName; //$NON-NLS-1$
    }

    /*
     * @see IThreadListElement#getStackTraceElements()
     */
    @Override
    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
    }

    /*
     * @see IThreadListElement#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    /*
     * @see IThreadListElement#isDeadlockede()
     */
    @Override
    public boolean isDeadlocked() {
        return isDeadlocked;
    }

    /*
     * @see IThreadListElement#getCpuUsage()
     */
    @Override
    public double getCpuUsage() {
        return cpuUsage;
    }

    @Override
    public String getSchedulingRules() {
        return toStringSeparatedByComma(heldSchedulingRuleNames);
    }

    /**
     * Sets the thread info.
     * 
     * @param threadInfo
     *            The thread info
     */
    public void setThreadInfo(ThreadInfo threadInfo) {
        threadName = threadInfo.getThreadName();
        threadState = threadInfo.getThreadState();
        blockedTime = threadInfo.getBlockedTime();
        blockedCount = threadInfo.getBlockedCount();
        waitedTime = threadInfo.getWaitedTime();
        waitedCount = threadInfo.getWaitedCount();
        lockName = threadInfo.getLockName();
        lockOwnerName = threadInfo.getLockOwnerName();
        stackTraceElements = threadInfo.getStackTrace();
        isSuspended = threadInfo.isSuspended();
    }

    /**
     * Sets the stack trace elements.
     * 
     * @param stackTraceElements
     *            The stack trace elements
     */
    public void setStackTrace(StackTraceElement[] stackTraceElements) {
        this.stackTraceElements = stackTraceElements;
    }

    /**
     * Sets the state indicating if thread is deadlocked.
     * 
     * @param deadlocked
     *            true if thread is deadlocked
     */
    public void setDeadlocked(boolean deadlocked) {
        this.isDeadlocked = deadlocked;
    }

    /**
     * Sets the CPU usage in percentage.
     * 
     * @param cpuUsage
     *            The CPU usage in percentage
     */
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * Sets the name of scheduling rule {@link ISchedulingRule} that this thread is
     * waiting for.
     * 
     * @param schedulingRuleName The name of scheduling rule {@link ISchedulingRule}
     *                           that this thread is waiting for
     */
    public void setSchedulingRuleName(String schedulingRuleName) {
        this.schedulingRuleName = schedulingRuleName;
    }

    /**
     * Sets the name of thread that holds the scheduling rule
     * {@link ISchedulingRule} this thread is waiting for.
     * 
     * @param schedulingRuleOwnerName The name of thread that holds the scheduling
     *                                rule {@link ISchedulingRule} this thread is
     *                                waiting for
     */
    public void setSchedulingRuleOwnerName(String schedulingRuleOwnerName) {
        this.schedulingRuleOwnerName = schedulingRuleOwnerName;
    }

    /**
     * Sets the scheduling rules {@link ISchedulingRule} that this thread holds.
     * 
     * @param heldSchedulingRuleNames The scheduling rules {@link ISchedulingRule}
     *                                held by this thread
     */
    public void setHeldSchedulingRules(String[] heldSchedulingRuleNames) {
        this.heldSchedulingRuleNames = heldSchedulingRuleNames;
    }

    /**
     * Gets the name of scheduling rule {@link ISchedulingRule} that this thread is
     * waiting for.
     * 
     * @return The name of scheduling rule {@link ISchedulingRule} that this thread
     *         is waiting for
     */
    public String getSchedulingRuleName() {
        return schedulingRuleName;
    }

    /**
     * Dumps the thread data to given string buffer.
     * 
     * @param buffer
     *            The string buffer
     */
    public void dump(StringBuffer buffer) {
        buffer.append("\t<thread"); //$NON-NLS-1$
        buffer.append(" name=\"").append(threadName).append('"'); //$NON-NLS-1$ 
        buffer.append(" state=\"").append(threadState).append('"'); //$NON-NLS-1$ 
        buffer.append(" blockedTime=\"").append(blockedTime).append('"'); //$NON-NLS-1$ 
        buffer.append(" blockedCount=\"").append(blockedCount).append('"'); //$NON-NLS-1$ 
        buffer.append(" waitedTime=\"").append(waitedTime).append('"'); //$NON-NLS-1$ 
        buffer.append(" waitedCount=\"").append(waitedCount).append('"'); //$NON-NLS-1$ 
        buffer.append(" lock=\"").append(getLockName()).append('"'); //$NON-NLS-1$
        buffer.append(" lockOwner=\"").append(getLockOwnerName()).append('"'); //$NON-NLS-1$
        buffer.append(" suspended=\"").append(isSuspended).append('"'); //$NON-NLS-1$
        buffer.append(" deadlocked=\"").append(isDeadlocked).append('"'); //$NON-NLS-1$
        buffer.append(" cpuUsage=\"").append(cpuUsage).append('"'); //$NON-NLS-1$
        if (schedulingRuleName != null) {
            buffer.append(" schedulingRuleName=\"").append(schedulingRuleName).append('"'); //$NON-NLS-1$
        }
        if (schedulingRuleOwnerName != null) {
            buffer.append(" schedulingRuleOwnerName=\"").append(schedulingRuleOwnerName).append('"'); //$NON-NLS-1$
        }
        if (heldSchedulingRuleNames != null) {
            buffer.append(" heldSchedulingRuleNames=\"").append(toStringSeparatedByComma(heldSchedulingRuleNames)) //$NON-NLS-1$
                    .append('"');
        }
        buffer.append(">\n"); //$NON-NLS-1$

        for (StackTraceElement element : stackTraceElements) {
            buffer.append("\t\t<frame "); //$NON-NLS-1$
            buffer.append("class=\"").append(element.getClassName()).append( //$NON-NLS-1$
                    "\" "); //$NON-NLS-1$
            String method = element.getMethodName().replaceAll("<", "&lt;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("method=\"").append(method).append("\" "); //$NON-NLS-1$ //$NON-NLS-2$
            buffer.append("file=\"").append(element.getFileName()) //$NON-NLS-1$
                    .append("\" "); //$NON-NLS-1$
            buffer.append("line=\"").append(element.getLineNumber()).append( //$NON-NLS-1$
                    "\"/>\n"); //$NON-NLS-1$
        }
        buffer.append("\t</thread>\n"); //$NON-NLS-1$
    }

    /*
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(threadName).append('\t');
        buffer.append(getThreadState().toString()).append('\t');
        buffer.append(getCpuUsage()).append('\t');
        buffer.append(getBlockedTime()).append('\t');
        buffer.append(getBlockedCount()).append('\t');
        buffer.append(getWaitedTime()).append('\t');
        buffer.append(getWaitedCount()).append('\t');
        buffer.append(getLockName()).append('\t');
        buffer.append(getLockOwnerName() != null ? getLockOwnerName() : ""); //$NON-NLS-1$
        return buffer.toString();
    }

    private static String toStringSeparatedByComma(String[] strings) {
        if (strings == null) {
            return "";//$NON-NLS-1$
        }

        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (builder.length() > 0) {
                builder.append(',').append(' ');
            }
            builder.append(string);
        }
        return builder.toString();
    }
}
