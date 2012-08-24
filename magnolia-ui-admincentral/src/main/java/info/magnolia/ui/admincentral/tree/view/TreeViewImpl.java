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
import info.magnolia.ui.admincentral.container.AbstractJcrContainer;
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


/**
 * Vaadin UI component that displays a tree.
 *
 */
public class TreeViewImpl implements TreeView {

    private final MagnoliaTreeTable jcrBrowser;

    private final VerticalLayout margin = new VerticalLayout();

    private ContentView.Listener listener;

    private Set<?> defaultValue = null;
    
    public TreeViewImpl(WorkbenchDefinition workbenchDefinition, TreeModel treeModel,ComponentProvider componentProvider) {

        jcrBrowser = new MagnoliaTreeTable(workbenchDefinition, treeModel, componentProvider);
        jcrBrowser.setImmediate(true);
        jcrBrowser.setNullSelectionAllowed(false);
        jcrBrowser.setSizeFull();
        
        jcrBrowser.addListener(new TreeTable.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (defaultValue == null && event.getProperty().getValue() instanceof Set) {
                    defaultValue = (Set<?>)event.getProperty().getValue();
                }
                if (asVaadinComponent().getApplication() != null) {
                    asVaadinComponent().getApplication().getMainWindow().showNotification(
                            event.getProperty().getValue() + " " + event.getProperty().getValue().getClass().getCanonicalName());                    
                }
                final Object value = event.getProperty().getValue();
                if (value instanceof String) {
                    presenterOnItemSelection(String.valueOf(value));
                } else if (value instanceof Set) {
                    final Set< ? > set = new HashSet<Object>((Set< ? >) value);
                    set.removeAll(defaultValue);
                    if (set.size() == 1) {
                        presenterOnItemSelection(String.valueOf(set.iterator().next()));
                    }
                }
            }
        });
        jcrBrowser.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if(event.isDoubleClick()) {
                    presenterOnDoubleClick(String.valueOf(event.getItemId()));
                }
            }
        });
        margin.setStyleName("mgnl-content-view");
        margin.addComponent(jcrBrowser);
    }

    private void presenterOnItemSelection(String id) {
        if (listener != null) {
            listener.onItemSelection(jcrBrowser.getItem(id));
        }
    }
    private void presenterOnDoubleClick(String id) {
        if (listener != null) {
            listener.onDoubleClick(jcrBrowser.getItem(id));
        }
    }

    /**
     *
     * @param path relative to the tree root, must start with /
     */
    @Override
    public void select(String path) {
        jcrBrowser.select(path);
    }

    @Override
    public void refresh() {
        jcrBrowser.refresh();
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
        jcrBrowser.updateItem(item);
    }
}
