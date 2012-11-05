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
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Vaadin UI component that displays a tree.
 */
public class TreeViewImpl implements TreeView {

    private static final Logger log = LoggerFactory.getLogger(TreeViewImpl.class);

    private final WorkbenchTreeTable treeTable;

    private final VerticalLayout margin = new VerticalLayout();

    private ContentView.Listener listener;

    private final HierarchicalJcrContainer container;

    private Set<?> defaultValue = null;

    public TreeViewImpl(WorkbenchDefinition workbenchDefinition, ComponentProvider componentProvider, HierarchicalJcrContainer container) {
        this.container = container;
        treeTable = new WorkbenchTreeTable(workbenchDefinition, componentProvider, container);
        treeTable.setImmediate(true);
        treeTable.setNullSelectionAllowed(true);
        treeTable.setSizeFull();

        treeTable.addListener(new TreeTable.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (defaultValue == null && event.getProperty().getValue() instanceof Set) {
                    defaultValue = (Set<?>) event.getProperty().getValue();
                }
                final Object value = event.getProperty().getValue();
                if (value instanceof String) {
                    presenterOnItemSelection(String.valueOf(value));
                } else if (value instanceof Set) {
                    final Set<?> set = new HashSet<Object>((Set<?>) value);
                    set.removeAll(defaultValue);
                    if (set.size() == 1) {
                        presenterOnItemSelection(String.valueOf(set.iterator().next()));
                    } else if (set.size() == 0) {
                        presenterOnItemSelection(null);
                        treeTable.setValue(null);
                    }
                }
            }
        });
        
        treeTable.addListener(new ItemClickEvent.ItemClickListener() {
            private Object previousSelection;

            @Override
            public void itemClick(ItemClickEvent event) {
                Object currentSelection = event.getItemId();
                if (event.isDoubleClick()) {
                    presenterOnDoubleClick(String.valueOf(event.getItemId()));
                } else {
                    //toggle will deselect
                    if (previousSelection == currentSelection) {
                        treeTable.setValue(null);
                    }
                }

                previousSelection = currentSelection;
            }
        });
        margin.setSizeFull();
        margin.setStyleName("mgnl-content-view");
        margin.addComponent(treeTable);
    }

    private void presenterOnItemSelection(String id) {
        if (listener != null) {
            listener.onItemSelection(treeTable.getItem(id));
        }
    }
    private void presenterOnDoubleClick(String id) {
        if (listener != null) {
            listener.onDoubleClick(treeTable.getItem(id));
        }
    }

    /**
     *
     * @param path relative to the tree root, must start with /
     */
    @Override
    public void select(String path) {
        treeTable.select(path);
    }

    @Override
    public void refresh() {
        container.refresh();
    }

    @Override
    public Component asVaadinComponent() {
        return margin;
    }

    @Override
    public void setListener(ContentView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public AbstractJcrContainer getContainer() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this operation");
    }

    @Override
    public void refreshItem(final Item item) {
        final String itemId = ((JcrItemAdapter) item).getItemId();
        if (container.containsId(itemId)) {
            container.fireItemSetChange();
        } else {
            log.warn("No item found for id [{}]", itemId);
        }
    }

    @Override
    public ViewType getViewType() {
        return ViewType.TREE;
    }
}
