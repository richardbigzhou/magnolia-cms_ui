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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Sub Nodes implementation of {@link info.magnolia.ui.form.field.transformer.Transformer} storing and retrieving properties (as {@link PropertysetItem}) displayed in MultiField.<br>
 * Storage strategy: <br>
 * - root node (relatedFormItem)<br>
 * -- child node (node name is the name of the related property)<br>
 * --- property1 (store the first value of the MultiField)<br>
 * --- property2 (store the second value of the MultiField)<br>
 * --- ...<br>
 */
public class MultiValueChildNodeTransformer extends BasicTransformer<PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(MultiValueChildNodeTransformer.class);

    private String childNodeType = NodeTypes.ContentNode.NAME;

    /**
     * @deprecated since 5.4.2 - use {@link #MultiValueChildNodeTransformer(Item, ConfiguredFieldDefinition, Class)} instead.
     */
    @Deprecated
    public MultiValueChildNodeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        this(relatedFormItem, definition, type, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Inject
    public MultiValueChildNodeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, i18NAuthoringSupport);
    }

    @Override
    public PropertysetItem readFromItem() {
        // i18n support
        String childNodeName = definePropertyName();
        PropertysetItem newValues = new PropertysetItem();
        // Get the child node containing the list of properties.
        try {
            JcrNodeAdapter child = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, childNodeName);
            // Populate
            if (!(child instanceof JcrNewNodeAdapter)) {
                int pos = 0;
                // Sort id in a natural order.
                List<Object> ids = new ArrayList<Object>(child.getItemPropertyIds());
                Collections.sort(ids, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        int int1 = Integer.valueOf(((String) o1));
                        int int2 = Integer.valueOf(((String) o2));
                        return int1 - int2;
                    }
                });

                for (Object id : ids) {
                    newValues.addItemProperty(pos, child.getItemProperty(id));
                    pos += 1;
                }
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the child node of '{}'", ((JcrNodeAdapter) relatedFormItem).getNodeName());
        }
        return newValues;
    }

    @Override
    public void writeToItem(PropertysetItem newValue) {
        // i18n support
        String childNodeName = definePropertyName();
        try {
            // get the child item
            JcrNodeAdapter child = getOrCreateChildItem((JcrNodeAdapter) relatedFormItem, childNodeName);
            // Remove all old properties
            for (Object id : child.getItemPropertyIds()) {
                if (newValue.getItemProperty(Integer.valueOf((String) id)) == null) {
                    child.removeItemProperty(id);
                }
            }
            // add all the new properties
            if (newValue != null) {
                Iterator<?> it = newValue.getItemPropertyIds().iterator();
                while (it.hasNext()) {
                    Object id = it.next();
                    child.addItemProperty(id.toString(), newValue.getItemProperty(id));
                }
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the child node of '{}'", NodeUtil.getName(((JcrNodeAdapter) relatedFormItem).getJcrItem()));
        }
    }

    /**
     * Return the child item containing the properties (displayed in the multiField).
     */
    private JcrNodeAdapter getOrCreateChildItem(JcrNodeAdapter parent, String childNodeName) throws RepositoryException {

        JcrNodeAdapter child = null;
        Node rootNode = parent.getJcrItem();
        if (rootNode.hasNode(childNodeName)) {
            child = new JcrNodeAdapter(rootNode.getNode(childNodeName));
            Node childNode = new JCRMgnlPropertiesFilteringNodeWrapper(rootNode.getNode(childNodeName));
            PropertyIterator iterator = childNode.getProperties();
            while (iterator.hasNext()) {
                // Make sure we populate the adapter with existing JCR properties.
                child.getItemProperty(iterator.nextProperty().getName());
            }
        } else {
            child = new JcrNewNodeAdapter(rootNode, childNodeType, childNodeName);
        }
        parent.addChild(child);
        return child;
    }



}
