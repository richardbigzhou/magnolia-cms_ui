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
package info.magnolia.ui.form.field;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.component.ContentPreviewComponent;
import info.magnolia.ui.form.field.converter.IdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * A base custom field comprising a text field and a button placed to its immediate right.
 * A {@link PropertyTranslator} can be set in order to have a different display and property stored.
 * For example, display can be the Item path and value stored is the identifier of the Item.
 */
public class LinkField extends CustomField<String> {
    private static final Logger log = LoggerFactory.getLogger(LinkField.class);

    /**
     * Normal {@link TextField} only exposing the fireValueChange(... that is by default protected.
     */
    private final class LinkFieldTextBox extends TextField {
        @Override
        public void fireValueChange(boolean repaintIsNotNeeded) {
            super.fireValueChange(repaintIsNotNeeded);
        }
    }

    // Define layout and component
    private final VerticalLayout rootLayout = new VerticalLayout();
    private final HorizontalLayout linkLayout = new HorizontalLayout();
    private final LinkFieldTextBox textField = new LinkFieldTextBox();
    private final Button selectButton = new NativeButton();

    private final IdentifierToPathConverter converter;
    private final LinkFieldDefinition definition;
    private String buttonCaptionNew;
    private String buttonCaptionOther;

    private final AppController appController;
    private final UiContext uiContext;
    private final ComponentProvider componentProvider;

    public LinkField(LinkFieldDefinition linkFieldDefinition, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        this.definition = linkFieldDefinition;
        this.converter = definition.getIdentifierToPathConverter();
        if (this.converter != null) {
            this.converter.setWorkspaceName(definition.getTargetWorkspace());
        }
        this.appController = appController;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
        setImmediate(true);
    }

    @Override
    protected Component initContent() {
        addStyleName("linkfield");
        // Initialize root
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        // Handle Text Field
        textField.setImmediate(true);
        textField.setWidth(100, Unit.PERCENTAGE);
        textField.setNullRepresentation("");
        textField.setNullSettingAllowed(true);
        // Handle Link Layout (Text Field & Select Button)
        linkLayout.setSizeFull();
        linkLayout.addComponent(textField);
        linkLayout.setExpandRatio(textField, 1);
        linkLayout.setComponentAlignment(textField, Alignment.MIDDLE_LEFT);
        // Only Handle Select button if the Text field is not Read Only.
        if (!textField.isReadOnly()) {
            selectButton.addStyleName("magnoliabutton");
            linkLayout.addComponent(selectButton);
            linkLayout.setExpandRatio(selectButton, 0);
            linkLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);
        }
        selectButton.addClickListener(createButtonClickListener());
        setButtonCaption(StringUtils.EMPTY);
        rootLayout.addComponent(linkLayout);

        // Register the content preview if it's define.
        if (definition.getContentPreviewDefinition() != null && definition.getContentPreviewDefinition().getContentPreviewClass() != null) {
            registerContentPreview();
        }
        return rootLayout;
    }

    public TextField getTextField() {
        return this.textField;
    }

    public Button getSelectButton() {
        return this.selectButton;
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException, ConversionException {
        textField.setValue(newValue);
    }

    /**
     * Update the Link component. <br>
     * - Set text Field as read only if desired. In this case remove the add button.
     * - If it is not read only. update the button label.
     */
    private void updateComponents(String currentValue) {
        if (!definition.isFieldEditable() && StringUtils.isNotBlank(currentValue)) {
            textField.setReadOnly(true);
            if (linkLayout.getComponentIndex(selectButton) != -1) {
                linkLayout.removeComponent(selectButton);
            }
        } else {
            setButtonCaption(currentValue);
        }
    }

    /**
     * Set propertyDatasource.
     * If the translator is not null, set it as datasource.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        if (converter != null) {
            textField.setConverter(converter);
        }
        textField.setPropertyDataSource(newDataSource);
        textField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                String value = event.getProperty().getValue() != null ? event.getProperty().getValue().toString() : StringUtils.EMPTY;
                updateComponents(value);
            }
        });

        super.setPropertyDataSource(newDataSource);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Property getPropertyDataSource() {
        return textField.getPropertyDataSource();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    private void setButtonCaption(String value) {
        if (StringUtils.isNotBlank(value)) {
            selectButton.setCaption(buttonCaptionOther);
            selectButton.setDescription(buttonCaptionOther);
        } else {
            selectButton.setCaption(buttonCaptionNew);
            selectButton.setDescription(buttonCaptionNew);
        }
    }

    private void registerContentPreview() {
        final ContentPreviewComponent<?> contentPreviewComponent = Components.newInstance(definition.getContentPreviewDefinition().getContentPreviewClass(), definition.getTargetWorkspace(), componentProvider);
        textField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                String itemReference = event.getProperty().getValue().toString();
                contentPreviewComponent.onValueChange(itemReference);
            }
        });
        rootLayout.addComponentAsFirst(contentPreviewComponent);
        if (StringUtils.isNotBlank(textField.getValue())) {
            textField.fireValueChange(false);
        }
    }

    /**
     * Create the Button click Listener. On click: Create a Dialog and
     * Initialize callback handling.
     */
    private Button.ClickListener createButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                appController.openChooseDialog(definition.getAppName(), uiContext, textField.getValue(), new ChooseDialogCallback() {
                    @Override
                    public void onItemChosen(String actionName, final Item chosenValue) {
                        String propertyName = definition.getTargetPropertyToPopulate();
                        String newValue = null;
                        if (chosenValue != null && !(chosenValue instanceof NullItem)) {
                            javax.jcr.Item jcrItem = ((JcrItemAdapter) chosenValue).getJcrItem();
                            if (jcrItem.isNode()) {
                                final Node selected = (Node) jcrItem;
                                try {
                                    boolean isPropertyExisting = StringUtils.isNotBlank(propertyName) && selected.hasProperty(propertyName);
                                    newValue = isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath();
                                } catch (RepositoryException e) {
                                    log.error("Not able to access the configured property. Value will not be set.", e);
                                }
                            }
                        }
                        setValue(newValue);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        };
    }

    /**
     * Caption section.
     */
    public void setButtonCaptionNew(String buttonCaptionNew) {
        this.buttonCaptionNew = buttonCaptionNew;
    }

    public void setButtonCaptionOther(String buttonCaptionOther) {
        this.buttonCaptionOther = buttonCaptionOther;
    }

    @Override
    public boolean isValid() {
        if (this.isRequired() && StringUtils.isBlank(getValue())) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public ErrorMessage getErrorMessage() {
        if (this.isRequired() && StringUtils.isBlank(getValue())) {
            return new UserError(getRequiredError());
        } else {
            return null;
        }
    }

}
