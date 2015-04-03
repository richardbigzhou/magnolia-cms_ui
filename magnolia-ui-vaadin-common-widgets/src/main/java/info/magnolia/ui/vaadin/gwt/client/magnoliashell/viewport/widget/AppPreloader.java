/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.LoadingIconWidget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Preloader of the apps.
 */
public class AppPreloader extends Widget {

    private final Element root = DOM.createDiv();

    private final Element navigator = DOM.createElement("ul");

    private final Element tab = DOM.createElement("li");

    private final Element tabCaption = DOM.createSpan();

    public AppPreloader() {
        super();
        setElement(root);
        setStyleName("v-app-preloader v-viewport v-shell-tabsheet app");

        navigator.addClassName("nav nav-tabs single-tab");
        tab.addClassName("clearfix active");
        tabCaption.setClassName("tab-title");

        tab.appendChild(tabCaption);
        navigator.appendChild(tab);
        root.appendChild(navigator);

        Element preloader = DOM.createDiv();
        preloader.addClassName("v-preloader");

        Element loading = DOM.createSpan();
        loading.addClassName("v-caption");
        loading.setInnerText("Loading");

        preloader.appendChild(new LoadingIconWidget().getElement());
        preloader.appendChild(DOM.createElement("br"));
        preloader.appendChild(loading);

        root.appendChild(preloader);
    }

    public void setCaption(String caption) {
        tabCaption.setInnerHTML(caption);
    }
}
