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
package info.magnolia.ui.form.field.factory;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;
import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a SwitchableField field based on a field definition.<br>
 * Switchable field has two components: <br>
 * - A select section configured based on the Options list of the definition<br>
 * - A field section configured based on the Fields list of the definition<br>
 * The link between select and fields is based on the association of: <br>
 * - The String property defined into the value property of the definition (value = date) <br>
 * and<br>
 * - The Field name defined into the Fields set (Date field named date).
 * 
 * @param <D> definition type
 */
public class SwitchableFieldFactory<D extends FieldDefinition> extends AbstractFieldFactory<SwitchableFieldDefinition, String> {
    private static final Logger log = LoggerFactory.getLogger(SwitchableFieldFactory.class);
    private FieldFactoryFactory fieldFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private ComponentProvider componentProvider;

    private HashMap<String, Field<?>> fieldMap;
    private AbstractSelect selectField;

    @Inject
    public SwitchableFieldFactory(SwitchableFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Field<String> createFieldComponent() {
        try {
            // Create the selection Field
            this.selectField = createSelectionField();

            // Create the related fieldMap
            this.fieldMap = createFieldSet();

        } catch (Exception e) {
            log.warn("Not able to create a SwitchableField");
            throw new RuntimeException(e);
        }
        return new SwitchableField(fieldMap, selectField);
    }

    /**
     * Create a RadioSelect or a NormalSelect Field based on the definition.<br>
     */
    private AbstractSelect createSelectionField() throws Exception {
        // Create the correct definition class
        SelectFieldDefinition selectDefinition = null;
        if (definition.getSelectionType().equals("radio")) {
            selectDefinition = new OptionGroupFieldDefinition();
        } else {
            selectDefinition = new SelectFieldDefinition();
        }
        // Copy options to the newly created select definition. definition
        BeanUtils.copyProperties(selectDefinition, definition);
        // Create the field
        AbstractSelect field = (AbstractSelect) createLocalField(selectDefinition);
        field.addStyleName("horizontal");
        field.setImmediate(true);

        return field;
    }

    /**
     * Create a Field Map.<br>
     * - key : Field name. Should be the same as the related select value.<br>
     * -value : Related Field. Created based on the definition coming from the Fields Definition list.
     */
    private HashMap<String, Field<?>> createFieldSet() {

        HashMap<String, Field<?>> localFieldMap = new HashMap<String, Field<?>>();
        // Iterate and create the related fields
        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            // As the definition is a singleton & as we set the name of the definition, this has to be done on a Clone
            // else this change will be propagated every time the dialog is open
            // (First call: fieldName = type+name = typename, Second dialog initialization fieldName = type+typename = typetypename)
            ConfiguredFieldDefinition fieldDefinitionClone = new Cloner().deepClone(fieldDefinition);
            String name = fieldDefinitionClone.getName();
            fieldDefinitionClone.setName(definition.getName() + fieldDefinition.getName());
            // Create the field
            Field<?> field = createLocalField(fieldDefinitionClone);
            localFieldMap.put(name, field);
        }

        return localFieldMap;
    }

    private Field<?> createLocalField(ConfiguredFieldDefinition fieldDefinition) {
        FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
        formField.setComponentProvider(componentProvider);
        formField.setI18nContentSupport(i18nContentSupport);
        Field<?> field = formField.createField();
        field.setCaption(null);
        return field;
    }

}
