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
package info.magnolia.ui.admincentral.field;

import info.magnolia.ui.form.field.LinkField;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.ItemChosenListener;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
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
    private static final Logger log = LoggerFactory.getLogger(MultiLinkField.class);

    private final Button addButton = new NativeButton();

    private final Converter<String, ?> converter;
    private final String buttonCaptionNew;
    private final String buttonCaptionAdd;
    private final String buttonCaptionOther;
    private final AppController appController;
    private final SubAppContext subAppContext;
    private String appName;
    private String dialogName;
    protected VerticalLayout root;
    private boolean allowChangesOnSelected;

    public MultiLinkField(Converter<String, ?> converter, String buttonCaptionAdd, String buttonCaptionNew, String buttonCaptionOther, AppController appController, SubAppContext subAppContext, String appName, String dialogName, boolean allowChangesOnSelected) {
        this.converter = converter;
        this.buttonCaptionNew = buttonCaptionNew;
        this.buttonCaptionOther = buttonCaptionOther;
        this.buttonCaptionAdd = buttonCaptionAdd;
        this.appController = appController;
        this.subAppContext = subAppContext;
        this.appName = appName;
        this.dialogName = dialogName;
        this.allowChangesOnSelected = allowChangesOnSelected;
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

        addButton.setCaption(buttonCaptionAdd);
        addButton.addStyleName("magnoliabutton");
        addButton.addClickListener(addButtonClickListener());

        root.addComponent(addButton);
        initFields();
        return root;
    }

    /**
     * Create a button listener bound to the add Button.
     */
    private Button.ClickListener addButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                root.addComponent(createSelectCompoment(""), root.getComponentCount() - 1);
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
                root.addComponentAsFirst(createSelectCompoment(entry));
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
    private Component createSelectCompoment(String entry) {
        HorizontalLayout layout = new HorizontalLayout();
        // Create a single LinkFild and set DataSource and ValueChangeListener.
        LinkField linkField = new LinkField(converter, buttonCaptionNew, buttonCaptionOther, allowChangesOnSelected);
        final Button selectButton = linkField.getSelectButton();
        if (StringUtils.isNotBlank(dialogName) || StringUtils.isNotBlank(appName)) {
            selectButton.addClickListener(createButtonClickListener(dialogName, appName, linkField));
        } else {
            selectButton.setCaption("No Target App Configured");
        }
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

    /**
     * Create the Button click Listener. On click: Create a Dialog and
     * Initialize callback handling.
     */
    private Button.ClickListener createButtonClickListener(final String dialogName, final String appName, final LinkField linkField) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                appController.openChooseDialog(appName, "/", subAppContext, new ItemChosenListener() {
                    @Override
                    public void onItemChosen(final Item chosenValue) {
                        String propertyName = "tmp";
                        String newValue = null;
                        if (chosenValue != null) {
                            javax.jcr.Item jcrItem = ((JcrItemAdapter) chosenValue).getJcrItem();
                            if (jcrItem.isNode()) {
                                final Node selected = (Node) jcrItem;
                                try {
                                    boolean isPropertyExisting = StringUtils.isNotBlank(propertyName) && !"transientPathProperty".equals(propertyName) && selected.hasProperty(propertyName);
                                    newValue = isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath();
                                } catch (RepositoryException e) {
                                    log.error("Not able to access the configured property. Value will not be set.", e);
                                }
                            }
                        }
                        linkField.setValue(newValue);
                    }

                    @Override
                    public void onChooseCanceled() {
                    }
                });
            }
        };
    }

    @Override
    public Class<? extends List> getType() {
        return List.class;
    }

}
