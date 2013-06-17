/**
 * This file Copyright (c) 2010-2013 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.CloseButton;
import info.magnolia.ui.vaadin.gwt.client.FullScreenButton;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.Callbacks;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.AppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.MagnoliaSwipeRecognizer;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.dom.client.recognizer.swipe.HasSwipeHandlers;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEndEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEndHandler;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEvent;
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

    /**
     * Listener interface for {@link AppsViewportWidget}.
     */
    public interface Listener {
        void closeCurrentApp();
    };

    private static final int SWIPE_OUT_THRESHOLD = 300;

    private final AppPreloader preloader = new AppPreloader();

    private boolean isAppClosing = false;

    private boolean isCurtainVisible = false;

    private Listener listener;

    private final TouchDelegate delegate = new TouchDelegate(this);

    private final FullScreenButton fullScreenButton = new FullScreenButton();

    private Element curtain = DOM.createDiv();

    private void closeCurrentApp() {
        if (!isAppClosing()) {
            isAppClosing = true;
            listener.closeCurrentApp();
        }
    }

    private void toggleFullScreen() {
        String cssClasses = RootPanel.get().getStyleName();
        boolean isFullScreen = cssClasses.contains("fullscreen");
        // toggle.
        setFullScreen(!isFullScreen);
    }

    private CloseButton closeButton = new CloseButton();

    /**
     * Set the look of the application and the state of the button.
     */
    public void setFullScreen(boolean isFullScreen) {

        if (isFullScreen) {
            // enable fullscreen
            RootPanel.get().addStyleName("fullscreen");

            fullScreenButton.getElement().addClassName("icon-close-fullscreen_2");
            fullScreenButton.getElement().removeClassName("icon-open-fullscreen_2");
        } else {

            // disable fullscreen
            RootPanel.get().removeStyleName("fullscreen");

            fullScreenButton.getElement().addClassName("icon-open-fullscreen_2");
            fullScreenButton.getElement().removeClassName("icon-close-fullscreen_2");
        }
    }

    public AppsViewportWidget(final Listener listener) {
        super();
        this.listener = listener;
        DOM.sinkEvents(getElement(), Event.ONCLICK);
        curtain.setClassName("v-curtain v-curtain-green");
        closeButton.addStyleDependentName("app");
        delegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                Element target = event.getNativeEvent().getEventTarget().cast();
                if (closeButton.getElement().isOrHasChild(target)) {
                    closeCurrentApp();
                } else if (fullScreenButton.getElement().isOrHasChild(target)) {
                    toggleFullScreen();
                }
            }
        });

        bindTouchHandlers();

    }

    public void goToNextApp() {
        if (getWidgetCount() > 1) {
            processSwipe(1);
            switchToApp(DIRECTION.LEFT_TO_RIGHT);
        }
    }

    public void goToPreviousApp() {
        if (getWidgetCount() > 1) {
            processSwipe(-1);
            switchToApp(DIRECTION.RIGHT_TO_LEFT);
        }
    }

    public Element getCurtain() {
        return curtain;
    }

    public void setCurtainVisible(boolean visible) {
        if (isCurtainVisible != visible) {
            this.isCurtainVisible = visible;
            ((AppsTransitionDelegate) getTransitionDelegate()).setCurtainVisible(isCurtainVisible);
        }
    }

    /* APP CLOSING */
    @Override
    public void showChildNoTransition(Widget w) {
        getElement().appendChild(closeButton.getElement());
        getElement().appendChild(fullScreenButton.getElement());
        Widget formerVisible = getVisibleChild();
        // do not hide app if closing
        if (formerVisible != null && !isAppClosing()) {
            formerVisible.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
        w.setVisible(true);
        w.getElement().getStyle().clearVisibility();
    }

    @Override
    public void removeChild(Widget w) {
        ((AppsTransitionDelegate) getTransitionDelegate()).removeWidget(w);
        if (getWidgetCount() < 2) {
            getElement().removeChild(closeButton.getElement());
            getElement().removeChild(fullScreenButton.getElement());
        }
    }

    @Override
    public void removeChildNoTransition(Widget w) {
        super.removeChildNoTransition(w);
        isAppClosing = false;
    }

    public boolean isAppClosing() {
        return isAppClosing;
    }

    /* APP PRELOADER */
    public void showAppPreloader(final String appName) {
        preloader.setCaption(appName);
        preloader.addStyleName("zoom-in");
        RootPanel.get().add(preloader);
    }

    public boolean hasPreloader() {
        return RootPanel.get().getWidgetIndex(preloader) >= 0;
    }

    public void removePreloader() {
        final FadeAnimation preloaderFadeOut = new FadeAnimation(0d, true);
        preloaderFadeOut.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                RootPanel.get().remove(preloader);
            }
        });
        preloaderFadeOut.run(500, preloader.getElement());
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
                processSwipe(event.getDistance() * (event.getDirection() == SwipeEvent.DIRECTION.LEFT_TO_RIGHT ? 1 : -1));
            }
        });

        addSwipeEndHandler(new SwipeEndHandler() {

            @Override
            public void onSwipeEnd(SwipeEndEvent event) {
                final SwipeEvent.DIRECTION direction = event.getDirection();

                if (event.isDistanceReached() && getWidgetCount() > 1) {

                    switchToApp(direction);

                } else {
                    final JQueryWrapper jq = JQueryWrapper.select(getVisibleChild());
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
                JQueryWrapper.select(getVisibleChild()).setCss("-webkit-transform", "");
                dropZIndeces();
            }
        });
    }

    private void switchToApp(final SwipeEvent.DIRECTION direction) {

        final Widget newVisibleWidget = direction == DIRECTION.LEFT_TO_RIGHT ? getPreviousWidget() : getNextWidget();

        final JQueryWrapper jq = JQueryWrapper.select(getVisibleChild());
        jq.animate(450, new AnimationSettings() {

            {
                setProperty("left", getOffsetWidth() * (direction == DIRECTION.LEFT_TO_RIGHT ? 1 : -1) - jq.position().left());
                setCallbacks(Callbacks.create(new JQueryCallback() {

                    @Override
                    public void execute(JQueryWrapper query) {
                        query.setCss("-webkit-transform", "");
                        query.setCss("left", "");
                        // query.setCss("opacity", "0");
                        // query.setCss("visibility", "hidden");
                        // do not trigger transitions
                        showChild(newVisibleWidget);
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

    }

    private void processSwipe(int translationValue) {
        JQueryWrapper.select(getVisibleChild()).setCss("-webkit-transform", "translate3d(" + translationValue + "px,0,0)");
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
            getVisibleChild().getElement().getStyle().setZIndex(251);
        } else {
            previousWidget.getElement().getStyle().setZIndex(250);
            getVisibleChild().getElement().getStyle().setZIndex(251);
        }

        if (isNext && getWidgetCount() > 2) {
            JQueryWrapper.select(nextWidget).setCss("-webkit-transform", "translate3d(" + (translationValue + getVisibleChild().getOffsetWidth()) + "px,0,0)");
        }

        nextWidget.getElement().getStyle().setVisibility(isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);
        previousWidget.getElement().getStyle().setVisibility(!isNext || nextWidget == previousWidget ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private Widget getNextWidget() {
        int index = getWidgetIndex(getVisibleChild());
        return getWidget((index + 1) % getWidgetCount());
    }

    private Widget getPreviousWidget() {
        int index = getWidgetIndex(getVisibleChild());
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

}
