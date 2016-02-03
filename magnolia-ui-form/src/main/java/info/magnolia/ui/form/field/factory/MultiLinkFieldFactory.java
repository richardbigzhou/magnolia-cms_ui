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
package info.magnolia.ui.form.field.factory;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.MultiLinkField;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.property.MultiProperty;
import info.magnolia.ui.form.field.property.MultiValueHandler;
import info.magnolia.ui.form.field.property.SingleValueHandler;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an multi-select field based on a field definition.<br>
 * This field builder create a {@link MultiProperty} based on the definition and set this property as <br>
 * Field property datasource.
 */
public class MultiLinkFieldFactory extends AbstractFieldFactory<MultiLinkFieldDefinition, List> {
    private static final Logger log = LoggerFactory.getLogger(MultiLinkFieldFactory.class);

    private final AppController appController;
    private final UiContext uiContext;
    private final ComponentProvider componentProvider;

    @Inject
    public MultiLinkFieldFactory(MultiLinkFieldDefinition definition, Item relatedFieldItem, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.appController = appController;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
    }

    /**
     * Use {@link #MultiLinkFieldFactory(info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition, com.vaadin.data.Item, info.magnolia.ui.api.app.AppController, info.magnolia.ui.api.context.UiContext, info.magnolia.objectfactory.ComponentProvider)}.
     *
     * @deprecated since 5.0.2
     */
    @Deprecated
    public MultiLinkFieldFactory(MultiLinkFieldDefinition definition, Item relatedFieldItem, AppController appController, SubAppContext subAppContext, ComponentProvider componentProvider) {
        this(definition, relatedFieldItem, appController, (UiContext) subAppContext, componentProvider);
    }

    @Override
    protected Field<List> createFieldComponent() {
        MultiLinkField field = new MultiLinkField(definition, appController, uiContext, componentProvider);
        field.setButtonCaptionAdd(getMessage(definition.getButtonSelectAddLabel()));
        field.setButtonCaptionNew(getMessage(definition.getButtonSelectNewLabel()));
        field.setButtonCaptionOther(getMessage(definition.getButtonSelectOtherLabel()));
        return field;
    }

    @Override
    protected Property<?> getOrCreateProperty() {
        Class<? extends MultiValueHandler> multiDelegate = null;
        String itemName = definition.getName();
        // Get configured MultiValueHandler class
        if (definition.getSaveModeType() != null && definition.getSaveModeType().getMultiValueHandlerClass() != null) {
            multiDelegate = definition.getSaveModeType().getMultiValueHandlerClass();
        } else {
            multiDelegate = SingleValueHandler.class;
            log.warn("No SaveModeType defined for this Multiselect Field definition. Default one will be taken: '{}'", SingleValueHandler.class.getSimpleName());
        }
        MultiValueHandler multiPropertyDelegate = this.componentProvider.newInstance(multiDelegate, item, itemName);
        MultiProperty property = new MultiProperty(multiPropertyDelegate);
        return property;
    }

}
