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
package info.magnolia.ui.form.field.property;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAwareProperty;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Base Handler exposing useful methods for JCR Item properties. <br>
 */
public class BaseHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseHandler.class);
    protected Item parent;
    protected final ConfiguredFieldDefinition definition;
    protected final ComponentProvider componentProvider;

    @Inject
    public BaseHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        this.definition = definition;
        this.parent = parent;
        this.componentProvider = componentProvider;
    }

    /**
     * If the desired property (propertyName) already exist in the JcrNodeAdapter, return this property<br>
     * else create a new {@link Property}.
     * 
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    protected <T> Property<T> getOrCreateProperty(Class<T> type, String defaultValueString, T defaultValue) {

        if (this.definition.isI18n()) {
            I18NAwareProperty<T> property = componentProvider.newInstance(I18NAwareProperty.class, this.definition.getName(), type, parent);
            property.setDefaultValue((T) defaultValue);
            return property;

        } else {
            Property<T> property = parent.getItemProperty(this.definition.getName());
            if (property == null) {
                if (defaultValue != null) {
                    property = new DefaultProperty<T>(defaultValue);
                } else {
                    property = DefaultPropertyUtil.newDefaultProperty(type, defaultValueString);
                }
                parent.addItemProperty(this.definition.getName(), property);
            }
            return property;
        }
    }

    /**
     * Retrieve or create a child node as {@link JcrNodeAdapter}.
     */
    protected JcrNodeAdapter getOrCreateChildNode(String chieldNodeName, String chieldNodeType) throws RepositoryException {
        JcrNodeAdapter child = null;
        if (!(parent instanceof JcrNodeAdapter)) {
            log.warn("Try to retrieve a Jcr Item from a Non Jcr Item Adapter. Will retrun null");
            return null;
        }
        Node node = ((JcrNodeAdapter) parent).getJcrItem();
        if (node.hasNode(chieldNodeName) && !(parent instanceof JcrNewNodeAdapter)) {
            child = new JcrNodeAdapter(node.getNode(chieldNodeName));
            child.setParent(((JcrNodeAdapter) parent));
        } else {
            child = new JcrNewNodeAdapter(node, NodeTypes.Content.NAME, chieldNodeName);
            child.setParent(((JcrNodeAdapter) parent));
        }
        return child;
    }

}
