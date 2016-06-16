/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.ShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.ZoomAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Util;

/**
 * The AppsTransitionDelegate provides custom transition logic when launching, closing an app, or
 * switching between apps.
 */
public class AppsTransitionDelegate implements TransitionDelegate {

    private static final Logger log = Logger.getLogger(AppsTransitionDelegate.class.getName());

    private static final double CURTAIN_ALPHA = 0.9;

    private static final int CURTAIN_FADE_IN_DURATION = 500;

    private static final int CURTAIN_FADE_OUT_DURATION = 800;

    private static final int CURTAIN_FADE_OUT_DELAY = 200;

    private static final int ZOOM_DURATION = 500;

    private final AppsViewportWidget viewport;
    
    /**
     * Since we reveal the already loaded apps instantly - we could use a lock
     * that ensures the zooming animation is distracted with Vaadin layout.
     */
    private Object lock = new Object();

    /**
     * If response handling has already started we cannot lock anything and cannot prevent scheduled {@link com.vaadin.client.LayoutManager} runs
     * which might spoil the layout of a zoomed app. In order to prevent that
     */
    private boolean isResponseProcessingInProgress = false;

    private ZoomAnimation zoomOutAnimation = new ZoomAnimation(false) {
        @Override
        protected void onComplete() {
            super.onComplete();
            viewport.removeChildNoTransition(Util.<Widget>findWidget(getElement(), null));
        }
    };

    private ZoomAnimation zoomInAnimation = new ZoomAnimation(true) {
        @Override
        protected void onStart() {
            super.onStart();
            setCurtainAttached(false);
            Util.findConnectorFor(viewport).getConnection().suspendReponseHandling(lock);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            ShellState.get().setAppStarted();
            log.warning("Switching to 'APP STARTED' state after zoom-in animation");
            Util.findConnectorFor(viewport).getConnection().resumeResponseHandling(lock);
        }
    };

    private FadeAnimation curtainFadeOutAnimation = new FadeAnimation(0, true);
    private FadeAnimation curtainFadeInAnimation = new FadeAnimation(CURTAIN_ALPHA, true) {
        @Override
        protected void onStart() {
            super.onStart();
            getJQueryWrapper().get(0).getStyle().setOpacity(0d);
        }
    };

    private Command pendingAppZoomCommand = null;

    final ApplicationConnection.CommunicationHandler communicationHandler = new ApplicationConnection.CommunicationHandler() {

        @Override
        public void onRequestStarting(ApplicationConnection.RequestStartingEvent e) {}

        @Override
        public void onResponseHandlingStarted(ApplicationConnection.ResponseHandlingStartedEvent e) {
            isResponseProcessingInProgress = true;
        }

        @Override
        public void onResponseHandlingEnded(ApplicationConnection.ResponseHandlingEndedEvent e) {
            isResponseProcessingInProgress = false;
            if (pendingAppZoomCommand != null) {
                pendingAppZoomCommand.execute();
                pendingAppZoomCommand = null;
            }
        }
    };

    public AppsTransitionDelegate(final AppsViewportWidget viewportWidget) {
        this.viewport = viewportWidget;
        curtainFadeOutAnimation.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper jq) {
                setCurtainAttached(false);
            }
        });

        Util.findConnectorFor(viewportWidget).getConnection().addHandler(ApplicationConnection.ResponseHandlingStartedEvent.TYPE, communicationHandler);
        Util.findConnectorFor(viewportWidget).getConnection().addHandler(ApplicationConnection.ResponseHandlingEndedEvent.TYPE, communicationHandler);
    }

    /**
     * Zoom-in if switching to a different running app, from apps-launcher only
     * closing an app doesn't zoom-in the next app, running apps are all hidden explicitly except current one.
     */
    @Override
    public void setVisibleChild(final Widget app) {
        if (!viewport.isAppClosing() && isWidgetVisibilityHidden(app)) {
            viewport.showChildNoTransition(app);
            pendingAppZoomCommand = new Command() {
                @Override
                public void execute() {
                    Util.findConnectorFor(viewport).getLayoutManager().layoutNow();
                    zoomInAnimation.run(ZOOM_DURATION, app.getElement());
                }
            };

            if (!isResponseProcessingInProgress) {
                pendingAppZoomCommand.execute();
                this.pendingAppZoomCommand = null;
            }


        } else {
            viewport.showChildNoTransition(app);
            ShellState.get().setAppStarted();
            log.warning("Switching to 'APP STARTED' state without app transition");
        }
    }

    public void setCurtainVisible(boolean visible) {
        final Element curtain = viewport.getCurtain();
        if (visible) {
            setCurtainAttached(true);
            curtainFadeOutAnimation.cancel();
            curtainFadeInAnimation.run(CURTAIN_FADE_IN_DURATION, curtain);
        } else {
            curtainFadeInAnimation.cancel();
            curtainFadeOutAnimation.run(CURTAIN_FADE_OUT_DURATION + CURTAIN_FADE_OUT_DELAY, curtain);
        }
    }

    public void removeWidget(Widget w) {
        zoomOutAnimation.run(ZOOM_DURATION, w.getElement());
    }

    private boolean isWidgetVisibilityHidden(final Widget app) {
        return Visibility.HIDDEN.getCssName().equals(app.getElement().getStyle().getVisibility()) ||
                Style.Display.NONE.getCssName().equals(app.getElement().getStyle().getDisplay());
    }

    public void setCurtainAttached(boolean visible) {
        Element viewportElement = viewport.getElement();
        Element curtain = viewport.getCurtain();
        if (visible) {
            viewportElement.appendChild(curtain);
        } else if (viewportElement.isOrHasChild(curtain)) {
            viewportElement.removeChild(curtain);
        }
    }

    @Override
    public boolean inProgress() {
        return zoomInAnimation.isRunning() || zoomOutAnimation.isRunning();
    }

    @Override
    public void setActive(boolean active) {
        viewport.setVisible(active);
    }
}
