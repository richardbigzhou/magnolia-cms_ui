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
package info.magnolia.ui.vaadin.integration.contentconnector;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCR-based implementation of {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector}.
 */
public class JcrContentConnector extends AbstractContentConnector implements SupportsVersions, SupportsCreation {

    private Logger log = LoggerFactory.getLogger(getClass());

    private VersionManager versionManager;

    private JcrItemId defaultItemId;

    @Inject
    public JcrContentConnector(final VersionManager versionManager, JcrContentConnectorDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
        this.versionManager = versionManager;
        try {
            this.defaultItemId = JcrItemUtil.getItemId(getWorkspace(), getPath());
        } catch (RepositoryException e) {
            log.error("Failed to retrieve default id: " + e.getMessage(), e);
            this.defaultItemId = null;
        }
    }

    @Override
    public String getItemUrlFragment(Object itemId) {
        try {
            if (itemId instanceof JcrItemId) {
                JcrItemId jcrItemId = (JcrItemId) itemId;
                javax.jcr.Item selected = JcrItemUtil.getJcrItem(getWorkspace(), jcrItemId);
                String selectedPath = JcrItemUtil.getItemPath(selected);
                String path = getPath();
                return StringUtils.removeStart(selectedPath, "/".equals(path) ? "" : path);
            }
        } catch (RepositoryException e) {
            log.error("Failed to convert item id to URL fragment: " + e.getMessage(), e);
        }
        return null;
    }


    @Override
    public JcrItemId getItemIdByUrlFragment(String urlFragment) {
        try {
            String fullFragment = ("/".equals(getPath()) ? "" : getPath()) + urlFragment;
            String nodePath = parseNodePath(fullFragment);
            JcrItemId nodeItemId = JcrItemUtil.getItemId(getWorkspace(), nodePath);
            if (!isPropertyItemId(fullFragment)) {
                return nodeItemId;
            } else {
                return new JcrPropertyItemId(nodeItemId, parsePropertyName(fullFragment));
            }
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
                if (itemId instanceof JcrNewNodeItemId) {
                    return new JcrNewNodeAdapter((Node) jcrItem, ((JcrNewNodeItemId)itemId).getPrimaryNodeType());
                }
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
        return defaultItemId;
    }

    @Override
    public boolean canHandleItem(Object itemId) {
        // XXXX  ???
        log.info("Trying to re-sync workbench with no longer existing path {} at workspace {}. Will reset path to its configured root {}.", new Object[] { itemId, getWorkspace(), getPath() });
        return (itemId instanceof JcrItemId) && ((JcrItemId)itemId).getWorkspace().equalsIgnoreCase(getWorkspace());
    }

    @Override
    public Object getItemVersion(Object itemId, String versionName) {
        try {
            Node node = NodeUtil.getNodeByIdentifier(getWorkspace(), String.valueOf(itemId));
            Version version = versionManager.getVersion(node, versionName);
            return JcrItemUtil.getItemId(version.getFrozenNode());
        } catch (RepositoryException e) {
            log.error("Failed to find item version for id: " + itemId, e.getMessage());
        }
        return null;
    }

    @Override
    public Object getNewItemId(String newItemPath, Object typeDefinition) {

        String primaryNodeType = String.valueOf(typeDefinition);
        String parentPath = StringUtils.substringBeforeLast(newItemPath, "/");
        parentPath = parentPath.isEmpty() ? getPath() : parentPath;
        Node parent = SessionUtil.getNode(getWorkspace(), parentPath);
        try {
            return new JcrNewNodeItemId(parent.getIdentifier(), getWorkspace(), primaryNodeType);
        } catch (RepositoryException e) {
            log.error("Failed to create new jcr node item id: " + e.getMessage(), e);
        }

        return null;
    }

    private String getPath() {
        return getContentConnectorDefinition().getPath();
    }

    private String getWorkspace() {
        return getContentConnectorDefinition().getWorkspace();
    }

    @Override
    public JcrContentConnectorDefinition getContentConnectorDefinition() {
        return (JcrContentConnectorDefinition) super.getContentConnectorDefinition();
    }

    /**
     * String separating property name and node identifier.
     */
    private static final String PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR = "@";

    /**
     * @return all chars in front of #PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR - if it doesn't contain #PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR the provided itemId (then we assume it's already a nodeId)
     */
    private  String parseNodePath(final String urlFragment) {
        return isPropertyItemId(urlFragment) ? urlFragment.substring(0, urlFragment.indexOf(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR)) : urlFragment;
    }

    private  String parsePropertyName(final String urlFragment) {
        return urlFragment.substring(urlFragment.indexOf(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR) + 1);
    }

    private  boolean isPropertyItemId(final String urlFragment) {
        return urlFragment != null && urlFragment.contains(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR);
    }
}
