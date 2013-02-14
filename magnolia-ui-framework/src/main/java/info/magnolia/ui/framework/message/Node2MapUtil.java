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
package info.magnolia.ui.framework.message;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.Components;

import java.util.Map;

import javax.jcr.Node;
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
        if (node.hasNodes() || node.hasProperties()) {
            throw new IllegalArgumentException("Node must be empty (no properties nor subnodes).");
        }
        // ok, if map is not empty, transform
        if (map != null && !map.isEmpty()) {
            for (String key : map.keySet()) {
                Object value = map.get(key);
                try {
                    // try to set as a primitive value
                    PropertyUtil.setProperty(node, key, value);
                } catch (IllegalArgumentException iae) {
                    // on error, check whether the value is a non-empty Map
                    boolean rethrow = true;
                    if (value instanceof Map && !((Map) value).isEmpty()) {
                        // check, whether the Map is Map<String,Object>
                        Object innerKey = ((Map) value).keySet().iterator().next();
                        if (innerKey instanceof String) {
                            // ok, create subnode
                            rethrow = false;
                            Node subnode = node.addNode(key);
                            subnode = Node2MapUtil.map2node(subnode, (Map<String, Object>) value);
                        }
                    }
                    if (rethrow) {
                        throw new IllegalArgumentException("Value is not a primitive value nor Map<String,Object>.", iae);
                    }
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

}
