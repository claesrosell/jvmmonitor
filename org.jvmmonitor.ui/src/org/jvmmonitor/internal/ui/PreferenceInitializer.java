/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui;

import static org.jvmmonitor.core.IPreferenceConstants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * The preference initializer.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * @see AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,PREFERENCES_ID);
        store.setDefault(SEARCH_JVM_PERIOD, DEFAULT_SEARCH_JVM_PERIOD);
        store.setDefault(UPDATE_PERIOD, DEFAULT_UPDATE_PERIOD);
        store.setDefault(LEGEND_VISIBILITY, DEFAULT_LEGEND_VISIBILITY);
        store.setDefault(WIDE_SCOPE_THREAD_FILTER, DEFAULT_WIDE_SCOPE_THREAD_FILTER);
        store.setDefault(MAX_CLASSES_NUMBER, DEFAULT_MAX_CLASSES_NUMBER);
        store.setDefault(WIDE_SCOPE_SWT_RESOURCE_FILTER, DEFAULT_WIDE_SCOPE_SWT_RESOURCE_FILTER);
    }
}
