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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

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

    /**
     * Slides down if active, fades out if inactive - except if the viewport is closing.
     */
    @Override
    public Callbacks setActive(final VShellViewport viewport, boolean active) {
        Callbacks callbacks = null;

        if (active) {
            viewport.setClosing(false);
            callbacks = slideDown(viewport);
        } else {

            if (viewport.isClosing()) {
                // slide up only if closing shell app
                callbacks = slideUp(viewport);
                callbacks.add(new JQueryCallback() {

                    @Override
                    public void execute(JQueryWrapper query) {
                        viewport.setClosing(false);
                    }
                });
            } else {
                callbacks = fadeOut(viewport);
            }

        }
        return callbacks;
    }

    /**
     * Cross-fades between shell apps.
     */
    @Override
    public Callbacks setVisibleApp(VShellViewport viewport, final Widget app) {
        Callbacks callbacks = null;
        if (viewport.getVisibleApp() == null || !viewport.isActive()) {
            // do not fade if first widget or viewport not active yet
            viewport.doSetVisibleApp(app);
        } else {
            fadeOut(viewport.getVisibleApp());
            callbacks = fadeIn(app);
        }
        return callbacks;
    }

    /**
     * SLIDE DOWN TRANSITION
     * 
     * @param viewport the viewport widget
     * @return the jquery callbacks
     */
    private Callbacks slideDown(final VShellViewport viewport) {
        JQueryWrapper jq = JQueryWrapper.select(viewport);

        // init
        if (jq.is(":animated")) {
            jq.stop();
            // reset opacity if animation was a fade out
            viewport.getElement().getStyle().clearOpacity();
            GWT.log(viewport.getStyleName() + ": stopping animation");
        } else {
            viewport.getElement().getStyle().setTop(-viewport.getOffsetHeight() + 60, Unit.PX);
        }
        viewport.setVisible(true);

        // callback
        final Callbacks callbacks = Callbacks.create();
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
        return callbacks;
    }

    /**
     * SLIDE UP TRANSITION
     * 
     * @param viewport the viewport widget
     * @return the jquery callbacks
     */
    private Callbacks slideUp(final VShellViewport viewport) {
        JQueryWrapper jq = JQueryWrapper.select(viewport);

        // init
        if (jq.is(":animated")) {
            jq.stop();
            GWT.log(viewport.getStyleName() + ": stopping animation");
        }

        // callback
        final Callbacks callbacks = Callbacks.create();
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
        return callbacks;
    }

    /**
     * FADE IN TRANSITION
     * 
     * @param w the app widget
     * @return the jquery callbacks
     */
    private Callbacks fadeIn(final Widget w) {

        JQueryWrapper jq = JQueryWrapper.select(w);
        final String debugId = jq.attr("id");

        // init
        if (jq.is(":animated")) {
            jq.stop();
            GWT.log(debugId + ": stopping animation");
        } else {
            // if (!jq.is(":visible")) {
            w.getElement().getStyle().setOpacity(ALPHA_MIN);
            w.setVisible(true);
        }

        // callback
        final Callbacks callbacks = Callbacks.create();
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
        return callbacks;
    }

    /**
     * FADE OUT TRANSITION
     * 
     * @param w the app widget
     * @return the jquery callbacks
     */
    private Callbacks fadeOut(final Widget w) {

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
        final Callbacks callbacks = Callbacks.create();
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
        return callbacks;
    }

}