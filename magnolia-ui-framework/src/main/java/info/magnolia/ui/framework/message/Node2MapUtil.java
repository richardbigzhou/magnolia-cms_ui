/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework.message;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Utility class to allow storing Map content to Node.
 * 
 */
public class Node2MapUtil {

    /**
     * Stores the content of <code>map</code> to the <code>node</code>. The
     * primitive values are stored as properties and
     * <code>Map&lt;String,Object&gt;</code> values are stored as a subnode. Any
     * other type will cause the IllegalArgumentException to be thrown. See the
     * {@link info.magnolia.jcr.util.PropertyUtil} for more details on how the
     * values are stored.
     */
    public static Node map2node(Node node, Map<String, Object> map) throws IllegalArgumentException, RepositoryException {
        // sanity checks
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null.");
        }
        // remove any "leftovers"
        if (node.hasNodes() || node.hasProperties()) {
            emptyNode(node);
        }
        // ok, if map is not empty, transform
        if (map != null && !map.isEmpty()) {
            for (String key : map.keySet()) {
                Object value = map.get(key);
                if (value instanceof Map) {
                    // if value is Map, then create subnode
                    Node subnode = node.addNode(key);
                    // and recursive call
                    Node2MapUtil.map2node(subnode, (Map<String, Object>) value);
                } else {
                    // try to set as a primitive value
                    PropertyUtil.setProperty(node, key, value);
                }
            }
        }
        return node;
    }

    /**
     * Reads the <code>node</code> properties and subnodes, and creates a
     * corresponding {@link java.util.Map} object, where the keys are the
     * property/subnode names, and values are the property values, resp.
     * <code>Map&lt;String, Object&gt;</code> for subnodes. Uses
     * {@link info.magnolia.jcr.node2bean.Node2BeanProcessor} to do the work.
     */
    public static Map<String, Object> node2map(Node node) throws Node2BeanException, RepositoryException {
        return (Map<String, Object>) Components.getComponent(Node2BeanProcessor.class).toBean(node);
    }

    private static void emptyNode(Node node) throws RepositoryException {
        for (Node child : NodeUtil.getNodes(node)) {
            child.remove();
        }
        PropertyIterator pi = node.getProperties();
        List<Property> list = new ArrayList<Property>();
        while (pi.hasNext()) {
            // getting ConcurrentModificationException when trying to remove
            // properties right here;
            // so just adding them to a temporary list
            Property prop = pi.nextProperty();
            if (!prop.getName().startsWith(NodeTypes.JCR_PREFIX)) {
                list.add(prop);
            }
        }
        for (Property prop : list) {
            // and removing the properties later
            prop.remove();
        }
    }

}
