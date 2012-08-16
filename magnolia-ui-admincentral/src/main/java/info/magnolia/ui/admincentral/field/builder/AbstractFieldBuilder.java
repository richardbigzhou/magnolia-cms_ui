/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.builder;

import info.magnolia.ui.admincentral.dialog.AbstractDialogItem;
import info.magnolia.ui.admincentral.field.FieldBuilder;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Abstract DialogField implementations, initializes common attributes and binds Vaadin {@link Field} instances created
 * by subclasses to the {@link Property} they will be reading and writing to.
 *
 * @param <D> definition type
 */
public abstract class AbstractFieldBuilder<D extends FieldDefinition> extends AbstractDialogItem implements FieldBuilder {

    public static final String FIELD_STYLE_NAME = "textfield";

    protected Item item;
    protected Field field;
    protected D definition;
    private String styleName;

    public AbstractFieldBuilder(D definition, Item relatedFieldItem) {
        this.definition = definition;
        this.item = relatedFieldItem;
    }

    @Override
    public Field getField() {
        if (field == null) {

            // Build the Vaadin field
            this.field = buildField();

            // Get and set the DataSource property
            Property property = getOrCreateProperty();
            setPropertyDataSource(property);

            // Set style
            this.field.setStyleName(getStyleName());

            //Set label
            this.field.setCaption(getMessage(getFieldDefinition().getLabel()));

            //Set SaveInfo (field property has to be updated)
            if(this.field.getPropertyDataSource()!=null) {
                ((DefaultProperty) this.field.getPropertyDataSource()).setSaveInfo(definition.getSaveInfo());
            }
        }
        return this.field;
    }

    @Override
    public D getFieldDefinition() {
        return this.definition;
    }

    /**
     * Set the DataSource of the current field.
     */
    public void setPropertyDataSource(Property property) {
        this.field.setPropertyDataSource(property);
    }

    /**
     * Implemented by subclasses to create and initialize the Vaadin Field instance to use.
     */
    protected abstract Field buildField();

    /**
     * Set the default CSS style name for the current field.
     */
    protected void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    protected String getStyleName() {
        return this.styleName != null ? this.styleName : FIELD_STYLE_NAME;
    }

    /**
     * Get a property from the current Item.
     * If the property already exist, return this property.
     * If the property does not exist:
     * Create a new property based on the defined Type, default value, and saveInfo.
     */
    public Property getOrCreateProperty() {
        DefaultProperty property = (DefaultProperty) item.getItemProperty(definition.getName());
        if (property == null) {
            property = DefaultPropertyUtil.newDefaultProperty(definition.getName(), getFieldType(definition).getSimpleName(), definition.getDefaultValue());
            item.addItemProperty(definition.getName(), property);
        }
        return property;
    }

    /**
     * Return the Class field Type if define in the configuration.
     * If the Type is not defined in the configuration or not of a supported type, throws
     * a {@link IllegalArgumentException}:
     */
    protected Class<?> getFieldType(FieldDefinition fieldDefinition) {
        if (StringUtils.isNotBlank(fieldDefinition.getType())) {
            return DefaultPropertyUtil.getFieldTypeClass(fieldDefinition.getType());
        }
        return getDefaultFieldType(fieldDefinition);
    }

    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        throw new IllegalArgumentException("Unsupported type " + fieldDefinition.getClass().getName());
    }

    /**
     * Returns the field related node.
     * If field is of type JcrNewNodeAdapter then return the parent node.
     * Else get the node associated with the Vaadin item.
     */
    protected Node getRelatedNode(Item fieldRelatedItem) {
        if (fieldRelatedItem instanceof JcrNewNodeAdapter) {
            return ((JcrNewNodeAdapter) fieldRelatedItem).getParentNode();
        } else {
            return ((JcrNodeAdapter) fieldRelatedItem).getNode();
        }
    }

    @Override
    protected String getI18nBasename() {
        return definition.getI18nBasename();
    }
}