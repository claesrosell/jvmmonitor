/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import org.jvmmonitor.core.IEclipseJobElement;

/**
 * The Eclipse job element.
 */
public class EclipseJobElement implements IEclipseJobElement {

    private final String name;

    private String className;

    private String state;

    private boolean isCanceled;

    private String thread;

    private String schedulingRule;

    EclipseJobElement(String name, String className, String state, boolean isCanceled, String thread,
            String schedulingRule) {
        this.name = name;
        this.className = className;
        this.state = state;
        this.isCanceled = isCanceled;
        this.thread = thread;
        this.schedulingRule = schedulingRule;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getThread() {
        return thread;
    }

    @Override
    public String getSchedulingRule() {
        return schedulingRule;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    void setClassName(String className) {
        this.className = className;
    }

    void setState(String state) {
        this.state = state;
    }

    void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    void setThread(String thread) {
        this.thread = thread;
    }

    void setSchedulingRule(String schedulingRule) {
        this.schedulingRule = schedulingRule;
    }

    /**
     * Dumps the eclipse job data to given string buffer.
     * 
     * @param buffer The string buffer
     */
    public void dump(StringBuffer buffer) {
        buffer.append("\t<job"); //$NON-NLS-1$
        buffer.append(" name=\"").append(name).append('"'); //$NON-NLS-1$
        buffer.append(" className=\"").append(className).append('"'); //$NON-NLS-1$
        buffer.append(" state=\"").append(state).append('"'); //$NON-NLS-1$
        buffer.append(" canceled=\"").append(isCanceled).append('"'); //$NON-NLS-1$
        buffer.append(" thread=\"").append(thread).append('"'); //$NON-NLS-1$
        buffer.append(" schedulingRule=\"").append(schedulingRule).append('"'); //$NON-NLS-1$
        buffer.append(">\n"); //$NON-NLS-1$
        buffer.append("\t</job>\n"); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(name).append('\t');
        buffer.append(className).append('\t');
        buffer.append(state).append('\t');
        String cancellationState = isCanceled ? "CANCELED" : ""; //$NON-NLS-1$  //$NON-NLS-2$
        buffer.append(cancellationState).append('\t');
        buffer.append(thread).append('\t');
        buffer.append(schedulingRule);
        return buffer.toString();
    }
}
