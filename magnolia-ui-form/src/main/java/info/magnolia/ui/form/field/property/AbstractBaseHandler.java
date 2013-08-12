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
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Base Handler exposing useful methods for JCR Item properties. <br>
 * 
 * @param <T>
 */
public abstract class AbstractBaseHandler<T> implements PropertyHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractBaseHandler.class);
    protected Item parent;
    protected final ConfiguredFieldDefinition definition;
    protected final ComponentProvider componentProvider;

    protected String basePropertyName;
    protected String i18NPropertyName;
    protected Locale locale;

    @Inject
    public AbstractBaseHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        this.definition = definition;
        this.parent = parent;
        this.componentProvider = componentProvider;
        this.basePropertyName = definition.getName();
        if (hasI18NSupport()) {
            this.i18NPropertyName = this.basePropertyName;
        }
    }

    @Override
    public boolean hasI18NSupport() {
        return definition.isI18n();
    }

    /**
     * If the desired property (propertyName) already exist in the JcrNodeAdapter, return this property<br>
     * else create a new {@link Property}.
     * 
     * @param <T>
     */
    protected <T> Property<T> getOrCreateProperty(Class<T> type, String defaultValueString, T defaultValue) {
        String propertyName = this.basePropertyName;

        if (hasI18NSupport()) {
            propertyName = this.i18NPropertyName;
        }

        Property<T> property = parent.getItemProperty(propertyName);
        if (property == null) {
            if (defaultValue != null) {
                property = new DefaultProperty<T>(defaultValue);
            } else {
                property = DefaultPropertyUtil.newDefaultProperty(type, defaultValueString);
            }
            parent.addItemProperty(propertyName, property);
        }
        return property;
    }


    /**
     * Retrieve or create a child node as {@link JcrNodeAdapter}.
     */
    protected JcrNodeAdapter getOrCreateChildNode(String childNodeName, String childNodeType) throws RepositoryException {
        JcrNodeAdapter child = null;
        if (!(parent instanceof JcrNodeAdapter)) {
            log.warn("Try to retrieve a Jcr Item from a Non Jcr Item Adapter. Will retrun null");
            return null;
        }
        Node node = ((JcrNodeAdapter) parent).getJcrItem();
        if (node.hasNode(childNodeName) && !(parent instanceof JcrNewNodeAdapter)) {
            child = new JcrNodeAdapter(node.getNode(childNodeName));
            child.setParent(((JcrNodeAdapter) parent));
        } else {
            child = new JcrNewNodeAdapter(node, NodeTypes.Content.NAME, childNodeName);
            child.setParent(((JcrNodeAdapter) parent));
        }
        return child;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setI18NPropertyName(String i18NPropertyName) {
        this.i18NPropertyName = i18NPropertyName;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public String getBasePropertyName() {
        return basePropertyName;
    }

}
