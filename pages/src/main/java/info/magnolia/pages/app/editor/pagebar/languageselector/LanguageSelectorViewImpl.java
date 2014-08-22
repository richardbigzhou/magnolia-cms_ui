/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.pagebar.languageselector;

import info.magnolia.context.MgnlContext;

import java.util.List;
import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

/**
 * Implementation of {@link LanguageSelector}. Displays a {@link ComboBox} for all available {@link Locale}s.
 */
public class LanguageSelectorViewImpl implements LanguageSelectorView {

    private Listener listener;
    private ComboBox selector;

    public LanguageSelectorViewImpl() {
        this.selector = new ComboBox();

        selector.setSizeUndefined();
        selector.setImmediate(true);
        selector.setNullSelectionAllowed(false);
        selector.setTextInputAllowed(false);
        selector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (listener != null) {
                    listener.languageSelected((Locale) event.getProperty().getValue());
                }
            }
        });
    }

    @Override
    public Component asVaadinComponent() {
        return selector;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setCurrentLanguage(Locale locale) {
        if (selector != null) {
            selector.setValue(locale);
        }
    }

    @Override
    public void setAvailableLanguages(List<Locale> locales) {
        if (locales != null && !locales.isEmpty()) {
            selector.removeAllItems();
            for (Locale locale : locales) {
                String label = locale.getDisplayLanguage(MgnlContext.getLocale());
                if (!locale.getDisplayCountry(MgnlContext.getLocale()).isEmpty()) {
                    label += " (" + locale.getDisplayCountry(MgnlContext.getLocale()) + ")";
                }
                selector.addItem(locale);
                selector.setItemCaption(locale, label);
            }
            selector.setVisible(true);
        } else {
            selector.setVisible(false);
        }
    }
}
