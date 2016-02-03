/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for item ids used in the container.
 * <p/>
 * The format is:
 * <ul>
 *     <li>for nodes &lt;node identifier&gt;
 *     <li>for properties &lt;node identifier&gt;@&lt;propertyName&gt;
 * </ul>
 */
public class JcrItemUtil {

    /**
     * String separating property name and node identifier.
     */
    public static final String PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR = "@";

    private static final Logger log = LoggerFactory.getLogger(JcrItemUtil.class);

    /**
     * @return all chars in front of #PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR - if it doesn't contain #PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR the provided itemId (then we assume it's already a nodeId)
     */
    public static String parseNodeIdentifier(final String itemId) {
        return isPropertyItemId(itemId) ? itemId.substring(0, itemId.indexOf(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR)) : itemId;
    }

    public static String parsePropertyName(final String itemId) {
        return itemId.substring(itemId.indexOf(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR) + 1);
    }

    public static boolean isPropertyItemId(final String itemId) {
        return itemId.contains(PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR);
    }

    /**
     * Returns the JCR Item represented by the given itemId or returns null if it doesn't exist.
     */
    public static Item getJcrItem(final String workspaceName, final String itemId) throws RepositoryException {
        if (itemId == null) {
            return null;
        }
        final String nodeId = parseNodeIdentifier(itemId);

        Node node;
        try {
            node = MgnlContext.getJCRSession(workspaceName).getNodeByIdentifier(nodeId);
        } catch (ItemNotFoundException e) {
            log.debug("Couldn't find item with id {} in workspace {}.", itemId, workspaceName);
            return null;
        }

        if (!isPropertyItemId(itemId)) {
            return node;
        }

        final String propertyName = parsePropertyName(itemId);
        if (node.hasProperty(propertyName)) {
            return node.getProperty(propertyName);
        }
        return null;
    }

    public static boolean itemExists(String workspaceName, String itemId) throws RepositoryException {
        return getJcrItem(workspaceName, itemId) != null;
    }

    public static String getItemId(final Item jcrItem) throws RepositoryException {
        if (jcrItem == null) {
            return null;
        }
        return jcrItem.isNode() ? ((Node) jcrItem).getIdentifier() : jcrItem.getParent().getIdentifier() + PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR + jcrItem.getName();
    }

    /**
     * Returns the itemId for a node at the given path if it exists, otherwise returns null.
     */
    public static String getItemId(final String workspaceName, final String absPath) throws RepositoryException {

        if (StringUtils.isEmpty(workspaceName) || StringUtils.isEmpty(absPath)) {
            return null;
        }

        Session session = MgnlContext.getJCRSession(workspaceName);
        if (!session.nodeExists(absPath)) {
            return null;
        }

        return getItemId(session.getNode(absPath));
    }

    public static List<Item> getJcrItems(final String workspaceName, List<String> ids) {
        // sanity check
        if (StringUtils.isBlank(workspaceName) || ids == null) {
            return null;
        }
        List<Item> items = new ArrayList<Item>();
        for (String id : ids) {
            Item item;
            try {
                item = getJcrItem(workspaceName, id);
                if (item != null) {
                    items.add(item);
                }
            } catch (RepositoryException e) {
                log.debug("Cannot find item with id [{}] in workspace [{}].", id, workspaceName);
            }
        }
        return items;
    }

    public static String getItemPath(Item item) {
        if (item == null) {
            return null;
        }
        String path = "unknown";
        try {
            if (item.isNode()) {
                path = item.getPath();
            } else {
                String parentPath = item.getParent().getPath();
                String name = item.getName();
                path = parentPath + PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR + name;
            }
        } catch (RepositoryException re) {
            log.error("Cannot get path for item: " + item);
        }
        return path;
    }

}
