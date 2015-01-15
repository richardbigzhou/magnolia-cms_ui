/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.sample.app.main;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppController;

import javax.inject.Inject;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * View implementation of navigation view.
 */
public class NavigationViewImpl implements NavigationView {

    private Listener listener;

    private VerticalLayout layout;

    private final AppController appController;

    private final SimpleTranslator i18n;

    @Inject
    public NavigationViewImpl(AppController appController, SimpleTranslator i18n) {
        this.appController = appController;
        this.i18n = i18n;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        if (layout == null) {
            layout = new VerticalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.addComponent(new Label(i18n.translate("sample.app.navigation.label")));

            layout.addComponent(createButton("Alpha"));
            layout.addComponent(createButton("Bravo"));
            layout.addComponent(createButton("Charlie"));
            layout.addComponent(createButton("Delta"));
            layout.addComponent(createButton("Echo"));
        }
        return layout;
    }

    private Button createButton(final String name) {
        return new Button(i18n.translate("sample.app.navigation.button.select") + " " + name, new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                listener.onItemSelected(name);
            }
        });
    }
}
