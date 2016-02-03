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
package info.magnolia.ui.vaadin.form;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DescriptionVisibilityEvent;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import java.util.Collection;
import java.util.Locale;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Owns a Form and Dialog and connects them.
 */
public class ItemFormView implements FormView {

    private AbstractSelect languageSelector;

    private BaseDialog dialog;

    private Form form;

    public ItemFormView() {

        form = new Form();

        dialog = new DialogContainingForm();
        dialog.setContent(form);

        dialog.addDescriptionVisibilityHandler(new BaseDialog.DescriptionVisibilityEvent.Handler() {

            @Override
            public void onDescriptionVisibilityChanged(DescriptionVisibilityEvent event) {
                form.setDescriptionVisibility(event.isVisible());
            }
        });
    }

    @Override
    public Component asVaadinComponent() {
        return dialog.asVaadinComponent();
    }

    @Override
    public Item getItemDataSource() {

        return form.getItemDataSource();
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        form.setItemDataSource(newDataSource);
    }

    @Override
    public void addField(Field<?> field) {
        form.addField(field);
    }

    @Override
    public void setDescriptionVisibility(boolean isVisible) {
        form.setDescriptionVisibility(isVisible);

    }

    @Override
    public void addAction(String actionName, String actionLabel, DialogActionListener callback) {
        dialog.addAction(actionName, actionLabel, callback);
    }

    @Override
    public void setFormDescription(String description) {
        dialog.setDialogDescription(description);

    }

    @Override
    public void setCaption(String caption) {
        dialog.setCaption(caption);
    }

    @Override
    public void addFormSection(String tabName, FormSection inputFields) {
        form.addFormSection(tabName, inputFields);
    }

    @Override
    public void showValidation(boolean isVisible) {
        form.showValidation(isVisible);

    }

    @Override
    public void setShowAllEnabled(boolean enabled) {
        form.setShowAllEnabled(enabled);

    }

    @Override
    public boolean isValid() {
        return form.isValid();
    }

    @Override
    public Collection<Field<?>> getFields() {
        return form.getFields();
    }

    @Override
    public void setCurrentLocale(Locale locale) {
        this.languageSelector.setValue(locale);
    }

    @Override
    public void setLocaleSelector(AbstractSelect languageChooser) {
        this.languageSelector = languageChooser;
        languageSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                I18NAuthoringSupport i18NAuthoringSupport = Components.getComponent(I18NAuthoringSupport.class);
                if (i18NAuthoringSupport != null) {
                    i18NAuthoringSupport.i18nize(form, (Locale) event.getProperty().getValue());
                }
            }
        });
        dialog.setFooterToolbar(languageSelector);
    }

}
