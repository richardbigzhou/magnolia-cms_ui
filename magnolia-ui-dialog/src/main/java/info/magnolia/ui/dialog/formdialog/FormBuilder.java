/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.dialog.formdialog;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.form.Form;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.FormTab;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.richtext.TextAreaStretcher;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;

/**
 * Builder for forms.
 */
public class FormBuilder {

    private FieldFactoryFactory fieldFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private I18NAuthoringSupport i18NAuthoringSupport;
    private ComponentProvider componentProvider;

    @Inject
    public FormBuilder(FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport,
            I18NAuthoringSupport i18NAuthoringSupport, ComponentProvider componentProvider) {
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
    }

    /**
     * return FormView populated with values from FormDefinition and Item.
     */
    public void buildForm(FormView view, FormDefinition formDefinition, Item item, FormItem parent) {
        final String description = formDefinition.getDescription();
        final String label = formDefinition.getLabel();

        // If we remove the if blocks below, we show up the (first) generated key for this label/description (unless it is translated),
        // thus overriding the dialog's title. See MGNLUI-2207.
        // The 'container' of the form (ie a dialog) may already have set these values on the view based on its definition (dialogDefintion).
        // Only if form specifies values - then use forms values.
        if (StringUtils.isNotBlank(description) && !isMessageKey(description)) {
            view.setDescription(description);
        }

        if (StringUtils.isNotBlank(label) && !isMessageKey(label)) {
            view.setCaption(label);
        }

        buildReducedForm(formDefinition, view, item, parent);
        boolean hasI18NAwareFields = hasI18NAwareFields(formDefinition);

        if (hasI18NAwareFields) {
            final AbstractSelect languageChooser = i18NAuthoringSupport.getLanguageChooser();
            if (languageChooser != null) {
                view.setLocaleSelector(languageChooser);
                view.setCurrentLocale(i18nContentSupport.getFallbackLocale());
            }
        }
    }

    public View buildView(FormDefinition formDefinition, Item item) {

        final CssLayout view = new CssLayout();
        view.setSizeFull();

        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                final FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
                if (formField == null) {
                    continue;
                }
                formField.setComponentProvider(componentProvider);
                formField.setI18nContentSupport(i18nContentSupport);

                final View fieldView = formField.getView();

                view.addComponent(fieldView.asVaadinComponent());

            }
        }
        return new View() {
            @Override
            public Component asVaadinComponent() {
                return view;
            }
        };
    }

    public void buildReducedForm(FormDefinition formDefinition, FormViewReduced view, Item item, FormItem parent) {
        final Form form = new Form(formDefinition);
        form.setParent(parent);
        view.setItemDataSource(item);

        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            FormTab tab = new FormTab(tabDefinition);
            tab.setParent(form);
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                final FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
                if (formField == null) {
                    continue;
                }
                formField.setComponentProvider(componentProvider);
                formField.setI18nContentSupport(i18nContentSupport);
                formField.setParent(tab);
                final Field<?> field = formField.createField();
                if (field instanceof AbstractComponent) {
                    ((AbstractComponent) field).setImmediate(true);
                }
                tab.addField(field);
                final String helpDescription = fieldDefinition.getDescription();

                if (StringUtils.isNotBlank(helpDescription) && !isMessageKey(helpDescription)) {
                    tab.setComponentHelpDescription(field, helpDescription);
                }
                TextAreaStretcher.extend(field);
                view.addField(field);
            }
            view.addFormSection(tabDefinition.getLabel(), tab.getContainer());
        }
        view.setShowAllEnabled(formDefinition.getTabs().size() > 1);

    }

    private boolean hasI18NAwareFields(FormDefinition formDefinition) {
        boolean hasI18NAwareFields = false;
        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                hasI18NAwareFields |= fieldDefinition.isI18n();
            }
        }
        return hasI18NAwareFields;

    }

    /**
     * @deprecated is a hack and should not be used. See MGNLUI-2207.
     */
    @Deprecated
    private boolean isMessageKey(final String text) {
        return !text.contains(" ") && text.contains(".") && !text.endsWith(".");
    }
}
