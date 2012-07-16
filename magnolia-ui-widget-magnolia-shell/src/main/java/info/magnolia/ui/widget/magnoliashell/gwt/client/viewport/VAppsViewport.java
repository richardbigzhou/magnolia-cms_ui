/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side implementation of Apps viewport.
 */
public class VAppsViewport extends VShellViewport {

    private VAppPreloader preloader = new VAppPreloader();
    
    public VAppsViewport() {
        super();
        setForceContentAlign(false);
        setContentAnimationDelegate(ContentAnimationDelegate.FadingDelegate);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        if (getWidgetIndex(preloader) >= 0) {
            new Timer() {
                @Override
                public void run() {
                    remove(preloader);
                }
            }.schedule(1000);
        }
    }
    /**
     * Called when the transition of preloader is finished.
     */
    public interface PreloaderCallback {
        void onPreloaderShown(String appName);
    }

    public void showAppPreloader(final String appName, final PreloaderCallback callback) {
        preloader.setCaption(appName);
        add(preloader, getElement());
        preloader.addStyleName("zoom-in");
        new Timer() {
            @Override
            public void run() {
                callback.onPreloaderShown(appName);
            }
        }.schedule(750);
    }

    /**
     * Preloader of the apps.
     */
    class VAppPreloader extends Widget {

        private Element root = DOM.createDiv();

        private Element navigator = DOM.createElement("ul");

        private Element tab = DOM.createElement("li");
        
        private Element captionSpan = DOM.createSpan();

        public VAppPreloader() {
            super();
            setElement(root);
            addStyleName("v-shell-tabsheet");
            addStyleName("v-preloader");
            navigator.addClassName("nav");
            navigator.addClassName("nav-tabs");

            root.appendChild(navigator);
            navigator.appendChild(tab);
            tab.appendChild(captionSpan);
            
            getElement().getStyle().setPosition(Position.ABSOLUTE);
            getElement().getStyle().setHeight(100, Unit.PCT);
            getElement().getStyle().setWidth(100, Unit.PCT);
            getElement().getStyle().setBackgroundColor("#FFFFFF");
            getElement().getStyle().setVisibility(Visibility.VISIBLE);

            Element preloadingScreen = DOM.createDiv();
            preloadingScreen.addClassName("loading-screen");
            preloadingScreen.setInnerHTML(
                    "<div class=\"loading-message-wrapper\"> " +
                    "   <div class=\"loading-message\">" +
                    "       <div class=\"spinner\">" +
                    "           </div> Loading </div>" +
                    "</div>");
            getElement().appendChild(preloadingScreen);
        }
        
        public void setCaption(String caption) {
            captionSpan.setInnerHTML(caption);
        }
    }
}
