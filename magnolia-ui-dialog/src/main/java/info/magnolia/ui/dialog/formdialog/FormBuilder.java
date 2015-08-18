/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.form.Form;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.FormTab;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.richtext.TextAreaStretcher;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;

/**
 * Builder for forms.
 */
public class FormBuilder {

    private static final Logger log = LoggerFactory.getLogger(FormBuilder.class);

    private final FieldFactoryFactory fieldFactoryFactory;
    private final I18NAuthoringSupport i18nAuthoringSupport;
    private final UiContext uiContext;
    private final ComponentProvider componentProvider;

    @Inject
    public FormBuilder(FieldFactoryFactory fieldFactoryFactory, I18NAuthoringSupport i18nAuthoringSupport, UiContext uiContext, ComponentProvider componentProvider) {
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.i18nAuthoringSupport = i18nAuthoringSupport;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
    }

    /**
     * @deprecated since 5.3.9 - use {@link #FormBuilder(FieldFactoryFactory, I18NAuthoringSupport, UiContext, ComponentProvider)} instead.
     */
    @Deprecated
    public FormBuilder(FieldFactoryFactory fieldFactoryFactory, I18NAuthoringSupport i18nAuthoringSupport, ComponentProvider componentProvider) {
        this(fieldFactoryFactory, i18nAuthoringSupport, componentProvider.getComponent(UiContext.class), componentProvider);
    }

    /**
     * @deprecated since 5.3.4 - use {@link #FormBuilder(FieldFactoryFactory, I18NAuthoringSupport, UiContext, ComponentProvider)} instead.
     */
    @Deprecated
    public FormBuilder(FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, I18NAuthoringSupport i18nAuthoringSupport, ComponentProvider componentProvider) {
        this(fieldFactoryFactory, i18nAuthoringSupport, componentProvider.getComponent(UiContext.class), componentProvider);
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

        if (hasI18nAwareFields(formDefinition)) {
                List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(item);
                view.setAvailableLocales(locales);
                // As of 5.3.9 only subapp context supports tracking current authoring locale, we may expand that to other UiContexts in the future if needed.
                if (uiContext instanceof SubAppContext && ((SubAppContext) uiContext).getAuthoringLocale() != null) {
                    view.setCurrentLocale(((SubAppContext) uiContext).getAuthoringLocale());
                } else {
                    view.setCurrentLocale(i18nAuthoringSupport.getDefaultLocale(item));
                }
        }
    }

    public View buildView(FormDefinition formDefinition, Item item) {

        final CssLayout view = new CssLayout();
        view.setSizeFull();

        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            List<FieldDefinition> fields = tabDefinition.getFields();
            if (fields.size() == 0) { // skip empty tabs
                continue;
            }
            for (final FieldDefinition fieldDefinition : fields) {
                final FieldFactory formField = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
                if (formField == null) {
                    continue;
                }
                formField.setComponentProvider(componentProvider);

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

        boolean firstFieldIsBuilt = false;

        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            FormTab tab = buildFormTab(tabDefinition, item, form);
            if (tab == null) continue;

            Iterator<Component> fieldIt = tab.getContainer().iterator();
            while (fieldIt.hasNext()) {
                view.addField((Field<?>) fieldIt.next());
            }

            fieldIt = tab.getContainer().iterator();
            if (!firstFieldIsBuilt && fieldIt.hasNext()) {
                ((Component.Focusable)fieldIt.next()).focus();
                firstFieldIsBuilt = true;
            }

            view.addFormSection(tabDefinition.getLabel(), tab.getContainer());
        }
        view.setShowAllEnabled(view.getFormSections().size() > 1);
    }

    public FormTab buildFormTab(TabDefinition tabDefinition, Item itemDatasource, Form parentForm) {
        List<FieldDefinition> fields = tabDefinition.getFields();
        if (fields.isEmpty()) { // skip empty tabs
            return null;
        }
        FormTab tab = new FormTab(tabDefinition);
        tab.setParent(parentForm);
        tab.getContainer().setName(tabDefinition.getName());
        for (final FieldDefinition fieldDefinition : fields) {
            final Field<?> field = createField(fieldDefinition, itemDatasource, tab);
            if (field != null) {
                tab.addField(field);
                final String helpDescription = fieldDefinition.getDescription();
                if (StringUtils.isNotBlank(helpDescription) && !isMessageKey(helpDescription)) {
                    tab.setComponentHelpDescription(field, helpDescription);
                }
            }
        }
        return tab;
    }

    public Field<?> createField(FieldDefinition fieldDefinition, Item itemDatasource, FormTab parentTab) {
        final FieldFactory fieldFactory = fieldFactoryFactory.createFieldFactory(fieldDefinition, itemDatasource);
        final FieldFactory formField = fieldFactory;
        if (formField == null) {
            return null;
        }

        formField.setComponentProvider(componentProvider);
        if (parentTab != null) {
            formField.setParent(parentTab);
        }

        final Field<?> field = formField.createField();
        if (field instanceof AbstractComponent) {
            ((AbstractComponent) field).setImmediate(true);
        }

        if (field instanceof TextArea || field instanceof CKEditorTextField || field instanceof AceEditor) {
            TextAreaStretcher.extend(field);
        }

        return field;
    }

    private boolean hasI18nAwareFields(FormDefinition formDefinition) {
        Iterator<TabDefinition> tabs = formDefinition.getTabs().iterator();

        while (tabs.hasNext()) {
            TabDefinition tab = tabs.next();
            Iterator<FieldDefinition> fields = tab.getFields().iterator();

            while (fields.hasNext()) {
                FieldDefinition field = fields.next();
                if (isI18nAware(field)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isI18nAware(FieldDefinition field) {
        if (field.isI18n()) {
            return true;
        }
        if (field instanceof CompositeFieldDefinition) {
            Iterator<ConfiguredFieldDefinition> fields = ((CompositeFieldDefinition) field).getFields().iterator();
            while (fields.hasNext()) {
                if (isI18nAware(fields.next())) {
                    return true;
                }
            }
        } else if (field instanceof MultiValueFieldDefinition) {
            field = ((MultiValueFieldDefinition) field).getField();
            return isI18nAware(field);
        }
        return false;
    }

    /**
     * @deprecated is a hack and should not be used. See MGNLUI-2207.
     */
    @Deprecated
    private boolean isMessageKey(final String text) {
        return !text.contains(" ") && text.contains(".") && !text.endsWith(".");
    }
}
