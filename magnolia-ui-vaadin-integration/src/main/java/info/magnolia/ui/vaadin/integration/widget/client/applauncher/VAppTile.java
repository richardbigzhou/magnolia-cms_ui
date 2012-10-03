/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import com.google.gwt.dom.client.Style;
import info.magnolia.ui.vaadin.integration.widget.client.applauncher.event.AppActivationEvent;

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
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * The tile of one single app in AppLauncher.
 *
 */
public class VAppTile extends Widget {

    private final Element icon = DOM.createDiv();

    private final Element label = DOM.createDiv();

    private final Element root = DOM.createDiv();

    private final Element runningIndicator = DOM.createDiv();

    private final Element iconContent = DOM.createSpan();

    private final VAppTileJSO appTileData;

    private final EventBus eventBus;

    private boolean isActive = false;

    private TouchDelegate touchDelegate = new TouchDelegate(this);

    public VAppTile(EventBus eventBus, VAppTileJSO appTile) {
        super();
        this.appTileData = appTile;
        this.eventBus = eventBus;
        constructDOM();
        bindHandlers();
        updateIcon();
        updateCaption();
    }

    private void constructDOM() {
        setElement(root);
        root.appendChild(icon);
        root.appendChild(label);
        root.appendChild(runningIndicator);
        addStyleName("item");
        icon.addClassName("icon");
        label.addClassName("label");

        icon.appendChild(iconContent);

        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
    }

    private void bindHandlers() {
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


        /*
        Currently no ability to cancel because we are using touchstart.
        touchDelegate.addTouchCancelHandler(new TouchCancelHandler() {

            @Override

            public void onTouchCanceled(TouchCancelEvent event) {
                updateColors();
            }
        });
        */

        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {

                getElement().removeClassName("hover");
                setColorsForClientGroup(false);

                eventBus.fireEvent(new AppActivationEvent(appTileData.getName()));
            }
        });


        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                if (!isActive()) {
                    setActive(true);
                }
                updateColors();
            }
        });

        touchDelegate.addTouchMoveHandler(new TouchMoveHandler() {
            @Override
            public void onTouchMove(TouchMoveEvent event) {
                //VConsole.log("TOUCH MOVE");
            }
        });
    }


    public void setActive(boolean isActive) {
        this.isActive = isActive;
        updateColors();
    }

    public void updateColors(){
        if (isActive()) {
            setColorsForClientGroup(true);
        } else {
            setColorsOff();
        }
    }

    private void setColorsOff(){
        final Style style = getElement().getStyle();
        style.setProperty("backgroundColor", "");
        style.setProperty("color", "");
    }

    private void setColorsForClientGroup(boolean isActive){
        final Style style = getElement().getStyle();
        boolean setColorWhiteAndBackgroundFromParent = getParent().isClientGroup();
        if (!isActive) {
            setColorWhiteAndBackgroundFromParent = !setColorWhiteAndBackgroundFromParent;
        }

        if (setColorWhiteAndBackgroundFromParent){
            style.setBackgroundColor(getParent().getColor());
            style.setColor("white");
        } else {
            style.setColor(getParent().getColor());
            style.setBackgroundColor("white");
        }
    }

    public String getName() {
        return appTileData.getName();
    }

    public String getCaption() {
        return appTileData.getCaption();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public VAppTileGroup getParent() {
        return (VAppTileGroup) super.getParent();
    }

    public void updateCaption() {
        if (appTileData != null) {
            label.setInnerText(appTileData.getCaption());
        }
    }

    public void updateIcon() {
        iconContent.addClassName(appTileData.getIcon());
    }
}
