/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.tree.view;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.ColumnFormatter;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.container.TreeModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;


/**
 * User interface component that extends TreeTable and uses a WorkbenchDefinition for layout and
 * invoking command callbacks.
 */
@SuppressWarnings("serial")
public class WorkbenchTreeTable extends MagnoliaTreeTable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HierarchicalJcrContainer container;

    private final TreeModel treeModel;

    public WorkbenchTreeTable(WorkbenchDefinition workbenchDefinition, TreeModel treeModel, ComponentProvider componentProvider) {
        super();
        this.treeModel = treeModel;

        setSizeFull();
        setEditable(false);
        setSelectable(true);
        setColumnCollapsingAllowed(true);
        setColumnReorderingAllowed(false);
        setImmediate(true);

        container = new HierarchicalJcrContainer(treeModel, workbenchDefinition);
        buildColumns(workbenchDefinition, componentProvider);
    }

    public void select(String itemId) {
        if (!container.isRoot(itemId)) {
            String parent = container.getParent(itemId);
            while (!container.isRoot(parent)) {
                setCollapsed(parent, false);
                parent = container.getParent(parent);
            }
            // finally expand the root else children won't be visibile.
            setCollapsed(parent, false);
        }
        // Set this multi select component to have only this one item selected
        final Set<Object> set = new HashSet<Object>();
        set.add(itemId);
        setValue(set);
    }

    public void refresh() {
        container.fireItemSetChange();
    }

    public void updateItem(final Item item) {
        final String itemId = ((JcrItemAdapter) item).getItemId();
        if (container.containsId(itemId)) {
            container.fireItemSetChange();
        } else {
            log.warn("No item found for id [{}]", itemId);
        }
    }

    private void buildColumns(WorkbenchDefinition workbenchDefinition, ComponentProvider componentProvider) {
        final Iterator<ColumnDefinition> iterator = workbenchDefinition.getColumns().iterator();
        final List<String> columnOrder = new ArrayList<String>();
        while (iterator.hasNext()) {
            final ColumnDefinition column = iterator.next();
            if (workbenchDefinition.isDialogWorkbench() && !column.isDisplayInDialog()) {
                continue;
            }
            final String columnProperty = column.getPropertyName() != null ? column.getPropertyName() : column.getName();
            // FIXME fgrilli workaround for conference
            // when setting cols width in dialogs we are forced to use explicit
            // px value instead of expand ratios, which for some reason don't
            // work
            if (workbenchDefinition.isDialogWorkbench()) {
                setColumnWidth(columnProperty, 300);
            } else {
                if (column.getWidth() > 0) {
                    setColumnWidth(columnProperty, column.getWidth());
                } else {
                    setColumnExpandRatio(columnProperty, column.getExpandRatio());
                }
            }
            setColumnHeader(columnProperty, column.getLabel());
            container.addContainerProperty(columnProperty, column.getType(), "");
            // Set Formatter
            if (StringUtils.isNotBlank(column.getFormatterClass())) {
                try {
                    addGeneratedColumn(
                        columnProperty,
                        (ColumnFormatter) componentProvider.newInstance(Class.forName(column.getFormatterClass()), column));
                } catch (ClassNotFoundException e) {
                    log.error("Not able to create the Formatter", e);
                }
            } else {
                container.addContainerProperty(columnProperty, column.getType(), "");
            }
            columnOrder.add(columnProperty);
        }
        setContainerDataSource(container);
        setVisibleColumns(columnOrder.toArray());
    }

}
