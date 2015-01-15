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
package info.magnolia.ui.framework.i18n;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.transformer.TransformedProperty;

import javax.jcr.Node;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

/**
 * Default implementation of {@link info.magnolia.ui.api.i18n.I18NAuthoringSupport}.
 */
public class DefaultI18NAuthoringSupport implements I18NAuthoringSupport {

    private I18nContentSupport i18nContentSupport;

    private boolean enabled = true;

    public DefaultI18NAuthoringSupport() {
        this.i18nContentSupport = Components.getComponent(I18nContentSupport.class);
    }

    @Override
    public AbstractSelect getLanguageChooser() {
        if (enabled && i18nContentSupport.isEnabled()) {
            Collection<Locale> locales = i18nContentSupport.getLocales();
            IndexedContainer c = new IndexedContainer();
            c.addContainerProperty("displayLanguage", String.class, "");
            for (Locale locale : locales) {
                Item it = c.addItem(locale);
                it.getItemProperty("displayLanguage").setValue(locale.getDisplayName());
            }
            ComboBox languageSelector = new ComboBox();
            languageSelector.setImmediate(true);
            languageSelector.setItemCaptionPropertyId("displayLanguage");
            languageSelector.setContainerDataSource(c);
            languageSelector.setNullSelectionAllowed(false);
            languageSelector.setTextInputAllowed(false);
            return languageSelector;
        } else {
            return null;
        }
    }

    @Override
    public void i18nize(HasComponents fieldContainer, Locale locale) {
        Iterator<Component> it = fieldContainer.iterator();
        boolean isFallbackLanguage = i18nContentSupport.getFallbackLocale().equals(locale);
        if (isEnabled() && i18nContentSupport.isEnabled() && locale != null) {
            while (it.hasNext()) {
                Component c = it.next();
                if (c instanceof  Field) {
                    Field f = (Field) c;
                    Property p = f.getPropertyDataSource();
                    if (p instanceof TransformedProperty) {
                        final TransformedProperty i18nBaseProperty = (TransformedProperty) p;
                        if (!i18nBaseProperty.hasI18NSupport()) {
                            continue;
                        }
                        final Locale formerLocale = i18nBaseProperty.getTransformer().getLocale();
                        final String basePropertyName = i18nBaseProperty.getTransformer().getBasePropertyName();
                        final String localizedPropertyName = isFallbackLanguage ?
                                basePropertyName :
                                constructI18NPropertyName(basePropertyName, locale);
                        i18nBaseProperty.getTransformer().setI18NPropertyName(localizedPropertyName);
                        i18nBaseProperty.getTransformer().setLocale(locale);
                        i18nBaseProperty.fireI18NValueChange();
                        String currentCaption = c.getCaption();
                        if (formerLocale != null) {
                            currentCaption = currentCaption.replace(String.format("(%s)", formerLocale.getLanguage()), "");
                        }
                        f.setCaption(String.format("%s (%s)", currentCaption, locale.getLanguage()));
                    }
                } else if (c instanceof  HasComponents) {
                    i18nize((HasComponents) c, locale);
                }
            }
        }
    }

    @Override
    public String createI18NURI(Node node, Locale locale){
        // we are going to change the context language, this is ugly but is safe as only the current Thread is modified
        Locale currentLocale = i18nContentSupport.getLocale();
        String uri = null;
        try {
            // this is going to set the local in the aggregation state and hence wont change the i18nSupport object itself
            i18nContentSupport.setLocale(locale);
            uri = LinkUtil.createAbsoluteLink(node);
        }
        // make sure that we always reset to the original locale
        finally{
            i18nContentSupport.setLocale(currentLocale);
        }
        return uri;
    }
    private String constructI18NPropertyName(CharSequence basePropertyName, Locale locale) {
        return basePropertyName + "_" + locale.toString();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
