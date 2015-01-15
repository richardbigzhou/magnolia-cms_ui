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
package info.magnolia.ui.admincentral;

import info.magnolia.init.MagnoliaConfigurationProperties;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UICreateEvent;

/**
 * The AdmincentralUIProvider allows for fetching the widgetset and theme names from magnolia.properties rather than annotation or servlet params.
 */
public class AdmincentralUIProvider extends DefaultUIProvider {

    private static final String WIDGETSET_PROPERTY_KEY = "magnolia.ui.vaadin.widgetset";
    private static final String THEME_PROPERTY_KEY = "magnolia.ui.vaadin.theme";

    private static final String DEFAULT_WIDGETSET = "info.magnolia.ui.vaadin.gwt.MagnoliaWidgetSet";
    private static final String DEFAULT_THEME = "admincentral";

    private final MagnoliaConfigurationProperties magnoliaProperties;

    @Inject
    public AdmincentralUIProvider(MagnoliaConfigurationProperties magnoliaProperties) {
        this.magnoliaProperties = magnoliaProperties;
    }

    @Override
    public String getWidgetset(UICreateEvent event) {
        if (magnoliaProperties != null) {
            String widgetset = magnoliaProperties.getProperty(WIDGETSET_PROPERTY_KEY);
            if (StringUtils.isNotBlank(widgetset)) {
                return widgetset;
            }
        }
        return DEFAULT_WIDGETSET;
    }

    @Override
    public String getTheme(UICreateEvent event) {
        if (magnoliaProperties != null) {
            String theme = magnoliaProperties.getProperty(THEME_PROPERTY_KEY);
            if (StringUtils.isNotBlank(theme)) {
                return theme;
            }
        }
        return DEFAULT_THEME;
    }
}
