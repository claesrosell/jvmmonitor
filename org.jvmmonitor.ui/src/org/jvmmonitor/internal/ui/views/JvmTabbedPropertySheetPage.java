/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.views;

import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jvmmonitor.core.IActiveJvm;

/**
 * The tabbed property sheet page.
 */
class JvmTabbedPropertySheetPage extends TabbedPropertySheetPage {

    private IActiveJvm jvm;

    JvmTabbedPropertySheetPage(ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
        super(tabbedPropertySheetPageContributor);
    }

    @Override
    public void resizeScrolledComposite() {
        // no scroll bar except for section itself
    }

    void setJvm(IActiveJvm jvm) {
        this.jvm = jvm;
    }

    IActiveJvm getJvm() {
        return jvm;
    }
}
