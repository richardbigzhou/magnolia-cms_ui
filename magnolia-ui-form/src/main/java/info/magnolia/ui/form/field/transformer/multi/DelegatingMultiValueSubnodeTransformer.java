/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * This delegating {@link info.magnolia.ui.form.field.transformer.Transformer Transformer} is dedicated to the {@link info.magnolia.ui.form.field.MultiField MultiField};
 * it considers entries as child nodes of a sub-node (named after the field) and delegates property handling to their respective sub-fields.
 * <p>
 * The storage strategy is that of the {@link info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodePropertiesTransformer MultiValueSubChildrenNodePropertiesTransformer}:
 * <ul>
 * <li>rootItem (relatedFormItem)
 * <ul>
 * <li>subNode (single sub-node for MultiField entries)<br>
 * <ul>
 * <li>0 (first entry of the MultiField)<br>
 * <li>1 (second entry of the MultiField)<br>
 * <li>...
 * </ul>
 * </ul>
 * </ul>
 */
public class DelegatingMultiValueSubnodeTransformer extends DelegatingMultiValueFieldTransformer {

    private static final Logger log = LoggerFactory.getLogger(MultiValueSubChildrenNodeTransformer.class);

    private JcrNodeAdapter subNode;

    public DelegatingMultiValueSubnodeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, i18NAuthoringSupport);
    }

    /**
     * Overridden to keep only the digit part in child-node names.
     */
    @Override
    protected String getSubItemBaseName() {
        return "";
    }

    /**
     * Overridden to get an intermediate sub-node where the multiple nodes are stored, rather than directly under the root node.
     * <p>
     * The sub-node is created if it doesn't exist, and is named after the multi-value field definition.
     */
    @Override
    protected JcrNodeAdapter getRootItem() {
        if (subNode == null) {
            JcrNodeAdapter rootItem = super.getRootItem();
            Node rootJcrItem = rootItem.getJcrItem();
            try {
                if (rootJcrItem.hasNode(definition.getName())) {
                    subNode = new JcrNodeAdapter(rootJcrItem.getNode(definition.getName()));
                } else if (rootItem.getChildren().containsKey(definition.getName())) {
                    // Initialize an intermediate sub-node when the child item has been create already but hasn't persisted yet.
                    Object childItem = rootItem.getChildren().get(definition.getName());
                    if (childItem instanceof JcrNodeAdapter) {
                        subNode = (JcrNodeAdapter) childItem;
                    }
                } else {
                    subNode = new JcrNewNodeAdapter(rootJcrItem, NodeTypes.ContentNode.NAME, definition.getName());
                }
                rootItem.addChild(subNode);
            } catch (RepositoryException e) {
                log.warn(String.format("Could not determine whether form item '%s' had a child node named '%s'", rootJcrItem, definition.getName()), e);
            }
        }
        return subNode;
    }
}
