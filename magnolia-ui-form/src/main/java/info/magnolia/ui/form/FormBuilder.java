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
package info.magnolia.ui.form;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.vaadin.form.FormView;

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
     * @return FormView populated with values from FormDefinition and Item.
     */
    public FormView buildForm(FormDefinition formDefinition, Item item, FormItem parent) {

        FormView view = componentProvider.newInstance(FormView.class);

        final Form form = new Form(formDefinition);
        form.setParent(parent);
        view.setItemDataSource(item);

        final String description = formDefinition.getDescription();
        final String label = formDefinition.getLabel();
        final String basename = formDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            view.setFormDescription(i18nDescription);
        }

        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            view.setCaption(i18nLabel);
        }

        boolean hasI18NAwareFields = false;
        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            FormTab tab = new FormTab(tabDefinition);
            tab.setParent(form);
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                // FIXME MGNLUI-829 should introduce a better handling for this
                // case.
                if (fieldDefinition.getClass().equals(ConfiguredFieldDefinition.class)) {
                    continue;
                }
                hasI18NAwareFields |= fieldDefinition.isI18n();
                final FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
                formField.setComponentProvider(componentProvider);
                formField.setI18nContentSupport(i18nContentSupport);
                formField.setParent(tab);
                final Field<?> field = formField.createField();
                if (field instanceof AbstractComponent) {
                    ((AbstractComponent) field).setImmediate(true);
                }
                tab.addField(field);
                if (StringUtils.isNotBlank(fieldDefinition.getDescription())) {
                    tab.setComponentHelpDescription(field, fieldDefinition.getDescription());
                }
                view.addField(field);
            }
            view.addFormSection(tab.getMessage(tabDefinition.getLabel()), tab.getContainer());
        }
        view.setShowAllEnabled(formDefinition.getTabs().size() > 1);
        if (hasI18NAwareFields) {
            final AbstractSelect languageChoser = i18NAuthoringSupport.getLanguageChooser();
            if (languageChoser != null) {
                view.setLocaleSelector(languageChoser);
                view.setCurrentLocale(i18nContentSupport.getFallbackLocale());
            }
        }
        return view;
    }

    public View buildView(FormDefinition formDefinition, Item item) {

        final CssLayout view = new CssLayout();
        view.setSizeFull();


        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {

                if (fieldDefinition.getClass().equals(ConfiguredFieldDefinition.class)) {
                    continue;
                }

                final FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
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

}
