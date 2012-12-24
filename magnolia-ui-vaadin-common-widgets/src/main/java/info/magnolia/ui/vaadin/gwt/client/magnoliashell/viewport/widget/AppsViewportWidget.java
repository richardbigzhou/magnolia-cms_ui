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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.LoadingIconWidget;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.Callbacks;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.AppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.MagnoliaSwipeRecognizer;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.TransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

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

/**
 * Client side implementation of Apps viewport.
 */
public class AppsViewportWidget extends ViewportWidget implements HasSwipeHandlers {

    private static final int SWIPE_OUT_THRESHOLD = 300;

    private static final String CLOSE_CLASSNAME = "v-app-close";

    private Element curtain;
    
    private final VAppPreloader preloader = new VAppPreloader();

    private final TouchDelegate delegate = new TouchDelegate(this);

    private final ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            final Element target = event.getNativeEvent().getEventTarget().cast();
            if (target.getClassName().contains(CLOSE_CLASSNAME)) {
                setClosing(true);
                getEventBus().fireEvent(new ViewportCloseEvent(ViewportType.APP_VIEWPORT));
            }
        }
    };

    /**
     * Instantiates a new apps viewport.
     */
    public AppsViewportWidget() {
        super();
        addDomHandler(closeHandler, ClickEvent.getType());
        bindTouchHandlers();
        setTransitionDelegate(TransitionDelegate.APPS_TRANSITION_DELEGATE);
    }

    /* CURTAIN INTEGRATION */

    @Override
    public void doSetActive(boolean active) {
        if (active) {
            setCurtainVisible(false);
        } else {
            if (hasChildren()) {
                setCurtainVisible(true);
            }
        }
    }

    public Element getCurtain() {
        if (curtain == null) {
            curtain = DOM.createDiv();
            curtain.setClassName("v-curtain v-curtain-green");
        }
        return curtain;
    }

    public void setCurtainVisible(boolean visible) {
        AppsTransitionDelegate transitionDelegate = (AppsTransitionDelegate) getTransitionDelegate();
        if (transitionDelegate != null) {
            transitionDelegate.setCurtainVisible(this, visible);
        } else {
            doSetCurtainVisible(visible);
        }
    }

    /**
     * Default non-transitioning behavior, accessible to transition delegates as
     * a fall back.
     */
    public void doSetCurtainVisible(boolean visible) {
        if (visible && getCurtain().getParentElement() != getElement()) {
            getElement().appendChild(getCurtain());
        } else if (!visible && getCurtain().getParentElement() == getElement()) {
            getElement().removeChild(getCurtain());
        }
    }

    private boolean hasChildren() {
        return getWidgetCount() - (isClosing() ? 1 : 0) > 0;
    }

    /* APP CLOSING */

    @Override
    public void doSetVisibleApp(Widget w) {
        if (getVisibleApp() != null) {
            // do not hide app if closing
            if (!isClosing()) {
                getVisibleApp().getElement().getStyle().setVisibility(Visibility.HIDDEN);
            }
        }
        w.setVisible(true);
        w.getElement().getStyle().clearVisibility();
    }

    @Override
    protected void removeWidget(final Widget w) {
        if (getTransitionDelegate() != null) {
            ((AppsTransitionDelegate) getTransitionDelegate()).removeWidget(this, w);
        } else {
            doRemoveWidget(w);
        }
    }

    @Override
    public void doRemoveWidget(Widget w) {
        super.doRemoveWidget(w);
        setClosing(false);
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
                final Widget newVisibleWidget = direction == DIRECTION.LEFT_TO_RIGHT ? getPreviousWidget()
                        : getNextWidget();
                if (event.isDistanceReached() && getWidgetCount() > 1) {
                    final JQueryWrapper jq = JQueryWrapper.select(getVisibleApp());
                    jq.animate(450, new AnimationSettings() {

                        {
                            setProperty("left", getOffsetWidth() * (direction == DIRECTION.LEFT_TO_RIGHT ? 1 : -1)
                                    - jq.position().left());
                            setCallbacks(Callbacks.create(new JQueryCallback() {

                                @Override
                                public void execute(JQueryWrapper query) {
                                    query.setCss("-webkit-transform", "");
                                    query.setCss("left", "");
                                    // query.setCss("opacity", "0");
                                    // query.setCss("visibility", "hidden");
                                    // do not trigger transitions
                                    setVisibleApp(newVisibleWidget);
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
                    final JQueryWrapper jq = JQueryWrapper.select(getVisibleApp());
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
                JQueryWrapper.select(getVisibleApp()).setCss("-webkit-transform", "");
                dropZIndeces();
            }
        });
    }

    private void processSwipe(int translationValue) {
        JQueryWrapper.select(getVisibleApp())
                .setCss("-webkit-transform", "translate3d(" + translationValue + "px,0,0)");
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
            getVisibleApp().getElement().getStyle().setZIndex(251);
        } else {
            previousWidget.getElement().getStyle().setZIndex(250);
            getVisibleApp().getElement().getStyle().setZIndex(251);
        }

        if (isNext && getWidgetCount() > 2) {
            JQueryWrapper.select(nextWidget).setCss("-webkit-transform",
                    "translate3d(" + (translationValue + getVisibleApp().getOffsetWidth()) + "px,0,0)");
        }
        
        nextWidget.getElement().getStyle()
                .setVisibility(isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);
        previousWidget.getElement().getStyle()
                .setVisibility(!isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private Widget getNextWidget() {
        int index = getWidgetIndex(getVisibleApp());
        return getWidget((index + 1) % getWidgetCount());
    }

    private Widget getPreviousWidget() {
        int index = getWidgetIndex(getVisibleApp());
        int count = getWidgetCount();
        return getWidget((index + (count - 1)) % count);
    }

    private void dropZIndeces() {
        final Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            it.next().getElement().getStyle().clearZIndex();
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

    /* APPEND CLOSE BUTTON */
    @Override
    public void insert(Widget w, int beforeIndex) {
        super.insert(w, beforeIndex);
        w.getElement().appendChild(createCloseButton());
    }

    private Element createCloseButton() {
        final Element closeElement = DOM.createSpan();
        closeElement.setClassName(CLOSE_CLASSNAME);
        closeElement.addClassName("icon-close");
        return closeElement;
    }

    /* APP PRELOADER */

    /**
     * Called when the transition of preloader is finished.
     */
    public interface PreloaderCallback {

        void onPreloaderShown(String appName);
    }

    public void showAppPreloader(final String appName, final PreloaderCallback callback) {
        // hideEntireContents();
        preloader.setCaption(appName);
        preloader.addStyleName("zoom-in");
        RootPanel.get().add(preloader);
        new Timer() {

            @Override
            public void run() {
                callback.onPreloaderShown(appName);
            }
        }.schedule(500);
    }

    public boolean hasPreloader() {
        return RootPanel.get().getWidgetIndex(preloader) >= 0;
    }

    /**
     * Preloader of the apps.
     */
    class VAppPreloader extends Widget {

        private final Element root = DOM.createDiv();

        private final Element navigator = DOM.createElement("ul");

        private final Element tab = DOM.createElement("li");

        private final Element tabCaption = DOM.createSpan();

        public VAppPreloader() {
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

    public void removePreloader() {
        RootPanel.get().remove(preloader);
    }

}
