/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.ui.admincentral.app.content.AbstractContentApp;
import info.magnolia.ui.admincentral.dialog.ChooseDialogPresenter;
import info.magnolia.ui.admincentral.dialog.ValueChosenListener;
import info.magnolia.ui.admincentral.field.TextAndButtonField;
import info.magnolia.ui.admincentral.field.translator.IdentifierToPathTranslator;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.LinkFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a LinkField field based on a field definition.
 * 
 * @param <D>
 *            definition type
 */
public class LinkFieldBuilder<D extends FieldDefinition> extends AbstractFieldBuilder<LinkFieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(LinkFieldBuilder.class);

    public static final String PATH_PROPERTY_NAME = "transientPathProperty";

    private TextAndButtonField textButton;

    private final AppController appController;

    @Inject
    public LinkFieldBuilder(LinkFieldDefinition definition, Item relatedFieldItem, AppController appController) {
        super(definition, relatedFieldItem);
        this.appController = appController;
    }

    @Override
    protected Field buildField() {
        // Create Translator if we need to store the Identifier
        IdentifierToPathTranslator translator = null;
        if (definition.isIdentifier()) {
            translator = new IdentifierToPathTranslator(definition.getWorkspace());
        }
        textButton = new TextAndButtonField(translator, getMessage(definition.getButtonSelectNewLabel()),
                getMessage(definition.getButtonSelectOtherLabel()));
        final Button selectButton = textButton.getSelectButton();

        if (StringUtils.isNotBlank(definition.getDialogName()) || StringUtils.isNotBlank(definition.getAppName())) {
            selectButton.addListener(createButtonClickListener(definition.getDialogName(), definition.getAppName()));
        } else {
            selectButton.setCaption(getMessage("field.link.select.error"));
        }
        return textButton;
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }

    /**
     * Create the Button click Listener. On click: Create a Dialog and
     * Initialize callback handling.
     */
    private Button.ClickListener createButtonClickListener(final String dialogName, final String appName) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // Get the property name to propagate.
                final String propertyName = getPropertyName();
                final App targetApp = appController.startIfNotAlreadyRunning(appName, new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appName, ""));
                if (targetApp != null) {
                    if (targetApp instanceof AbstractContentApp) {
                        final ChooseDialogPresenter<Item> pickerPresenter = ((AbstractContentApp) targetApp).openChooseDialog();
                        pickerPresenter.getView().setCaption("Select a contact");
                        pickerPresenter.addValuePickListener(new ValueChosenListener<Item>() {
                            @Override
                            public void onValueChosen(final Item pickedValue) {
                                javax.jcr.Item jcrItem = ((JcrItemAdapter) pickedValue).getJcrItem();
                                if (jcrItem.isNode()) {
                                    final Node selected = (Node) jcrItem;
                                    try {
                                        boolean isPropertyExisting = StringUtils.isNotBlank(propertyName) && 
                                                !PATH_PROPERTY_NAME.equals(propertyName) && selected.hasProperty(propertyName);
                                        textButton.setValue(isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath());
                                        
                                        if ("assets".equals(appName)) {
                                            selected.setProperty("image", "dms");
                                        }
                                    } catch (RepositoryException e) {
                                        log.error("Not able to access the configured property. Value will not be set.", e);
                                    }
                                }
                            }

                            @Override
                            public void selectionCanceled() {

                            }

                        });
                    }
                }
            }
        };
    }

    private String getCustomPropertyName() {
        return StringUtils.isEmpty(definition.getPropertyName()) ? PATH_PROPERTY_NAME : definition.getPropertyName();
    }
}
