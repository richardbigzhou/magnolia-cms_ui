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
package info.magnolia.ui.contentapp.contentconnector;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.contentconnector.SupportsCreation;
import info.magnolia.ui.vaadin.integration.contentconnector.SupportsVersions;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * JCR-based implementation of {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector}.
 */
public class JcrContentConnector extends AbstractContentConnector implements SupportsVersions, SupportsCreation {

    private Logger log = LoggerFactory.getLogger(getClass());

    private SubAppContext subAppContext;

    private VersionManager versionManager;

    @Inject
    public JcrContentConnector(SubAppContext subAppContext, final VersionManager versionManager, JcrContentConnectorDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
        this.subAppContext = subAppContext;
        this.versionManager = versionManager;
    }

    @Override
    public String getItemUrlFragment(Object itemId) {
        try {
            if (itemId instanceof JcrItemId) {
                JcrItemId jcrItemId = (JcrItemId) itemId;
                javax.jcr.Item selected = JcrItemUtil.getJcrItem(getWorkspace(), jcrItemId);
                String path = getPath();
                return StringUtils.removeStart(selected.getPath(), "/".equals(path) ? "" : path);
            }
        } catch (RepositoryException e) {
            log.error("Failed to convert item id to URL fragment: " + e.getMessage(), e);
        }
        return null;
    }


    @Override
    public JcrItemId getItemIdByUrlFragment(String urlFragment) {
        try {
            return new JcrItemId(JcrItemUtil.getItemId(getWorkspace(), urlFragment), getWorkspace());
        } catch (RepositoryException e) {
            log.error("Failed to obtain JCR id for fragment: " + e.getMessage(), e);
            return null;
        }
    }


    @Override
    public JcrItemAdapter getItem(Object itemId) {
        if (!(itemId instanceof JcrItemId)) {
            return null;
        }

        javax.jcr.Item jcrItem;
        try {
            jcrItem = JcrItemUtil.getJcrItem(getWorkspace(), (JcrItemId) itemId);
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
        } catch (Exception e) {
            log.error("Unknown error for: " + itemId, e.getMessage());
            return null;
        }
    }

    @Override
    public Object getDefaultItemId() {
        return getItemIdByUrlFragment(getWorkbenchDefinition().getPath());
    }

    @Override
    public boolean canHandleItem(Object itemId) {
        return (itemId instanceof JcrItemId) &&
                ((JcrItemId)itemId).getWorkspace().equalsIgnoreCase(getWorkspace());
    }

    protected WorkbenchDefinition getWorkbenchDefinition() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof BrowserSubAppDescriptor) {
            return ((BrowserSubAppDescriptor) subAppDescriptor).getWorkbench();
        }
        return null;
    }

    @Override
    public JcrNodeAdapter getItemVersion(Object itemId, String versionName) {
        try {
            Node node = NodeUtil.getNodeByIdentifier(getWorkspace(), String.valueOf(itemId));
            Version version = versionManager.getVersion(node, versionName);
            return new JcrNodeAdapter(version.getFrozenNode());
        } catch (RepositoryException e) {
            log.error("Failed to find item version for id: " + itemId, e.getMessage());
        }
        return null;
    }

    @Override
    public Item createNew(String newItemPath) {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();

        String primaryNodeType = null;
        if (subAppDescriptor instanceof DetailSubAppDescriptor) {
            primaryNodeType = ((DetailSubAppDescriptor) subAppDescriptor).getEditor().getNodeType().getName();
        }

        if (primaryNodeType != null) {
            String parentPath = StringUtils.substringBeforeLast(newItemPath, "/");
            parentPath = parentPath.isEmpty() ? "/" : parentPath;
            Node parent = SessionUtil.getNode(getWorkspace(), parentPath);
            return new JcrNewNodeAdapter(parent, primaryNodeType);
        }

        return null;
    }

    private String getPath() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof BrowserSubAppDescriptor) {
            return ((BrowserSubAppDescriptor) subAppDescriptor).getWorkbench().getPath();
        }

        return "/";
    }

    private String getWorkspace() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof BrowserSubAppDescriptor) {
            return ((BrowserSubAppDescriptor) subAppDescriptor).getWorkbench().getWorkspace();
        }

        if (subAppDescriptor instanceof DetailSubAppDescriptor) {
            return ((DetailSubAppDescriptor) subAppDescriptor).getEditor().getWorkspace();
        }
        return null;
    }

    private Collection<NodeTypeDefinition> getNodeTypes() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof BrowserSubAppDescriptor) {
            return ((BrowserSubAppDescriptor) subAppDescriptor).getWorkbench().getNodeTypes();
        }

        if (subAppDescriptor instanceof DetailSubAppDescriptor) {
            return Arrays.asList(((DetailSubAppDescriptor) subAppDescriptor).getEditor().getNodeType());
        }
        return null;
    }
}
