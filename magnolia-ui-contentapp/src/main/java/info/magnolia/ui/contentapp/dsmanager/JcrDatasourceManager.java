/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.contentapp.dsmanager;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.dsmanager.DataSourceManager;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.list.FlatJcrContainer;
import info.magnolia.ui.workbench.search.SearchJcrContainer;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * JCR-based implementation of {@link DataSourceManager}.
 */
public class JcrDataSourceManager extends AbstractDataSourceManager {

    private Logger log = LoggerFactory.getLogger(getClass());

    private SubAppContext subAppContext;

    @Inject
    public JcrDataSourceManager(SubAppContext subAppContext, @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(eventBus);
        this.subAppContext = subAppContext;
    }

    @Override
    public String serializeItemId(Object itemId) {
        try {
            WorkbenchDefinition workbenchDefinition = getWorkbenchDefinition();
            javax.jcr.Item selected = JcrItemUtil.getJcrItem(workbenchDefinition.getWorkspace(), JcrItemUtil.parseNodeIdentifier(String.valueOf(itemId)));
            String workbenchPath = workbenchDefinition.getPath();
            return StringUtils.removeStart(selected.getPath(), "/".equals(workbenchPath) ? "" : workbenchPath);
        } catch (RepositoryException e) {
            log.error("Failed to convert item id to URL fragment: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Object deserializeItemId(String strPath) {
        try {
            return JcrItemUtil.getItemId(getWorkbenchDefinition().getWorkspace(), strPath);
        } catch (RepositoryException e) {
            log.error("Failed to obtain JCR id for fragment: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Item getItemById(Object itemId) {
        javax.jcr.Item jcrItem;
        try {
            jcrItem = JcrItemUtil.getJcrItem(getWorkbenchDefinition().getWorkspace(), String.valueOf(itemId));
            JcrItemAdapter itemAdapter;
            if (jcrItem.isNode()) {
                itemAdapter = new JcrNodeAdapter((Node) jcrItem);
            } else {
                itemAdapter = new JcrPropertyAdapter((Property) jcrItem);
            }
            return itemAdapter;
        } catch (RepositoryException e) {
            log.error("Failed to find item for id: " + itemId, e.getMessage());
            return null;
        }
    }

    @Override
    public Object getItemId(Item item) {
        if (item instanceof JcrItemAdapter) {
            return ((JcrItemAdapter) item).getItemId();
        }
        return null;
    }

    @Override
    public Object getRootItemId() {
        return deserializeItemId(getWorkbenchDefinition().getPath());
    }

    @Override
    public boolean itemExists(Object itemId) {
        return getItemById(itemId) != null;
    }

    private WorkbenchDefinition getWorkbenchDefinition() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof BrowserSubAppDescriptor) {
            return ((BrowserSubAppDescriptor) subAppDescriptor).getWorkbench();
        }
        return null;
    }

    @Override
    public Container getContainerForViewType(String contentViewId) {
        if ("treeview".equalsIgnoreCase(contentViewId)) {
            return new HierarchicalJcrContainer(getWorkbenchDefinition());
        } else if ("searchview".equalsIgnoreCase(contentViewId)) {
            return new SearchJcrContainer(getWorkbenchDefinition());
        }
        return new FlatJcrContainer(getWorkbenchDefinition());
    }
}
