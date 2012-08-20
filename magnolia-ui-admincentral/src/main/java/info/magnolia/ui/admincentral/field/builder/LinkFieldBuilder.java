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

import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.field.TextAndButtonField;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.LinkFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.widget.dialog.MagnoloaDialogPresenter;
import info.magnolia.ui.widget.dialog.MagnoloaDialogPresenter.Presenter;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;


import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a LinkField field based on a field definition.
 */
public class LinkFieldBuilder extends AbstractFieldBuilder<LinkFieldDefinition> {

    TextAndButtonField textButton;
    DialogPresenterFactory dialogPresenterFactory;

    @Inject
    public LinkFieldBuilder(LinkFieldDefinition definition, Item relatedFieldItem, DialogPresenterFactory dialogPresenterFactory ) {
        super(definition, relatedFieldItem);
        this.dialogPresenterFactory = dialogPresenterFactory;
    }

    @Override
    protected Field buildField() {
        textButton = new TextAndButtonField();
        Button selectButton = textButton.getSelectButton();
        // Set Button Caption
        selectButton.setCaption(getMessage(definition.getButtonLabel()));
        // Set Button Listener
        if(StringUtils.isNotBlank(definition.getDialogName())) {
            selectButton.addListener(createButtonClickListener(definition.getDialogName()));
        } else {
            selectButton.setCaption("No Select Menu Defined");
        }

        return textButton;
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }

    /**
     * Create the Button click Listener.
     * On click: Create a Dialog and Initialize callBack handling.
     */
    private Button.ClickListener createButtonClickListener(final String dialogName) {
        Button.ClickListener res = new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                final Presenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
                final PropertysetItem item = new PropertysetItem();
                Property property = DefaultPropertyUtil.newDefaultProperty("transiantPorps", null, (String)textButton.getTextField().getValue());
                item.addItemProperty("transiantPorps", property);

                dialogPresenter.start(item, new MagnoloaDialogPresenter.Presenter.CallBack() {

                    @Override
                    public void onSuccess(String actionName) {
                        Property p = item.getItemProperty("transiantPorps");
                        textButton.setValue(p.getValue());
                        textButton.getWindow().showNotification("Got following value from Sub Window " +p.getValue(), Notification.TYPE_HUMANIZED_MESSAGE);
                        dialogPresenter.closeDialog();
                    }

                    @Override
                    public void onCancel() {
                        textButton.getWindow().showNotification("Cancel from Sub Window", Notification.TYPE_HUMANIZED_MESSAGE);
                        dialogPresenter.closeDialog();
                    }
                });
            }
        };
        return res;
    }
}
