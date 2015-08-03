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
package info.magnolia.ui.api.i18n;

import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import com.vaadin.ui.HasComponents;

/**
 * Allows authors to create components with locale-dependent controls.
 */
public interface I18NAuthoringSupport {

    List<Locale> getAvailableLocales();

    Locale getDefaultLocale();

    boolean isDefaultLocale(Locale locale);

    String createI18NURI(Node node, Locale locale);

    String deriveLocalisedPropertyName(String base, Locale locale);

    List<Locale> getAvailableLocales(Node node);

    /**
     * @deprecated I18nAuthoringSupport is an instance singleton, and should not be used to sync UI state of a specific user. Since 5.3.9, use {@link info.magnolia.ui.api.app.SubAppContext#getAuthoringLocale() SubAppContext#getAuthoringLocale()}.
     * @see <a href="https://jira.magnolia-cms.com/browse/MGNLUI-3221">[MGNLUI-3221] All authors share the same authoring locale</a>
     */
    @Deprecated
    Locale getAuthorLocale();

    Locale getDefaultLocale(Node node);

    /**
     * @deprecated since 5.4.1 without a substitute - the i18n-ready components should now take
     * of switching the language on their own.
     */
    @Deprecated
    void i18nize(HasComponents fieldContainer, Locale locale);
}
