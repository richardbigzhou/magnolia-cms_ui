/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.FormPresenter;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.vaadin.form.FormSection;
import info.magnolia.ui.vaadin.form.FormViewReduced;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.ui.SingleComponentContainer;

/**
 * Default implementation of {@link info.magnolia.ui.form.FormPresenter}. Main responsibilities of the class are:
 * <ul>
 * <li>Communication with {@link FormBuilder}</li>
 * <li>Tracking locale-specific representations of {@link FormSection}s</li>
 * <li>Listening to the {@link FormView}'s locale switcher and setting {@link SubAppContext}-wide locale.</li>
 * </ul>
 *
 * <strong>NOTE:</strong> ideally this class should have reside at the {@code info.magnolia.ui.form} package in <strong>magnolia-ui-form</strong>,
 * but due to {@link FormBuilder} being in <strong>magnolia-ui-dialog</strong> and the incorrect relation between {@link FormView}, {@link info.magnolia.ui.dialog.DialogView DialogView} and
 * {@link FormViewReduced} it has to in the current package.
 */
public class FormPresenterImpl implements FormPresenter {

    private final FormBuilder formBuilder;

    private final Map<Locale, Map<TabDefinition, FormSection>> localeToFormSections = Maps.newHashMap();

    private final UiContext uiContext;

    private FormViewReduced formView;

    private Locale activeLocale;

    private FormDefinition formDefinition;

    private Item itemDatasource;

    @Inject
    public FormPresenterImpl(FormBuilder formBuilder, UiContext uiContext) {
        this.formBuilder = formBuilder;
        this.uiContext = uiContext;
    }

    @Override
    public void presentView(FormViewReduced formView, FormDefinition formDefinition, Item item, FormItem parent) {
        this.formView = formView;
        this.formDefinition = formDefinition;
        this.itemDatasource = item;

        localeToFormSections.clear();
        // FormBuilder still expects the FormView object to build, so we have to cast here but ideally that should be refactored
        formBuilder.buildForm((FormView) this.formView, this.formDefinition, item, parent);

        // We should expand locale-awareness onto all the UI contexts.
        if (uiContext instanceof SubAppContext) {
            this.activeLocale = ((SubAppContext)uiContext).getAuthoringLocale();
            formView.setListener(new FormView.Listener() {
                @Override
                public void localeChanged(Locale newLocale) {
                    if (newLocale != null && !ObjectUtils.equals(((SubAppContext)uiContext).getAuthoringLocale(), newLocale)) {
                        setLocale(newLocale);
                    }
                }
            });

            Map<TabDefinition, FormSection> formSectionsMap = FluentIterable.from(formDefinition.getTabs())
                    .filter(new Predicate<TabDefinition>() {
                        @Override
                        public boolean apply(TabDefinition tabDefinition) {
                            return tabDefinition.getFields() != null && tabDefinition.getFields().size() > 0;
                        }
                    })
                    .toMap(new Function<TabDefinition, FormSection>() {
                        @Nullable
                        @Override
                        public FormSection apply(TabDefinition tabDefinition) {
                            return Iterables.tryFind(FormPresenterImpl.this.formView.getFormSections(), new FormSectionNameMatches(tabDefinition.getName())).orNull();
                        }
                    });
            
            localeToFormSections.put(this.activeLocale, formSectionsMap);
        }
    }

    @Override
    public boolean isValid() {
        return formView.isValid();
    }

    @Override
    public void setLocale(Locale locale) {
        if (uiContext instanceof SubAppContext && !ObjectUtils.equals(locale, this.activeLocale)) {
            final Locale formerLocale = this.activeLocale;
            this.activeLocale = locale;
            ((SubAppContext)uiContext).setAuthoringLocale(locale);

            final Map<TabDefinition, FormSection> currentFormSections = localeToFormSections.get(formerLocale);
            final Map<TabDefinition, FormSection> newFormSections = getLocaleSpecificFormSections(this.activeLocale);

            for (final Map.Entry<TabDefinition, FormSection> currentFormSectionEntry : currentFormSections.entrySet()) {
                final TabDefinition tabDefinition = currentFormSectionEntry.getKey();
                final FormSection currentFormSection = currentFormSectionEntry.getValue();

                if (currentFormSection != null) {
                    FormSection newFormSection = Iterables.tryFind(newFormSections.values(), new FormSectionNameMatches(currentFormSection.getName())).orNull();
                    if (newFormSection == null) {
                        newFormSection = formBuilder.buildFormTab(tabDefinition, itemDatasource, null).getContainer();
                        newFormSections.put(tabDefinition, newFormSection);
                    }

                    ((SingleComponentContainer) currentFormSection.getParent()).setContent(newFormSection);
                }
            }
        }
    }

    private Map<TabDefinition, FormSection> getLocaleSpecificFormSections(Locale activeLocale) {
        Map<TabDefinition, FormSection> result = localeToFormSections.get(activeLocale);
        if (result == null) {
            result = Maps.newLinkedHashMap();
            localeToFormSections.put(activeLocale, result);
        }
        return result;
    }

    private class FormSectionNameMatches implements Predicate<FormSection> {

        private String nameToMatch;

        public FormSectionNameMatches(String nameToMatch) {
            this.nameToMatch = nameToMatch;
        }

        @Override
        public boolean apply(@Nonnull FormSection input) {
            return nameToMatch.equals(input.getName());
        }
    }

}
