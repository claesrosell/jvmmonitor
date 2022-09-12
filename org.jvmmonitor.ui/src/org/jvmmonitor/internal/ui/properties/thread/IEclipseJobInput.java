/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import org.jvmmonitor.core.IEclipseJobElement;

/**
 * The eclipse job input.
 */
public interface IEclipseJobInput {

    /**
     * Gets the eclipse job elements.
     *
     * @return The eclipse job elements
     */
    IEclipseJobElement[] getEclipseJobElements();
}
