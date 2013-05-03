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
package info.magnolia.ui.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Presenter for ContentView.
 */
public class ContentPresenter implements ContentView.Listener {

    private static final String ICON_PROPERTY = "icon-node-data";

    private static final Logger log = LoggerFactory.getLogger(ContentPresenter.class);

    private EventBus eventBus;

    private final ContentViewBuilder contentViewBuilder;

    private WorkbenchDefinition workbenchDefinition;

    private String selectedItemPath;

    private ImageProviderDefinition imageProviderDefinition;

    @Inject
    public ContentPresenter(final ContentViewBuilder contentViewBuilder) {
        this.contentViewBuilder = contentViewBuilder;
    }

    public void start(WorkbenchView workbenchView, WorkbenchDefinition workbenchDefinition, ImageProviderDefinition imageProviderDefinition, EventBus eventBus) {
        this.workbenchDefinition = workbenchDefinition;
        this.imageProviderDefinition = imageProviderDefinition;
        this.eventBus = eventBus;
        initContentView(workbenchView);
    }

    @Override
    public void onItemSelection(Item item) {
        if (item == null) {
            log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
            selectedItemPath = workbenchDefinition.getPath();
            eventBus.fireEvent(new ItemSelectedEvent(workbenchDefinition.getWorkspace(), null));
            return;
        }
        try {
            selectedItemPath = ((JcrItemAdapter) item).getPath();
            log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemPath);
            eventBus.fireEvent(new ItemSelectedEvent(workbenchDefinition.getWorkspace(), (JcrItemAdapter) item));
        } catch (Exception e) {
            log.error("An error occurred while selecting a row in the data grid", e);
        }
    }

    /**
     * @return the path of the vaadin item currently selected in the currently active {@link ContentView}. It is
     *         equivalent to javax.jcr.Item#getPath().
     * @see JcrItemAdapter#getPath()
     */
    public String getSelectedItemPath() {
        return selectedItemPath;
    }

    public void setSelectedItemPath(String selectedItemPath) {
        this.selectedItemPath = selectedItemPath;
    }

    @Override
    public void onDoubleClick(Item item) {
        if (item != null) {
            try {
                selectedItemPath = ((JcrItemAdapter) item).getPath();
                log.debug("com.vaadin.data.Item at {} was double clicked. Firing ItemDoubleClickedEvent...", selectedItemPath);
                eventBus.fireEvent(new ItemDoubleClickedEvent(workbenchDefinition.getWorkspace(), selectedItemPath));
            } catch (Exception e) {
                log.error("An error occurred while double clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    @Override
    public void onItemEdited(Item item) {
        try {
            if (item != null) {
                log.debug("com.vaadin.data.Item edited. Firing ItemEditedEvent...");
                eventBus.fireEvent(new ItemEditedEvent(item));
            } else {
                log.warn("Null item edited");
            }
        } catch (Exception e) {
            log.error("An error occurred while double clicking on a row in the data grid", e);
        }
    }

    @Override
    public String getItemIcon(Item item) {
        if (item instanceof JcrPropertyAdapter && workbenchDefinition.includeProperties()) {
            return ICON_PROPERTY;
        }

        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter node = (JcrNodeAdapter) item;
            String typeName = node.getPrimaryNodeTypeName();
            List<NodeTypeDefinition> nodeTypes = workbenchDefinition.getNodeTypes();
            for (NodeTypeDefinition currentNodeType: nodeTypes) {
                if (currentNodeType.getName().equals(typeName)) {
                    return currentNodeType.getIcon();
                }
            }
        }

        return null;
    }

    protected void initContentView(WorkbenchView parentView) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName() + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        for (final ContentPresenterDefinition contentViewDefinition : workbenchDefinition.getContentViews()) {
            final ContentView contentView = contentViewBuilder.build(workbenchDefinition, imageProviderDefinition, contentViewDefinition);
            contentView.setListener(this);
            contentView.select(workbenchDefinition.getPath());
            parentView.addContentView(contentViewDefinition.getViewType(), contentView, contentViewDefinition);
        }

        selectedItemPath = workbenchDefinition.getPath();
    }

    protected WorkbenchDefinition getWorkbenchDefinition() {
        return workbenchDefinition;
    }
}
