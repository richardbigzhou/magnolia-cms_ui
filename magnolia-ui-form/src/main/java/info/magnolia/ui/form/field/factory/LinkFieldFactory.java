/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.LinkField;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a LinkField field based on a field definition.
 *
 * @param <D> definition type
 */
public class LinkFieldFactory<D extends FieldDefinition> extends AbstractFieldFactory<LinkFieldDefinition, String> {
    private static final Logger log = LoggerFactory.getLogger(LinkFieldFactory.class);
    public static final String PATH_PROPERTY_NAME = "transientPathProperty";

    private LinkField linkField;

    private final AppController appController;
    private final UiContext uiContext;
    private ComponentProvider componentProvider;

    @Inject
    public LinkFieldFactory(LinkFieldDefinition definition, Item relatedFieldItem, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.appController = appController;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
    }

    @Override
    public void setComponentProvider(ComponentProvider componentProvider) {
        super.setComponentProvider(componentProvider);
        this.componentProvider = componentProvider;
    }

    @Override
    protected Field<String> createFieldComponent() {
        linkField = new LinkField(definition, componentProvider);
        // Set Caption
        linkField.setButtonCaptionNew(getMessage(definition.getButtonSelectNewLabel()));
        linkField.setButtonCaptionOther(getMessage(definition.getButtonSelectOtherLabel()));
        // Add a callback listener on the select button
        linkField.getSelectButton().addClickListener(createButtonClickListener());
        return linkField;
    }

    /**
     * Create the Button click Listener. On click: Create a Dialog and
     * Initialize callback handling.
     */
    private Button.ClickListener createButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                ChooseDialogCallback callback = createChooseDialogCallback();
                String value = linkField.getTextField().getValue();
                if (StringUtils.isNotBlank(definition.getTargetTreeRootPath())) {
                    appController.openChooseDialog(definition.getAppName(), uiContext, definition.getTargetTreeRootPath(), value, callback);
                } else {
                    appController.openChooseDialog(definition.getAppName(), uiContext, value, callback);
                }
            }
        };
    }

    /**
     * @return specific {@link ChooseDialogCallback} implementation used to process the selected value.
     */
    protected ChooseDialogCallback createChooseDialogCallback() {
        return new ChooseDialogCallback() {

            @Override
            public void onItemChosen(String actionName, final Object chosenValue) {
                String newValue = null;
                if (chosenValue instanceof JcrItemId) {
                    String propertyName = definition.getTargetPropertyToPopulate();
                    try {
                        javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem((JcrItemId) chosenValue);
                        if (jcrItem.isNode()) {
                            final Node selected = (Node) jcrItem;
                            boolean isPropertyExisting = StringUtils.isNotBlank(propertyName) && selected.hasProperty(propertyName);
                            newValue = isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath();
                        }
                    } catch (RepositoryException e) {
                        log.error("Not able to access the configured property. Value will not be set.", e);
                    }
                } else {
                    newValue = String.valueOf(chosenValue);
                }
                linkField.setValue(newValue);
            }

            @Override
            public void onCancel() {
            }
        };
    }

}
