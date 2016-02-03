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
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * A base custom field creating a List of {@list LinkField}, configuring an Add/Remove Buttons. <br>
 * Note that the DataSource Property is of type {@link info.magnolia.ui.form.field.property.MultiProperty}.
 */
public class MultiLinkField extends CustomField<List> {

    private final Button addButton = new NativeButton();
    private final MultiLinkFieldDefinition definition;

    private String buttonCaptionAdd;
    private String buttonCaptionNew;
    private String buttonCaptionOther;

    private final AppController appController;
    private final UiContext uiContext;
    private final ComponentProvider componentProvider;
    protected VerticalLayout root;

    @Inject
    public MultiLinkField(MultiLinkFieldDefinition definition, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        this.definition = definition;
        this.appController = appController;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
    }

    /**
     * Use {@link #MultiLinkField(info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition, info.magnolia.ui.api.app.AppController, info.magnolia.ui.api.context.UiContext, info.magnolia.objectfactory.ComponentProvider)}.
     *
     * @deprecated since 5.0.2
     */
    @Deprecated
    public MultiLinkField(MultiLinkFieldDefinition definition, AppController appController, SubAppContext subAppContext, ComponentProvider componentProvider) {
        this(definition, appController, (UiContext) subAppContext, componentProvider);
    }

    /**
     * Initialize the basic component.<br>
     * - Root layout (contains all fields)<br>
     * - Add button (add a single linkField)<br>
     * - Add linkField for already stored values (initFields)
     */
    @Override
    protected Component initContent() {
        addStyleName("linkfield");
        root = new VerticalLayout();
        root.setSizeUndefined();
        // Initialize Existing field
        initFields();
        // Add addButton
        addButton.setCaption(buttonCaptionAdd);
        addButton.addStyleName("magnoliabutton");
        addButton.addClickListener(addButtonClickListener());
        root.addComponent(addButton);

        return root;
    }

    /**
     * Create a button listener bound to the add Button.
     */
    private Button.ClickListener addButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                root.addComponent(createEntryComponent(""), root.getComponentCount() - 1);
            };
        };
    }

    /**
     * Listener used to update the Data source property.
     */
    private Property.ValueChangeListener selectionListener = new ValueChangeListener() {
        @Override
        public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
            List<String> currentValues = getCurrentValues(root);
            setValue(currentValues);
        }
    };

    /**
     * Initialize the LinkField's based on the already stored values.
     */
    private void initFields() {
        List<String> newValue = (List<String>) getPropertyDataSource().getValue();
        List<String> currentValues = getCurrentValues(root);
        Iterator<String> it = newValue.iterator();
        while (it.hasNext()) {
            String entry = it.next();
            if (!currentValues.contains(entry)) {
                root.addComponent(createEntryComponent(entry));
            }
        }
    };

    /**
     * Retrieve the Values stored into the text field.
     */
    private List<String> getCurrentValues(HasComponents root) {
        Iterator<Component> it = root.iterator();
        List<String> newValue = new ArrayList<String>();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof AbstractField) {
                newValue.add(String.valueOf(((AbstractField<?>) c).getConvertedValue()));
            } else if (c instanceof HasComponents) {
                newValue.addAll(getCurrentValues((HasComponents) c));
            }
        }
        return newValue;
    }

    /**
     * Create a single element.<br>
     * This single element is composed of:<br>
     * - a linkField <br>
     * - a remove Button<br>
     */
    private Component createEntryComponent(String entry) {
        HorizontalLayout layout = new HorizontalLayout();
        // Create a single LinkFild and set DataSource and ValueChangeListener.
        LinkField linkField = new LinkField(definition, appController, uiContext, componentProvider);
        linkField.setButtonCaptionNew(buttonCaptionNew);
        linkField.setButtonCaptionOther(buttonCaptionOther);
        layout.addComponent(linkField);
        linkField.setPropertyDataSource(new ObjectProperty<String>(entry));
        linkField.addValueChangeListener(selectionListener);
        // Delete Button
        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("remove");
        deleteButton.setDescription("Remove");
        deleteButton.addClickListener(removeButtonClickListener(layout));
        layout.addComponent(deleteButton);

        return layout;
    }

    /**
     * Create a button listener bound to the delete Button.
     */
    private Button.ClickListener removeButtonClickListener(final HorizontalLayout layout) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                root.removeComponent(layout);
                setValue(getCurrentValues(root));
            };
        };
    }

    @Override
    public Class<? extends List> getType() {
        return List.class;
    }

    /**
     * Caption section.
     */
    public void setButtonCaptionAdd(String buttonCaptionAdd) {
        this.buttonCaptionAdd = buttonCaptionAdd;
    }

    public void setButtonCaptionNew(String buttonCaptionNew) {
        this.buttonCaptionNew = buttonCaptionNew;
    }

    public void setButtonCaptionOther(String buttonCaptionOther) {
        this.buttonCaptionOther = buttonCaptionOther;
    }
}
