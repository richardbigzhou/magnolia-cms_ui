/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.workbench.column.definition;

import info.magnolia.ui.workbench.column.AbstractColumnFormatter;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Table;

/**
 * Definition of a simple column formatter displaying primary node types for nodes and property type for properties.
 */
public class NodeTypeColumnDefinition extends AbstractColumnDefinition {

    private static final Logger log = LoggerFactory.getLogger(NodeTypeColumnFormatter.class);

    public NodeTypeColumnDefinition() {
        setFormatterClass(NodeTypeColumnFormatter.class);
    }

    /**
     * Simple column formatter displaying primary node types for nodes and property type for properties.
     */
    public static class NodeTypeColumnFormatter extends AbstractColumnFormatter<NodeTypeColumnDefinition> implements ColumnFormatter {

        @Inject
        public NodeTypeColumnFormatter(NodeTypeColumnDefinition definition) {
            super(definition);
        }

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            final Item jcrItem = getJcrItem(source, itemId);
            try {
                if (jcrItem == null) {
                    return StringUtils.EMPTY;
                } else if (jcrItem.isNode()) {
                    Node node = (Node) jcrItem;
                    return node.getPrimaryNodeType().getName();
                } else {
                    Property property = (Property) jcrItem;
                    return PropertyType.nameFromValue(property.getType());
                }
            } catch (RepositoryException e) {
                log.warn("Unable to get the displayed node type for the {}", itemId, e);
                return StringUtils.EMPTY;
            }
        }

    }
}
