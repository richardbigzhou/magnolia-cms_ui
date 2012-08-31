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
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell;
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

    private static final String CLOSE_CLASSNAME = "v-app-close";

    private final VAppPreloader preloader = new VAppPreloader();

    private final TouchDelegate delegate = new TouchDelegate(this);

    private final ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            final Element target = (Element) event.getNativeEvent().getEventTarget().cast();
            if (target.getClassName().contains(CLOSE_CLASSNAME)) {
                setClosingWidget(true);
                getEventBus().fireEvent(new ViewportCloseEvent(VMagnoliaShell.ViewportType.APP_VIEWPORT));
            }
        }
    };

    /**
     * Instantiates a new apps viewport.
     */
    public VAppsViewport() {
        super();
        setForceContentAlign(false);
        addDomHandler(closeHandler, ClickEvent.getType());
        setContentHideAnimationDelegate(AnimationDelegate.ZOOMING_DELEGATE);

        bindTouchHandlers();
    }

    @Override
    protected void setClosingWidget(boolean closingWidget) {
        if (!closingWidget) {
            setViewportHideAnimationDelegate(null);
        }
        super.setClosingWidget(closingWidget);
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
            }.schedule(500);
        }
    }

    /* SWIPE GESTURES */

    private void bindTouchHandlers() {
        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
        delegate.addTouchHandler(new MagnoliaSwipeRecognizer(delegate, SWIPE_OUT_THRESHOLD));

        addSwipeStartHandler(new SwipeStartHandler() {

            @Override
            public void onSwipeStart(SwipeStartEvent event) {
                processSwipe(event.getDistance() * (event.getDirection() == DIRECTION.LEFT_TO_RIGHT ? 1 : -1));
            }
        });

        addSwipeMoveHandler(new SwipeMoveHandler() {

            @Override
            public void onSwipeMove(SwipeMoveEvent event) {
                processSwipe(event.getDistance() * (event.getDirection() == DIRECTION.LEFT_TO_RIGHT ? 1 : -1));
            }
        });

        addSwipeEndHandler(new SwipeEndHandler() {

            @Override
            public void onSwipeEnd(SwipeEndEvent event) {
                final DIRECTION direction = event.getDirection();
                final Widget newVisibleWidget = direction == DIRECTION.LEFT_TO_RIGHT ? getPreviousWidget() : getNextWidget();
                if (event.isDistanceReached() && getWidgetCount() > 1) {
                    final JQueryWrapper jq = JQueryWrapper.select(getVisibleWidget());
                    jq.animate(450, new AnimationSettings() {

                        {
                            setProperty("left", getOffsetWidth() * (direction == DIRECTION.LEFT_TO_RIGHT ? 1 : -1) - jq.position().left());
                            setCallbacks(Callbacks.create(new JQueryCallback() {

                                @Override
                                public void execute(JQueryWrapper query) {
                                    query.setCss("-webkit-transform", "");
                                    query.setCss("left", "");
                                    query.setCss("opacity", "0");
                                    query.setCss("visibility", "hidden");
                                    setVisibleWidget(newVisibleWidget);
                                    dropZIndeces();
                                }
                            }));
                        }
                    });

                    if (direction == DIRECTION.RIGHT_TO_LEFT && getWidgetCount() > 2) {
                        final JQueryWrapper query = JQueryWrapper.select(newVisibleWidget);
                        query.setCss("-webkit-transform", "");
                        newVisibleWidget.addStyleName("app-slider");
                        new Timer() {

                            @Override
                            public void run() {
                                newVisibleWidget.removeStyleName("app-slider");
                            }
                        }.schedule(500);
                    }
                } else {
                    final JQueryWrapper jq = JQueryWrapper.select(getVisibleWidget());
                    jq.setCssPx("left", jq.position().left());
                    jq.setCss("-webkit-transform", "");
                    jq.animate(500, new AnimationSettings() {

                        {
                            setProperty("left", 0);
                        }
                    });
                }
            }
        });

        delegate.addTouchCancelHandler(new TouchCancelHandler() {

            @Override
            public void onTouchCanceled(TouchCancelEvent event) {
                JQueryWrapper.select(getVisibleWidget()).setCss("-webkit-transform", "");
                dropZIndeces();
            }
        });
    }

    private void processSwipe(int translationValue) {
        JQueryWrapper.select(getVisibleWidget()).setCss("-webkit-transform", "translate3d(" + translationValue + "px,0,0)");
        if (getWidgetCount() > 1) {
            showCandidateApp(translationValue);
        }
    }

    private void showCandidateApp(int translationValue) {
        final Widget nextWidget = getNextWidget();
        final Widget previousWidget = getPreviousWidget();
        boolean isNext = translationValue < 0;
        if (isNext) {
            nextWidget.getElement().getStyle().setZIndex(250);
            getVisibleWidget().getElement().getStyle().setZIndex(251);
        } else {
            previousWidget.getElement().getStyle().setZIndex(250);
            getVisibleWidget().getElement().getStyle().setZIndex(251);
        }

        if (isNext && getWidgetCount() > 2) {
            JQueryWrapper.select(nextWidget).setCss(
                "-webkit-transform",
                "translate3d(" + (translationValue + getVisibleWidget().getOffsetWidth()) + "px,0,0)");
        }
        nextWidget.getElement().getStyle().setVisibility(isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);
        previousWidget.getElement().getStyle().setVisibility(!isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);

        nextWidget.getElement().getStyle().setOpacity(isNext || nextWidget == previousWidget ? 1 : 0);
        previousWidget.getElement().getStyle().setOpacity(!isNext || nextWidget == previousWidget ? 1 : 0);
    }

    private Widget getNextWidget() {
        int index = getWidgetIndex(getVisibleWidget());
        return getWidget((index + 1) % getWidgetCount());
    }

    private Widget getPreviousWidget() {
        int index = getWidgetIndex(getVisibleWidget());
        int count = getWidgetCount();
        return getWidget((index + (count - 1)) % count);
    }

    private void dropZIndeces() {
        final Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            it.next().getElement().getStyle().setProperty("zIndex", "");
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

    /* APPENDING CLOSE BUTTON */

    @Override
    protected void add(Widget child, Element container) {
        super.add(child, container);
        child.getElement().appendChild(appendCloseButton());
    }

    private Element appendCloseButton() {
        final Element closeElement = DOM.createSpan();
        closeElement.setClassName(CLOSE_CLASSNAME);
        // TODO mge: remove this classname when checking the styles
        closeElement.addClassName("close");
        closeElement.addClassName("icon-close");
        return closeElement;
    }

    /* SHOW PRELOADER */

    /**
     * Called when the transition of preloader is finished.
     */
    public interface PreloaderCallback {

        void onPreloaderShown(String appName);
    }

    public void showAppPreloader(final String appName, final PreloaderCallback callback) {
        // hideEntireContents();
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
            setStyleName("v-shell-viewport v-shell-tabsheet");
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
