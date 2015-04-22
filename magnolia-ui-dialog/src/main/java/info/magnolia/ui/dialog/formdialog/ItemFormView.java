/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.BaseDialogViewImpl;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DescriptionVisibilityEvent;
import info.magnolia.ui.vaadin.dialog.FormDialog;
import info.magnolia.ui.vaadin.form.Form;
import info.magnolia.ui.vaadin.form.FormSection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

/**
 * Owns a Form and Dialog and connects them.
 */
public class ItemFormView extends BaseDialogViewImpl implements FormView {

    private static final Logger log = LoggerFactory.getLogger(ItemFormView.class);

    private ComboBox languageSelector;

    private Form form = new Form();

    private final SimpleTranslator i18n;
    private final I18NAuthoringSupport i18nAuthoringSupport;
    private final UiContext uiContext;

    @Inject
    public ItemFormView(SimpleTranslator i18n, I18NAuthoringSupport i18nAuthoringSupport, UiContext uiContext) {
        super(new FormDialog());
        this.i18n = i18n;
        this.i18nAuthoringSupport = i18nAuthoringSupport;
        this.uiContext = uiContext;

        form.setErrorLabels(i18n.translate("validation.message.errors"), i18n.translate("validation.message.nextError"));

        getDialog().setContent(form);
        getDialog().addDescriptionVisibilityHandler(new BaseDialog.DescriptionVisibilityEvent.Handler() {

            @Override
            public void onDescriptionVisibilityChanged(DescriptionVisibilityEvent event) {
                form.setDescriptionVisibility(event.isVisible());
            }
        });

        createLocaleSelector();
    }

    @Deprecated
    public ItemFormView(SimpleTranslator i18n, I18NAuthoringSupport i18nAuthoringSupport) {
        this(i18n, i18nAuthoringSupport, null);
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
    public void setCaption(String caption) {
        getDialog().setCaption(caption);
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
        form.getContent().showAllTab(enabled, i18n.translate("form.tabs.all"));
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
    public void setAvailableLocales(List<Locale> locales) {
        if (locales != null && !locales.isEmpty()) {
            languageSelector.removeAllItems();
            for (Locale locale : locales) {
                String label= locale.getDisplayLanguage(MgnlContext.getLocale());
                if (!locale.getDisplayCountry(MgnlContext.getLocale()).isEmpty()) {
                    label += " (" + locale.getDisplayCountry(MgnlContext.getLocale()) + ")";
                }
                languageSelector.addItem(locale);
                languageSelector.setItemCaption(locale, label);
            }
            getActionAreaView().setToolbarComponent(languageSelector);
        }
    }

    private void createLocaleSelector() {
        languageSelector = new ComboBox();
        languageSelector.setSizeUndefined();
        languageSelector.setImmediate(true);
        languageSelector.setNullSelectionAllowed(false);
        languageSelector.setTextInputAllowed(false);
        languageSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateLocale((Locale) event.getProperty().getValue());
            }
        });
    }

    protected void updateLocale(Locale locale) {
        if (i18nAuthoringSupport != null) {
            i18nAuthoringSupport.i18nize(form, (locale));
            // As of 5.3.9 only subapp context supports tracking current authoring locale, we may expand that to other UiContexts in the future if needed.
            if (uiContext instanceof SubAppContext && locale != null) {
                // Temporarily only in SubAppContextImpl, by the time method is generalized to SubAppContext interface in 5.4.
                setAuthoringLocale((SubAppContext) uiContext, locale);
            }
        }
    }

    /**
     * @deprecated since 5.4 - once interface method <code>setAuthoringLocale(Locale)</code> is added to SubAppContext, remove.
     */
    private Locale setAuthoringLocale(SubAppContext subAppContext, Locale authoringLocale) {
        Method methodToFind;
        try {
            methodToFind = subAppContext.getClass().getDeclaredMethod("setAuthoringLocale", new Class[] { Locale.class });
            if (methodToFind != null) {
                return (Locale) methodToFind.invoke(subAppContext, new Object[] { authoringLocale });
            }
        } catch (NoSuchMethodException e) {
            log.error("Error getting method 'getAuthoringLocale()' from SubAppContextImpl, got {}.", subAppContext, e);
        } catch (InvocationTargetException e) {
            log.error("Error invoking method 'getAuthoringLocale()' from SubAppContextImpl, got {}.", subAppContext, e);
        } catch (IllegalAccessException e) {
            log.error("Error accessing method 'getAuthoringLocale()' from SubAppContextImpl, got {}.", subAppContext, e);
        }
        return null;
    }
}
