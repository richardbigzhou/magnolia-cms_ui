/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.ShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.AppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.MagnoliaSwipeRecognizer;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.SlideAnimation;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
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
public class AppsViewportWidget extends ViewportWidget<AppsTransitionDelegate> implements HasSwipeHandlers {

    public static final String APP_INACTIVE_CLASS_NAME = "app-inactive";

    /**
     * Listener interface for {@link AppsViewportWidget}.
     */
    public interface Listener {

        void closeCurrentApp();

        void activateApp(Widget appWidget);

    };

    private static final int SWIPE_OUT_THRESHOLD = 300;

    private final AppPreloader preloader = new AppPreloader();

    private boolean isAppClosing = false;

    private boolean isCurtainVisible = false;

    private Listener listener;

    private final TouchDelegate delegate = new TouchDelegate(this);

    private final FullScreenButton fullScreenButton = new FullScreenButton();

    private Element curtain = DOM.createDiv();

    /**
     * Checks whether there are enough apps to navigate to next/previous app (> 1), that some app is loaded and displayed and
     * that no animation is currently in progress.
     */
    public boolean readyForAppSwipeOrShortcutNavigation() {
        return ShellState.get().isAppStarted() && getWidgetCount() > 1 && !getTransitionDelegate().inProgress();
    }

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

            fullScreenButton.getElement().addClassName("icon-extend-header");
            fullScreenButton.getElement().removeClassName("icon-collapse-header");
        } else {

            // disable fullscreen
            RootPanel.get().removeStyleName("fullscreen");

            fullScreenButton.getElement().addClassName("icon-collapse-header");
            fullScreenButton.getElement().removeClassName("icon-extend-header");
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
            processSwipe(-1);
            switchToApp(DIRECTION.RIGHT_TO_LEFT);
        }
    }

    public void goToPreviousApp() {
        if (getWidgetCount() > 1) {
            processSwipe(1);
            switchToApp(DIRECTION.LEFT_TO_RIGHT);
        }
    }

    /**
     * Get the app that is currently open.
     * @return The currently open app or null if none are open.
     */
    public Widget getCurrentApp(){
        if (getWidgetCount() < 1) {
            return null;
        }else{
            for (int w=0; w < getWidgetCount(); w++){
                Widget app = getWidget(w);
                String style = app.getStyleName();
                if (!app.getStyleName().contains("app-inactive")){
                    return app;
                }
            }
        }
        // No app is active.
        return null;
    }

    public Element getCurtain() {
        return curtain;
    }

    public void setCurtainVisible(boolean visible) {
        if (isCurtainVisible != visible) {
            this.isCurtainVisible = visible;
            getTransitionDelegate().setCurtainVisible(isCurtainVisible);
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
            formerVisible.addStyleName(APP_INACTIVE_CLASS_NAME);
        }
        w.setVisible(true);
        w.removeStyleName(APP_INACTIVE_CLASS_NAME);
        w.getElement().getStyle().clearVisibility();
    }

    @Override
    public void removeChild(Widget w) {
        getTransitionDelegate().removeWidget(w);
        getElement().removeChild(closeButton.getElement());
        getElement().removeChild(fullScreenButton.getElement());
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
                if (getWidgetCount() > 1) {
                    final SwipeEvent.DIRECTION direction = event.getDirection();
                    if (event.isDistanceReached()) {
                        switchToApp(direction);
                    } else {
                        final Widget visibleChild = getVisibleChild();
                        final SlideAnimation slideAnimation = new SlideAnimation(false, true);
                        slideAnimation.setTargetValue(0);
                        slideAnimation.run(500, visibleChild.getElement());
                        slideAnimation.addCallback(new JQueryCallback() {
                            @Override
                            public void execute(JQueryWrapper query) {
                                // do not trigger transitions
                                dropZIndeces();
                                Widget next = getNextWidget();
                                Widget previous = getPreviousWidget();
                                if (next != null) {
                                    next.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                                }

                                if (previous != null) {
                                    previous.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                                }
                            }
                        });
                    }
                }
            }
        });

        delegate.addTouchCancelHandler(new TouchCancelHandler() {
            @Override
            public void onTouchCanceled(TouchCancelEvent event) {
                dropZIndeces();
                Widget next = getNextWidget();
                Widget previous = getPreviousWidget();
                if (next != null) {
                    next.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                }

                if (previous != null) {
                    previous.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                }

                getVisibleChild().getElement().getStyle().clearLeft();
            }
        });
    }

    private void switchToApp(final SwipeEvent.DIRECTION direction) {

        final Widget newVisibleWidget = direction == DIRECTION.LEFT_TO_RIGHT ? getPreviousWidget() : getNextWidget();
        SlideAnimation slideAnimation = new SlideAnimation(false, true);
        slideAnimation.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                // do not trigger transitions
                showChild(newVisibleWidget);
                dropZIndeces();
                listener.activateApp(newVisibleWidget);
            }
        });

        Element targetElement = getVisibleChild().getElement();
        slideAnimation.setTargetValue(getOffsetWidth() * (direction == DIRECTION.LEFT_TO_RIGHT ? 1 : -1));
        slideAnimation.run(450, targetElement);

        if (direction == DIRECTION.RIGHT_TO_LEFT && getWidgetCount() > 2) {
            final SlideAnimation slideNewVisibleToEdgeAnimation = new SlideAnimation(false, true);
            slideNewVisibleToEdgeAnimation.setTargetValue(0);
            slideNewVisibleToEdgeAnimation.run(500, newVisibleWidget.getElement());
        }

    }

    private void processSwipe(int translationValue) {
        if (getWidgetCount() > 1) {
            JQueryWrapper.select(getVisibleChild()).setCss("-webkit-transform", "translate3d(" + translationValue + "px,0,0)");
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
