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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.HasComponents;

/**
 * Default implementation of {@link info.magnolia.ui.api.i18n.I18NAuthoringSupport}.
 */
public class DefaultI18NAuthoringSupport implements I18NAuthoringSupport {

    private static final Logger log = LoggerFactory.getLogger(DefaultI18NAuthoringSupport.class);

    private I18nContentSupport i18nContentSupport;

    private boolean enabled = true;

    public DefaultI18NAuthoringSupport() {
        this.i18nContentSupport = Components.getComponent(I18nContentSupport.class);
    }

    /**
     * Returns the available locales for the given page, area or component node.<br>
     * Please note though that this default implementation exclusively resolves locales through {@link #i18nContentSupport},
     * i.e. as configured in /server/i18n/content/locales, regardless of the passed node.
     *
     * @return the list of locales if both i18nAuthoringSupport and i18nContentSupport are enabled, <code>null</code> otherwise.
     */
    @Override
    public List<Locale> getAvailableLocales(Node node) {
        if (enabled && i18nContentSupport.isEnabled()) {
            return new ArrayList<>(i18nContentSupport.getLocales());
        }
        return Collections.emptyList();
    }

    @Override
    public Locale getDefaultLocale(Node node) {
        if (enabled && i18nContentSupport.isEnabled()) {
            return i18nContentSupport.getDefaultLocale();
        }
        return null;
    }

    @Override
    public String deriveLocalisedPropertyName(String base, Locale locale) {
        return String.format("%s_%s", base, locale.toString());
    }

    @Override
    public List<Locale> getAvailableLocales(Item item) {
        if (item instanceof JcrNodeAdapter) {
            return getAvailableLocales(((JcrNodeAdapter)item).getJcrItem());
        }
        return Collections.emptyList();
    }

    @Override
    public Locale getDefaultLocale(Item item) {
        if (item instanceof JcrNodeAdapter) {
            return getDefaultLocale(((JcrNodeAdapter) item).getJcrItem());
        }
        return MgnlContext.getLocale();
    }

    @Override
    public boolean isDefaultLocale(Locale locale, Item item) {
        return ObjectUtils.equals(getDefaultLocale(item), locale);
    }

    @Override
    public void i18nize(HasComponents fieldContainer, Locale locale) {
        log.warn("I18NAuthoringSupport.i18nize is deprecated without a replacement as of version 5.4.1, see e.g. #FormView.Listener.localeChanged(..) implementation for a workaround hint");
    }

    @Override
    public String createI18NURI(Node node, Locale locale) {
        // we are going to change the context language, this is ugly but is safe as only the current Thread is modified
        Locale currentLocale = i18nContentSupport.getLocale();
        String uri = null;
        try {
            // this is going to set the local in the aggregation state and hence wont change the i18nSupport object itself
            i18nContentSupport.setLocale(locale);
            uri = LinkUtil.createAbsoluteLink(node);
        }
        // make sure that we always reset to the original locale
        finally {
            i18nContentSupport.setLocale(currentLocale);
        }
        return uri;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @Deprecated
    public Locale getAuthorLocale() {
        log.warn("I18NAuthoringSupport.getAuthorLocale() is deprecated, returning null. Use SubAppContext.getAuthoringLocale() instead.");
        return null;
    }

    @Deprecated
    public void setAuthorLocale(Locale locale) {
        log.warn("I18NAuthoringSupport.setAuthorLocale(Locale) is deprecated, not doing anything. Use SubAppContext.setAuthoringLocale(Locale) instead.");
    }

}
