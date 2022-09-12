/*******************************************************************************
 * Copyright (c) 2021 JVM Monitor project. All rights reserved.
 *
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.internal.ui.properties.thread;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jvmmonitor.internal.ui.IConfigurableColumns;
import org.jvmmonitor.internal.ui.actions.ConfigureColumnsAction;
import org.jvmmonitor.internal.ui.actions.CopyAction;
import org.jvmmonitor.internal.ui.actions.OpenDeclarationAction;
import org.jvmmonitor.ui.Activator;

/**
 * The filtered tree to show eclipse jobs.
 */
public class EclipseJobFilteredTree extends FilteredTree implements
        IConfigurableColumns, IPropertyChangeListener {

    private LinkedHashMap<String, Boolean> columns;

    private ConfigureColumnsAction configureColumnsAction;

    EclipseJobFilteredTree(Composite parent, IActionBars actionBars) {
        super(parent, SWT.MULTI | SWT.FULL_SELECTION, new PatternFilter(), true, true);

        loadColumnsPreference();
        configureTree();
        sortByStateColumn();
        createContextMenu(actionBars);
        setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }

    @Override
    protected void createControl(Composite composite, int treeStyle) {
        super.createControl(composite, treeStyle);

        // adjust the indentation of filter composite
        GridData data = (GridData) filterComposite.getLayoutData();
        data.horizontalIndent = 2;
        data.verticalIndent = 2;
        filterComposite.setLayoutData(data);
    }

    @Override
    public List<String> getColumns() {
        ArrayList<String> columnLabels = new ArrayList<>();
        EclipseJobColumn[] values = EclipseJobColumn.values();
        for (EclipseJobColumn value : values) {
            columnLabels.add(value.label);
        }
        return columnLabels;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public boolean getDefaultVisibility(String column) {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!event.getProperty().equals(getId())
                || getViewer().getTree().isDisposed()) {
            return;
        }

        String columnsString = (String) event.getNewValue();
        if (columnsString == null || columnsString.isEmpty()) {
            return;
        }

        setColumns(columnsString);
        configureTree();
        getViewer().refresh();
    }

    @Override
    public void dispose() {
        super.dispose();
        Activator.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
    }

    private void loadColumnsPreference() {
        columns = new LinkedHashMap<>();
        String value = Activator.getDefault().getPreferenceStore()
                .getString(getId());
        if (value.isEmpty()) {
            for (EclipseJobColumn column : EclipseJobColumn.values()) {
                columns.put(column.label, true);
            }
        } else {
            setColumns(value);
        }
    }

    private void setColumns(String columnData) {
        columns.clear();
        for (String column : columnData.split(",")) { //$NON-NLS-1$
            String[] elemnets = column.split("="); //$NON-NLS-1$
            String columnName = elemnets[0];
            Boolean columnVisibility = Boolean.valueOf(elemnets[1]);
            columns.put(columnName, columnVisibility);
        }
    }

    private void configureTree() {
        for (TreeColumn column : getViewer().getTree().getColumns()) {
            column.dispose();
        }

        getViewer().getTree().setLinesVisible(true);
        getViewer().getTree().setHeaderVisible(true);
        for (Entry<String, Boolean> entry : columns.entrySet()) {
            EclipseJobColumn column = EclipseJobColumn.getColumn(entry.getKey());
            if (!columns.get(column.label)) {
                continue;
            }

            TreeColumn treeColumn = new TreeColumn(getViewer().getTree(),
                    SWT.NONE);
            treeColumn.setText(column.label);
            treeColumn.setWidth(column.defalutWidth);
            treeColumn.setAlignment(column.initialAlignment);
            treeColumn.setToolTipText(column.toolTip);
            treeColumn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.widget instanceof TreeColumn) {
                        sortColumn((TreeColumn) e.widget);
                    }
                }
            });
        }
    }

    private void sortByStateColumn() {
        for (TreeColumn column : getViewer().getTree().getColumns()) {
            if (EclipseJobColumn.STATE.label.equals(column.getText())) {
                sortColumn(column);
                break;
            }
        }
    }

    void sortColumn(TreeColumn treeColumn) {
        int columnIndex = getViewer().getTree().indexOf(treeColumn);
        EclipseJobComparator sorter = (EclipseJobComparator) getViewer()
                .getComparator();

        if (sorter != null && columnIndex == sorter.getColumnIndex()) {
            sorter.reverseSortDirection();
        } else {
            sorter = new EclipseJobComparator(columnIndex);
            getViewer().setComparator(sorter);
        }
        getViewer().getTree().setSortColumn(treeColumn);
        getViewer().getTree().setSortDirection(sorter.getSortDirection());
        getViewer().refresh();
    }

    private void createContextMenu(IActionBars actionBars) {
        final OpenDeclarationAction openAction = OpenDeclarationAction
                .createOpenDeclarationAction(actionBars);
        final CopyAction copyAction = CopyAction.createCopyAction(actionBars);
        configureColumnsAction = new ConfigureColumnsAction(this);
        getViewer().addSelectionChangedListener(openAction);
        getViewer().addSelectionChangedListener(copyAction);

        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(openAction);
                manager.add(copyAction);
                manager.add(new Separator());
                manager.add(configureColumnsAction);
            }
        });

        Menu menu = menuMgr.createContextMenu(getViewer().getControl());
        getViewer().getControl().setMenu(menu);
    }
}
