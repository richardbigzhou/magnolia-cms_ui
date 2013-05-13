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
package info.magnolia.ui.vaadin.integration.jcr;

import info.magnolia.context.MgnlContext;

import javax.jcr.Node;
import javax.jcr.Item;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports retrieving Jcr Items or uuids from itemId Strings and vice versa.
 *
 * each itemId can be a
 *   nodeId     -> the jcr uuid of the node
 *   propertyId -> the jcr uuid of the hosting node + #PROPERTY_NAME_AND_UUID_SEPARATOR + the name of the property
 */
public class JcrItemUtil {
    /**
     * String separating a properties name and the uuid of its node.
     */
    public static final String PROPERTY_NAME_AND_UUID_SEPARATOR = "@";

    private static final Logger log = LoggerFactory.getLogger(JcrItemUtil.class);

    /**
     * @return all chars in front of #PROPERTY_NAME_AND_UUID_SEPARATOR - if it doesn't contain #PROPERTY_NAME_AND_UUID_SEPARATOR the provided itemId (then we assume it's already a nodeId)
     */
    public static String getNodeUuidFrom(final String itemId) {
        return isPropertyId(itemId) ? itemId.substring(0, itemId.indexOf(PROPERTY_NAME_AND_UUID_SEPARATOR)) : itemId;
    }

    public static String getPropertyName(final String propertyId) {
        return propertyId.substring(propertyId.indexOf(PROPERTY_NAME_AND_UUID_SEPARATOR) + 1);
    }

    public static boolean isPropertyId(final String itemId) {
        return itemId.contains(PROPERTY_NAME_AND_UUID_SEPARATOR);
    }

    public static Node getNode(final String workspaceName, final String nodePath) throws RepositoryException {
        Node node = null;
        if (StringUtils.isNotEmpty(workspaceName) && StringUtils.isNotEmpty(nodePath)) {
            node = MgnlContext.getJCRSession(workspaceName).getNode(nodePath);
        }
        return node;
    }

    public static String getUuid(final String workspaceName, final String pathOfNode) throws RepositoryException {
        return getNode(workspaceName, pathOfNode).getIdentifier();
    }

    /**
     * Get uuid of the desired node or null in case it cannot be found.
     */
    public static String getUuidOrNull(final String workspaceName, final String nodePath) {
        String uuid = null;
        try {
            uuid = getUuid(workspaceName, nodePath);
        } catch (PathNotFoundException p) {
            log.debug("Workspace {} does not contain node with path {}", workspaceName, nodePath);
        } catch (RepositoryException e) {
            log.error("Could not determine uuid for node with path " + nodePath + " in workspace " + workspaceName, e);
        }
        return uuid;
    }

    public static String getItemId(final Item jcrItem) throws RepositoryException {
        return jcrItem.isNode() ? ((Node) jcrItem).getIdentifier() : jcrItem.getParent().getIdentifier() + PROPERTY_NAME_AND_UUID_SEPARATOR + jcrItem.getName();
    }

    public static Item getJcrItem(final String workspaceName, final String itemId) throws RepositoryException {
        if (itemId == null) {
            return null;
        }
        final String nodeId = getNodeUuidFrom(itemId);

        final Node node = MgnlContext.getJCRSession(workspaceName).getNodeByIdentifier(nodeId);
        if (node == null || !isPropertyId(itemId)) {
            return node;
        }

        final String propertyName = getPropertyName(itemId);
        if (node.hasProperty(propertyName)) {
            return node.getProperty(propertyName);
        }
        return null;
    }

}
