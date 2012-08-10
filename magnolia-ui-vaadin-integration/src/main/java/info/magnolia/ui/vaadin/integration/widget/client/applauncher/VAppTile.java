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

import info.magnolia.ui.vaadin.integration.widget.client.applauncher.event.AppActivationEvent;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
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
                getElement().getStyle().setColor(getParent().getColor());
                getElement().getStyle().setBackgroundColor("white");
            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (!isActive()) {
                    getElement().getStyle().setProperty("backgroundColor", "");
                    getElement().getStyle().setProperty("color", "");
                } else {
                    getElement().getStyle().setBackgroundColor(getParent().getColor());
                    getElement().getStyle().setColor("white");
                }
            }
        }, MouseOutEvent.getType());

        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
            }
        }, MouseDownEvent.getType());
        
        
        touchDelegate.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent event) {
                getElement().getStyle().setColor(getParent().getColor());
                getElement().getStyle().setBackgroundColor("white");                
            }
        });
        
        touchDelegate.addTouchCancelHandler(new TouchCancelHandler() {
            @Override
            public void onTouchCanceled(TouchCancelEvent event) {
                if (!isActive()) {
                    getElement().getStyle().setProperty("backgroundColor", "");
                    getElement().getStyle().setProperty("color", "");
                } else {
                    getElement().getStyle().setBackgroundColor(getParent().getColor());
                    getElement().getStyle().setColor("white");
                }                
            }
        });
        
        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                if (!isActive()) {
                    setActive(true);
                    getElement().getStyle().setBackgroundColor(getParent().getColor());
                    getElement().getStyle().setColor("white");
                }
                eventBus.fireEvent(new AppActivationEvent(appTileData.getName()));
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
        if (!isActive()) {
            getElement().getStyle().setProperty("backgroundColor", "");
            getElement().getStyle().setProperty("color", "");
        } else {
            getElement().getStyle().setBackgroundColor(getParent().getColor());
            getElement().getStyle().setColor("white");
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
