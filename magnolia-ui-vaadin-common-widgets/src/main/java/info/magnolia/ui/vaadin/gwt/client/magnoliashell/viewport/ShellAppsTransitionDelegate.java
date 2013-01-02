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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.Callbacks;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

/**
 * The ShellAppsTransitionDelegate provides custom transition logic when activating viewport or a
 * specific app. It also defines its own slide and fade transitions (not those from JQueryWrapper)
 * because it might animate other CSS properties and can then result in CSS3 transitions through the
 * jquery.transition.js plugin.
 */
class ShellAppsTransitionDelegate implements TransitionDelegate {

    private final static int SLIDE_DURATION = 600;

    private final static int FADE_DURATION = 600;

    private final static int ALPHA_MIN = 0;

    private final static int ALPHA_MAX = 1;

    private boolean viewportReady = true;

    private boolean visibleAppReady = true;

    /**
     * Slides down if active, fades out if inactive - except if the viewport is closing.
     */
    @Override
    public void setActive(final ViewportWidget viewport, boolean active) {
        final Callbacks callbacks = Callbacks.create();

        if (active) {
            viewport.setClosing(false);
            viewportReady = false;
            callbacks.add(new JQueryCallback() {
                @Override
                public void execute(JQueryWrapper query) {
                    viewportReady = true;
                    refreshShellApp((ShellAppsViewportWidget) viewport);
                }
            });
            slideDown(viewport, callbacks);
            // viewport.iLayout();
        } else {
            // slide up only if closing shell app
            if (viewport.isClosing()) {
                callbacks.add(new JQueryCallback() {

                    @Override
                    public void execute(JQueryWrapper query) {
                        viewport.setClosing(false);
                    }
                });
                slideUp(viewport, callbacks);
            } else {
                fadeOut(viewport, callbacks);
            }

        }
    }

    /**
     * Cross-fades between shell apps.
     */
    @Override
    public void setVisibleApp(final ViewportWidget viewport, final Widget app) {
        final Callbacks callbacks = Callbacks.create();

        if (viewport.getVisibleApp() == null || !viewport.isActive()) {
            // do not fade if first widget or viewport not active yet
            viewport.doSetVisibleApp(app);
            callbacks.fire();
        } else {
            // do not trigger callbacks twice, only for visible app
            fadeOut(viewport.getVisibleApp(), Callbacks.create());
            visibleAppReady = false;
            callbacks.add(new JQueryCallback() {

                @Override
                public void execute(JQueryWrapper query) {
                    visibleAppReady = true;
                    refreshShellApp((ShellAppsViewportWidget) viewport);
                }
            });
            fadeIn(app, callbacks);
            // viewport.iLayout();
        }
    }

    private void refreshShellApp(ShellAppsViewportWidget viewport) {
        if (viewportReady && visibleAppReady) {
            viewport.refreshShellApp();
        }
    }

    private void slideDown(final ViewportWidget viewport, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(viewport);

        // init
        viewport.setVisible(true);
        if (jq.is(":animated")) {
            jq.stop();
            // reset opacity if animation was a fade out
            viewport.getElement().getStyle().clearOpacity();
            GWT.log(viewport.getStyleName() + ": stopping animation");
        } else {
            viewport.getElement().getStyle().setTop(-viewport.getOffsetHeight() + 60, Unit.PX);
        }

        // callback
        callbacks.add(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                viewport.getElement().getStyle().clearTop();
                GWT.log(viewport.getStyleName() + ": ACTIVE callback");
            }

        });

        // animate
        jq.animate(SLIDE_DURATION, new AnimationSettings() {

            {
                setProperty("top", "60px");
                setCallbacks(callbacks);
            }
        });
    }

    private void slideUp(final ViewportWidget viewport, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(viewport);

        // init
        if (jq.is(":animated")) {
            jq.stop();
            GWT.log(viewport.getStyleName() + ": stopping animation");
        }

        // callback
        callbacks.add(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                viewport.setVisible(false);
                viewport.getElement().getStyle().clearTop();
                GWT.log(viewport.getStyleName() + ": INACTIVE callback");
            }

        });

        // animate
        jq.animate(SLIDE_DURATION, new AnimationSettings() {

            {
                setProperty("top", (-viewport.getOffsetHeight() + 60) + "px");
                setCallbacks(callbacks);
            }
        });
    }

    private void fadeIn(final Widget w, final Callbacks callbacks) {

        JQueryWrapper jq = JQueryWrapper.select(w);
        final String debugId = jq.attr("id");

        // init
        if (jq.is(":animated")) {
            jq.stop();
            GWT.log(debugId + ": stopping animation");
        } else {
            w.getElement().getStyle().setOpacity(ALPHA_MIN);
            w.setVisible(true);
        }

        // callback
        callbacks.add(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                w.getElement().getStyle().clearOpacity();
                GWT.log(debugId + ": ACTIVE callback");
            }

        });

        // animate
        jq.animate(FADE_DURATION, new AnimationSettings() {

            {
                setProperty("opacity", ALPHA_MAX);
                setCallbacks(callbacks);
            }
        });
    }

    /**
     * FADE OUT TRANSITION.
     * 
     * @param w
     *            the app widget
     * @param callbacks
     *            the callbacks
     */
    private void fadeOut(final Widget w, final Callbacks callbacks) {

        JQueryWrapper jq = JQueryWrapper.select(w);
        final String debugId = jq.attr("id");

        // init
        if (jq.is(":animated")) {
            jq.stop();
            GWT.log(debugId + ": stopping animation");
        } else {
            w.getElement().getStyle().setOpacity(ALPHA_MAX);
        }

        // callback
        callbacks.add(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper jq) {
                w.setVisible(false);
                w.getElement().getStyle().clearOpacity();
                GWT.log(debugId + ": INACTIVE callback");
            }
        });

        // animate
        jq.animate(FADE_DURATION, new AnimationSettings() {
            {
                setProperty("opacity", ALPHA_MIN);
                setCallbacks(callbacks);
            }
        });
    }

}