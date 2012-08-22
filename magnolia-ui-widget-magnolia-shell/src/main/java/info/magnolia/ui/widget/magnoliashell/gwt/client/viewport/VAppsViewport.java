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

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelHandler;
import com.googlecode.mgwt.dom.client.recognizer.swipe.HasSwipeHandlers;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEndEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEndHandler;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEvent.DIRECTION;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeMoveEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeMoveHandler;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeStartEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeStartHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side implementation of Apps viewport.
 */
public class VAppsViewport extends VShellViewport implements HasSwipeHandlers {

    private static final int SWIPE_OUT_THRESHOLD = 300;
            
    private final VAppPreloader preloader = new VAppPreloader();

    private final Element closeWrapper = DOM.createDiv();

    private final TouchDelegate delegate = new TouchDelegate(this);
    
    public VAppsViewport() {
        super();
        setForceContentAlign(false);
        setContentAnimationDelegate(ContentAnimationDelegate.FadingDelegate);
        final Element closeButton = DOM.createButton();
        closeWrapper.setClassName("close");
        closeButton.setClassName("action-close");
        closeWrapper.appendChild(closeButton);
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (closeWrapper.isOrHasChild((Element) event.getNativeEvent().getEventTarget().cast())) {
                    getEventBus().fireEvent(new ViewportCloseEvent(VAppsViewport.this));
                }
            }
        }, ClickEvent.getType());

        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
        delegate.addTouchHandler(new MagnoliaSwipeRecognizer(delegate, SWIPE_OUT_THRESHOLD));
        
        addSwipeStartHandler(new SwipeStartHandler() {
            @Override
            public void onSwipeStart(SwipeStartEvent event) {
                
            }
        });

        addSwipeMoveHandler(new SwipeMoveHandler() {
            @Override
            public void onSwipeMove(SwipeMoveEvent event) {
                final DIRECTION direction = event.getDirection();
                int translationValue = event.getDistance() * (direction == DIRECTION.LEFT_TO_RIGHT ? 1 : -1);
                JQueryWrapper.select(getVisibleWidget()).setCss("-webkit-transform", "translate3d("+ translationValue + "px,0,0)");
                if (getWidgetCount() > 1) {
                    showCandidateApp(translationValue  > 0);
                }
            }
        });
        
        
        addSwipeEndHandler(new SwipeEndHandler() {
            @Override
            public void onSwipeEnd(SwipeEndEvent event) {
                dropZIndeces();
                final DIRECTION direction = event.getDirection();
                final Widget newVisibleWidget = direction == DIRECTION.LEFT_TO_RIGHT ? getPreviousWidget() : getNextWidget();  
                if (event.isDistanceReached()) {
                    JQueryWrapper.select(getVisibleWidget()).animate(300, new AnimationSettings() {{
                        setProperty("left", getOffsetWidth());
                        setCallbacks(Callbacks.create(new JQueryCallback() {
                            @Override
                            public void execute(JQueryWrapper query) {    
                                query.setCss("opacity", "0");
                                query.setCss("left", "");
                                query.setCss("-webkit-transform", "translate3d(0,0,0)");
                                query.setCss("visibility", "hidden");
                                setVisibleWidget(newVisibleWidget);
                            }
                        }));
                    }});
                } else {
                    JQueryWrapper.select(getVisibleWidget()).setCss("-webkit-transform", "translate3d(0,0,0)");
                }
            }
        });
        
        delegate.addTouchCancelHandler(new TouchCancelHandler() {
            @Override
            public void onTouchCanceled(TouchCancelEvent event) {
                JQueryWrapper.select(getVisibleWidget()).setCss("-webkit-transform", "translate3d(0,0,0)");
                dropZIndeces();
            }
        });
    }

    protected void showCandidateApp(boolean isNext) {
        final Widget nextWidget = getNextWidget();
        final Widget previousWidget = getPreviousWidget();
        
        if (isNext) {
            
            previousWidget.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            previousWidget.getElement().getStyle().setOpacity(1d);
            
            if (nextWidget != previousWidget) {
                nextWidget.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                nextWidget.getElement().getStyle().setOpacity(0d);
            }
            
            previousWidget.getElement().getStyle().setZIndex(250);
            getVisibleWidget().getElement().getStyle().setZIndex(251);
        } else {
            nextWidget.getElement().getStyle().setOpacity(1d);
            nextWidget.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            
            if (nextWidget != previousWidget) {
                previousWidget.getElement().getStyle().setOpacity(0d);
                previousWidget.getElement().getStyle().setVisibility(Visibility.HIDDEN);   
            }

            nextWidget.getElement().getStyle().setZIndex(250);
            getVisibleWidget().getElement().getStyle().setZIndex(251);
        }
        
    }

    protected Widget getNextWidget() {
        int index = getWidgetIndex(getVisibleWidget());
        return getWidget((index + 1) % getWidgetCount());
    }
    
    protected Widget getPreviousWidget() {
        int index = getWidgetIndex(getVisibleWidget());
        int count = getWidgetCount();
        return getWidget((index + (count - 1)) % count);        
    }
    
    protected void dropZIndeces() {
        final Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            it.next().getElement().getStyle().setProperty("zIndex", "");
        }
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        preloader.getElement().getStyle().setZIndex(299);
        super.updateFromUIDL(uidl, client);
        if (RootPanel.get().getWidgetIndex(preloader) >= 0) {
            new Timer() {
                @Override
                public void run() {
                    RootPanel.get().remove(preloader);
                }
            }.schedule(1000);
        }
    }

    @Override
    public HandlerRegistration addSwipeStartHandler(SwipeStartHandler handler) {
        return addHandler(handler, SwipeStartEvent.getType());
    }

    @Override
    public HandlerRegistration addSwipeMoveHandler(SwipeMoveHandler handler) {
        return addHandler(handler, SwipeMoveEvent.getType());
    }

    @Override
    public HandlerRegistration addSwipeEndHandler(SwipeEndHandler handler) {
        return addHandler(handler, SwipeEndEvent.getType());
    }
    
    /**
     * Called when the transition of preloader is finished.
     */
    public interface PreloaderCallback {

        void onPreloaderShown(String appName);
    }

    public void showAppPreloader(final String appName, final PreloaderCallback callback) {
        hideEntireContents();
        preloader.setCaption(appName);
        RootPanel.get().add(preloader);
        preloader.addStyleName("zoom-in");
        new Timer() {

            @Override
            public void run() {
                callback.onPreloaderShown(appName);
            }
        }.schedule(500);
    }

    @Override
    protected void setWidgetVisibleWithTransition(Widget w) {
        super.setWidgetVisibleWithTransition(w);
        w.getElement().appendChild(closeWrapper);
    }

    /**
     * Preloader of the apps.
     */
    class VAppPreloader extends Widget {

        private final Element root = DOM.createDiv();

        private final Element navigator = DOM.createElement("ul");

        private final Element tab = DOM.createElement("li");

        private final Element captionSpan = DOM.createSpan();

        public VAppPreloader() {
            super();
            setElement(root);
            setStyleName("v-shell-vieport v-shell-tabsheet");
            navigator.addClassName("nav nav-tabs single-tab");
            tab.addClassName("clearfix active");
            captionSpan.setClassName("tab-title");

            tab.appendChild(captionSpan);
            navigator.appendChild(tab);
            root.appendChild(navigator);

            Element preloadingScreen = DOM.createDiv();
            preloadingScreen.addClassName("loading-screen");
            preloadingScreen.setInnerHTML("<div class=\"loading-message-wrapper\"> "
                    + "<div class=\"loading-message\"><div class=\"spinner\"></div> Loading </div></div>");
            root.appendChild(preloadingScreen);
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            getElement().getStyle().setZIndex(301);
        }

        public void setCaption(String caption) {
            captionSpan.setInnerHTML(caption);
        }
    }

}
