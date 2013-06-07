/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemRightClickedEvent;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Abstract generic logic for content presenters.
 */
public abstract class AbstractContentPresenter implements ContentPresenter, ContentView.Listener {

    private static final Logger log = LoggerFactory.getLogger(AbstractContentPresenter.class);

    private static final String ICON_PROPERTY = "icon-node-data";

    private static final String ICON_TRASH = "icon-trash";

    protected EventBus eventBus;

    protected WorkbenchDefinition workbenchDefinition;

    protected String viewTypeName;

    private String selectedItemId;
    private final ComponentProvider componentProvider;

    @Inject
    public AbstractContentPresenter(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    protected ComponentProvider getComponentProvider() {
        return componentProvider;
    }

    @Override
    public ContentView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;
        this.viewTypeName = viewTypeName;
        return null;
    }

    @Override
    public String getSelectedItemId() {
        return selectedItemId;
    }

    @Override
    public void setSelectedItemId(String selectedItemId) {
        this.selectedItemId = selectedItemId;
    }

    // CONTENT VIEW LISTENER

    @Override
    public void onItemSelection(Item item) {
        try {
            if (item == null) {
                log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
                selectedItemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), workbenchDefinition.getPath());
                eventBus.fireEvent(new ItemSelectedEvent(workbenchDefinition.getWorkspace(), null));
            } else {
                selectedItemId = ((JcrItemAdapter) item).getItemId();
                log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemId);
                eventBus.fireEvent(new ItemSelectedEvent(workbenchDefinition.getWorkspace(), (JcrItemAdapter) item));
            }
        } catch (Exception e) {
            log.error("An error occurred while selecting a row in the data grid", e);
        }
    }

    @Override
    public void onDoubleClick(Item item) {
        if (item != null) {
            try {
                setSelectedItemId(((JcrItemAdapter) item).getItemId());
                log.debug("com.vaadin.data.Item at {} was double clicked. Firing ItemDoubleClickedEvent...", selectedItemId);
                eventBus.fireEvent(new ItemDoubleClickedEvent(workbenchDefinition.getWorkspace(), selectedItemId));
            } catch (Exception e) {
                log.error("An error occurred while double clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    @Override
    public void onRightClick(Item item, int clickX, int clickY) {
        if (item != null) {
            try {
                selectedItemId = ((JcrItemAdapter) item).getItemId();
                String clickedItemId = ((JcrItemAdapter) item).getItemId();
                log.debug("com.vaadin.data.Item at {} was right clicked. Firing ItemRightClickedEvent...", clickedItemId);
                eventBus.fireEvent(new ItemRightClickedEvent(workbenchDefinition.getWorkspace(), (JcrItemAdapter) item, clickX, clickY));
            } catch (Exception e) {
                log.error("An error occurred while right clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    protected Iterator<ColumnDefinition> getColumnsIterator() {
        Iterator<ContentPresenterDefinition> viewsIterator = workbenchDefinition.getContentViews().iterator();
        while (viewsIterator.hasNext()) {
            ContentPresenterDefinition contentView = viewsIterator.next();
            if (contentView.getViewType().getText().equals(viewTypeName)) {
                return getAvailableColumns(contentView.getColumns()).iterator();
            }
        }
        return null;
    }

    @Override
    public String getIcon(Item item) {
        try {
            if (item instanceof JcrPropertyAdapter) {
                return ICON_PROPERTY;
            } else if (item instanceof JcrNodeAdapter) {
                Node node = ((AbstractJcrNodeAdapter)item).getJcrItem();
                if (NodeUtil.hasMixin(node, NodeTypes.Deleted.NAME)) {
                    return ICON_TRASH;
                }

                NodeTypeDefinition nodeTypeDefinition = getNodeTypeDefinitionForNode(node);
                if (nodeTypeDefinition != null) {
                    return nodeTypeDefinition.getIcon();
                }
            }

        } catch (RepositoryException e) {
            log.warn("Unable to resolve icon", e);
        }
        return null;
    }

    private NodeTypeDefinition getNodeTypeDefinitionForNode(Node node) throws RepositoryException {
        String primaryNodeTypeName = node.getPrimaryNodeType().getName();
        for (NodeTypeDefinition nodeTypeDefinition : workbenchDefinition.getNodeTypes()) {
            if (nodeTypeDefinition.isStrict()) {
                if (primaryNodeTypeName.equals(nodeTypeDefinition.getName())) {
                    return nodeTypeDefinition;
                }
            } else if (NodeUtil.isNodeType(node, nodeTypeDefinition.getName())) {
                return nodeTypeDefinition;
            }
        }
        return null;
    }

    protected List<ColumnDefinition> getAvailableColumns(final List<ColumnDefinition> allColumns) {
        final List<ColumnDefinition> availableColumns = new ArrayList<ColumnDefinition>();
        Iterator<ColumnDefinition> it = allColumns.iterator();
        while (it.hasNext()) {
            ColumnDefinition column = it.next();
            if (column.isEnabled()) {
                availableColumns.add(column);
            }
        }
        return availableColumns;
    }
}
