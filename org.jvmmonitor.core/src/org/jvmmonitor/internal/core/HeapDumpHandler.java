/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.core;

import static org.jvmmonitor.core.IPreferenceConstants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.jvmmonitor.core.IHeapDumpHandler;
import org.jvmmonitor.core.JvmCoreException;
import org.jvmmonitor.tools.Tools;
import org.jvmmonitor.tools.ToolsException;
/**
 * The heap dump handler that contributes to the extension point
 * <tt>org.jvmmonitor.core.heapDumpHandler</tt>.
 */
public class HeapDumpHandler implements IHeapDumpHandler {

    /*
     * @see IHeapDumpHandler#dumpHeap(int, boolean)
     */
    @Override
    public String dumpHeap(int pid, boolean isLive) throws JvmCoreException {
        try {
            return Tools.getHeapHistogram(pid, isLive);
        } catch (ToolsException e) {
            // does nothing
            return "";
        }
    }

    /*
     * @see IHeapDumpHandler#getMaxClassesNumber()
     */
    @Override
    public int getMaxClassesNumber() {
        return InstanceScope.INSTANCE.getNode(PREFERENCES_ID).getInt(MAX_CLASSES_NUMBER, DEFAULT_MAX_CLASSES_NUMBER);
    }

}
