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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.cms.core.Path;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Set;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Sub Nodes implementation of {@link info.magnolia.ui.form.field.transformer.Transformer} storing and retrieving properties (as {@link PropertysetItem}) displayed in MultiField.<br>
 * Storage strategy: <br>
 * - root node (relatedFormItem)<br>
 * -- main child node (nodeName = field name) <br>
 * --- child node 1 (used to store the first value of the MultiField as a property)<br>
 * ---- property1 (store the first value of the MultiField)<br>
 * --- child node 2 (used to store the second value of the MultiField as a property)<br>
 * ---- property2 (store the second value of the MultiField)<br>
 * ...<br>
 * Main child node : field name <br>
 * Child node name : 20 first char of the related value <br>
 * Property name : field name <br>
 */
public class MultiValueSubChildrenNodeTransformer extends MultiValueChildrenNodeTransformer {

    private static final Logger log = LoggerFactory.getLogger(MultiValueSubChildrenNodePropertiesTransformer.class);
    private String subNodeName;
    private int valueItemNameSize = 20;

    @Inject
    public MultiValueSubChildrenNodeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
        this.subNodeName = definition.getName();
    }

    @Override
    protected JcrNodeAdapter getRootItem() {
        JcrNodeAdapter res = null;
        try {
            res = getOrCreateChildNode(subNodeName, childNodeType);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve or create a sub node for the parent node {}", ((JcrNodeAdapter) relatedFormItem).getItemId());
        }
        return res;
    }

    @Override
    protected String createChildItemName(Set<String> childNames, Object value, JcrNodeAdapter rootItem) {
        return Path.getValidatedLabel(StringUtils.left(value.toString(), valueItemNameSize));
    }

    @Override
    protected void handleRootitemAndParent(JcrNodeAdapter rootItem) {
        // Attach the child item to the root item
        if (rootItem.getChildren() != null && !rootItem.getChildren().isEmpty()) {
            ((JcrNodeAdapter) relatedFormItem).addChild(rootItem);
        } else {
            ((JcrNodeAdapter) relatedFormItem).removeChild(rootItem);
        }
    }

    /**
     * Return a null predicate.
     */
    @Override
    protected Predicate createPredicateToEvaluateChildNode() {
        return null;
    }

}

