/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UICreateEvent;

/**
 * The AdmincentralUIProvider allows for fetching the widgetset and theme names from magnolia.properties rather than annotation or servlet params.
 */
public class AdmincentralUIProvider extends DefaultUIProvider {

    private static final Logger log = LoggerFactory.getLogger(AdmincentralUIProvider.class);

    public static final String WIDGETSET_PROPERTY_KEY = "magnolia.ui.vaadin.widgetset";
    public static final String THEME_PROPERTY_KEY = "magnolia.ui.vaadin.theme";

    public static final String OLD_52_WIDGETSET = "info.magnolia.ui.vaadin.gwt.MagnoliaWidgetSet";
    public static final String DEFAULT_WIDGETSET = "info.magnolia.widgetset.MagnoliaWidgetSet";
    public static final String DEFAULT_THEME = "admincentral";

    public static final String WIDGETSET_DOCUMENTATION_URL = "https://documentation.magnolia-cms.com/display/DOCS/Using+custom+widgets";

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
                if (widgetset.equals(OLD_52_WIDGETSET)) {
                    log.warn("Magnolia's default widgetset was relocated to '" + DEFAULT_WIDGETSET + "' but the '" + WIDGETSET_PROPERTY_KEY + "' property still points to its former location. "
                            + "Please update your magnolia.properties; for more info, see " + WIDGETSET_DOCUMENTATION_URL);
                } else {
                    return widgetset;
                }
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
