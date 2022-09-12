/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core;

/**
 * The constants used for preferences.
 */
public interface IPreferenceConstants {

    /**
     * The preference ID to get preferences from <tt>IPreferencesService</tt>.
     */
    static final String PREFERENCES_ID = "org.jvmmonitor.preferences"; //$NON-NLS-1$

    /**
     * The default value for period to search running JVMs on local host. The
     * unit is milliseconds.
     */
    static final int DEFAULT_SEARCH_JVM_PERIOD = 3000;

    /** The default value for period to update model. The unit is milliseconds. */
    static final int DEFAULT_UPDATE_PERIOD = 1000;

    /** The default value for max number of classes. */
    static final int DEFAULT_MAX_CLASSES_NUMBER = 50;

    /** The default value for legend visibility. */
    static final boolean DEFAULT_LEGEND_VISIBILITY = false;

    /** The default value for threads filter to take stack traces into account. */
    static final boolean DEFAULT_WIDE_SCOPE_THREAD_FILTER = true;

    /** The default value for SWT resources filter to take stack traces into account. */
    static final boolean DEFAULT_WIDE_SCOPE_SWT_RESOURCE_FILTER = true;

    /** The preference key for period of searching JVMs. */
    static final String SEARCH_JVM_PERIOD = "SearchJvmPeriod"; //$NON-NLS-1$

    /** The preference key for update period. */
    static final String UPDATE_PERIOD = "UpdatePeriod"; //$NON-NLS-1$

    /** The preference key for legend visibility. */
    static final String LEGEND_VISIBILITY = "LegendVisibility"; //$NON-NLS-1$

    /** The preference key for threads filter to take stack traces into account. */
    static final String WIDE_SCOPE_THREAD_FILTER = "WideScopeThreadFilter"; //$NON-NLS-1$

    /** The preference key for max number of classes on memory page. */
    static final String MAX_CLASSES_NUMBER = "org.jvmmonitor.memory.maxClassCount"; //$NON-NLS-1$

    /** The preference key for SWT resources filter to take stack traces into account. */
    static final String WIDE_SCOPE_SWT_RESOURCE_FILTER = "WideScopeSWTResourcesFilter"; //$NON-NLS-1$

}
