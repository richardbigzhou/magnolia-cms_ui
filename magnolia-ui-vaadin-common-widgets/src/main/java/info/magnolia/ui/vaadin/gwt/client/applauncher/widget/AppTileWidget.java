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
package info.magnolia.ui.vaadin.gwt.client.applauncher.widget;

import info.magnolia.ui.vaadin.gwt.client.applauncher.event.AppActivationEvent;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppTile;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * The tile of one single app in AppLauncher.
 */
public class AppTileWidget extends Widget {

    private final Element icon = DOM.createDiv();

    private final Element label = DOM.createDiv();
    
    private final Element appLink = DOM.createAnchor();

    private final Element root = DOM.createDiv();

    private final Element runningIndicator = DOM.createDiv();

    private final Element iconContent = DOM.createSpan();

    private final AppTile appTile;

    private final EventBus eventBus;

    private boolean isActive = false;

    private TouchDelegate touchDelegate = new TouchDelegate(this);

    public AppTileWidget(EventBus eventBus, AppTile appTile) {
        super();
        this.appTile = appTile;
        this.eventBus = eventBus;
        constructDOM();
        bindHandlers();
        updateIcon();
        updateLink();
        updateCaption();
    }

    private void constructDOM() {
        setElement(root);
        root.appendChild(icon);
        root.appendChild(label);
        root.appendChild(appLink);
        root.appendChild(runningIndicator);
        addStyleName("item");
        icon.addClassName("icon");
        icon.appendChild(iconContent);

        label.addClassName("label");
        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
    }

    private void bindHandlers() {
        /**
         * Note that we have to add a explict hover class and not use the :hover
         * pseudo-selector. This is because we need an active state for when in
         * click that overrules the hover colors, And we cannot use an :active
         * pseudoselector either because the colors are assigned by code, not
         * stylesheet. But FYI, hover states are not useful for touch devices,
         * just for desktop now.
         */
        addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                getElement().addClassName("hover");
            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                getElement().removeClassName("hover");
                updateColors();
            }
        }, MouseOutEvent.getType());

        touchDelegate.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent event) {
                getElement().removeClassName("hover");
                setColorsClick();
            }
        });

        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                getElement().removeClassName("hover");
                setActiveState(true);
                eventBus.fireEvent(new AppActivationEvent(appTile.getName()));
            }
        });
    }

    public void setActiveState(boolean isActive) {
        this.isActive = isActive;
        updateColors();
    }

    /**
     * ** COLORING. ********
     */

    public void updateColors() {
        if (isActive()) {
            setColorsOn();
        } else {
            setColorsOff();
        }
    }

    /**
     * Set the tile colors for the Off state, not active, not clicked.
     */
    private void setColorsOff() {
        final Style style = getElement().getStyle();
        style.setProperty("backgroundColor", "");
        style.setProperty("color", "");
    }

    /**
     * Set the tile colors for the click state, whether active or not.
     */
    private void setColorsClick() {
        boolean isTileWhite = !getParent().isClientGroup();
        setColors(isTileWhite);
    }

    /**
     * Set the tile colors for the state: active, but not in a click or touch.
     */
    private void setColorsOn() {
        boolean isTileWhite = getParent().isClientGroup();
        setColors(isTileWhite);
    }

    /**
     * Set colors with the group coloring.
     */
    private void setColors(boolean isTileWhite) {
        final Style style = getElement().getStyle();
        if (isTileWhite) {
            style.setColor(getParent().getColor());
            style.setBackgroundColor("white");
        } else {
            style.setBackgroundColor(getParent().getColor());
            style.setColor("white");
        }
    }

    public String getName() {
        return appTile.getName();
    }

    public String getCaption() {
        return appTile.getCaption();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public VAppTileGroup getParent() {
        return (VAppTileGroup) super.getParent();
    }

    public void updateCaption() {
        if (appTile != null) {
            label.setInnerText(appTile.getCaption());
        }
    }

    public void updateIcon() {
        iconContent.addClassName(appTile.getIcon());
    }
    
    public void updateLink(){
        appLink.setAttribute("href", "#app:" + appTile.getName());
        appLink.addClassName("wai-aria-element");
        appLink.setInnerText(appTile.getCaption());
    }
}
