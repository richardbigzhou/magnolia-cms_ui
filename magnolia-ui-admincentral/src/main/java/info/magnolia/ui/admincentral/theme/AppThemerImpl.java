/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.admincentral.theme;

import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.framework.app.theme.AppThemer;
import info.magnolia.ui.framework.app.theme.ThemedApp;

import org.vaadin.cssinject.CSSInject;

import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;

/**
 * Add theme to an App.
 */
public class AppThemerImpl implements AppThemer {

    private CSSInject cssInject = null;

    public AppThemerImpl() {
        this.cssInject = new CSSInject(VaadinService.getCurrent().findUI(
                VaadinService.getCurrentRequest()));
    }

    @Override
    public void themeAnnotated(App app) {
        if (app.getClass().isAnnotationPresent(ThemedApp.class)) {
            ThemedApp themeAnnotation = app.getClass().getAnnotation(
                    ThemedApp.class);
            setTheme(app.getView(), themeAnnotation.value());
        }
    }

    @Override
    public void setTheme(AppView view, String themeName) {
        String stylename = String.format("app-%s", themeName);
        Component vaadinComponent = view.asVaadinComponent();
        vaadinComponent.addStyleName(stylename);

        String themeUrl = String.format("../%s/styles.css", themeName);
        ThemeResource res = new ThemeResource(themeUrl);
        this.cssInject.addStyleSheet(res);
    }
}
