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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * A base custom field comprising a text field and a button placed to its immediate right.
 * A {@link PropertyTranslator} can be set in order to have a different display and property stored.
 * For example, display can be the Item path and value stored is the identifier of the Item.
 */
public class LinkField extends CustomField<String> {
    // Define layout and component
    private final VerticalLayout rootLayout = new VerticalLayout();
    private final HorizontalLayout linkLayout = new HorizontalLayout();
    private final TextField textField = new TextField();
    private final Button selectButton = new NativeButton();
    private Component contentPreview;
    private String buttonCaptionNew;
    private String buttonCaptionOther;
    private boolean isFieldEditable;

    public LinkField() {
        setImmediate(true);
    }

    /**
     * @deprecated since 5.5, use {@link #LinkField()} instead, none of arguments are required.
     */
    @Deprecated
    public LinkField(LinkFieldDefinition linkFieldDefinition, ComponentProvider componentProvider) {
        this();
    }

    @Deprecated
    public LinkField(LinkFieldDefinition linkFieldDefinition, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        this();
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
        setButtonCaption(StringUtils.EMPTY);
        rootLayout.addComponent(linkLayout);

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
        if (!isFieldEditable && StringUtils.isNotBlank(currentValue)) {
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
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        textField.setPropertyDataSource(newDataSource);
        textField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
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

    public void setContentPreview(Component contentPreviewComponent) {
        if (contentPreview != null) {
            rootLayout.removeComponent(contentPreview);
        }
        contentPreviewComponent.setVisible(StringUtils.isNotBlank(textField.getValue()));
        rootLayout.addComponentAsFirst(contentPreviewComponent);
        contentPreview = contentPreviewComponent;
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

    public void setTextFieldConverter(Converter textFieldConverter) {
        textField.setConverter(textFieldConverter);
    }

    public void setFieldEditable(boolean isFieldEditable) {
        this.isFieldEditable = isFieldEditable;
    }
}
