/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.setup.for5_3;

import static info.magnolia.ui.admincentral.AdmincentralUIProvider.*;

import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;
import info.magnolia.objectfactory.Components;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * The {@link WidgetsetRelocationCondition} adds a warning in case the <code>'magnolia.ui.vaadin.widgetset'</code> property was not updated according to the widgetset relocation.
 * <p>
 * For the time being we still tolerate the old widgetset name and convert it to the new one.
 *
 * @see {@link info.magnolia.ui.admincentral.AdmincentralUIProvider}
 */
public class WidgetsetRelocationCondition extends AbstractCondition {

    final MagnoliaConfigurationProperties magnoliaProperties;

    public WidgetsetRelocationCondition() {
        this(Components.getComponent(MagnoliaConfigurationProperties.class));
    }

    @Inject
    public WidgetsetRelocationCondition(MagnoliaConfigurationProperties magnoliaProperties) {
        super("Widgetset check", "See " + WIDGETSET_DOCUMENTATION_URL + " for more details.");
        this.magnoliaProperties = magnoliaProperties;
    }

    @Override
    public boolean check(InstallContext installContext) {
        String widgetset = magnoliaProperties.getProperty(WIDGETSET_PROPERTY_KEY);
        if (StringUtils.equals(widgetset, OLD_52_WIDGETSET)) {
            installContext.warn("Magnolia's default widgetset was relocated to '" + DEFAULT_WIDGETSET + "' but the '" + WIDGETSET_PROPERTY_KEY + "' property still points to its former location. "
                    + "Please update your magnolia.properties; for more info, see " + WIDGETSET_DOCUMENTATION_URL);
        }
        return true;
    }
}
