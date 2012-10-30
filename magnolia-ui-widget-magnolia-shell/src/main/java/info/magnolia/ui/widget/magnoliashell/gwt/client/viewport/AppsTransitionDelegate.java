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
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.TransitionDelegate.BaseTransitionDelegate;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;


/**
 * The AppsTransitionDelegate provides custom transition logic when launching, closing an app, or
 * switching between apps.
 */
class AppsTransitionDelegate extends BaseTransitionDelegate {

    private static final double CURTAIN_ALPHA = 0.9;

    private static final int CURTAIN_FADE_IN_DURATION = 500;

    private static final int CURTAIN_FADE_OUT_DURATION = 400;

    private static final int CURTAIN_FADE_OUT_DELAY = 200;

    @Override
    public void setVisibleApp(VShellViewport viewport, final Widget app) {
        // zoom-in if switching to a different running app, from appslauncher only
        // closing an app doesn't zoom-in the next app
        // running apps are all hidden explicitely except current one
        if (!viewport.isClosing() && Visibility.HIDDEN.getCssName().equals(app.getElement().getStyle().getVisibility())) {
            viewport.doSetVisibleApp(app);

            app.addStyleName("zoom-in");
            new Timer() {

                @Override
                public void run() {
                    app.removeStyleName("zoom-in");
                }
            }.schedule(500);
        } else {
            viewport.doSetVisibleApp(app);
        }
    }

    public void setCurtainVisible(final VAppsViewport viewport, boolean visible) {
        final Element curtain = viewport.getCurtain();
        final Callbacks callbacks = Callbacks.create();

        if (visible) {
            // show curtain immediately
            viewport.doSetCurtainVisible(visible);
            fadeIn(curtain, callbacks);
        } else {
            // fade out after 200ms then remove curtain
            new Timer() {

                @Override
                public void run() {
                    callbacks.add(new JQueryCallback() {

                        @Override
                        public void execute(JQueryWrapper jq) {
                            viewport.doSetCurtainVisible(false);
                        }
                    });
                    fadeOut(curtain, callbacks);
                }
            }.schedule(CURTAIN_FADE_OUT_DELAY);
        }
    }

    public void removeWidget(final VAppsViewport viewport, final Widget w) {
        w.addStyleName("zoom-out");
        new Timer() {

            @Override
            public void run() {
                viewport.doRemoveWidget(w);
                viewport.setClosing(false);
            }
        }.schedule(500);
    }

    /**
     * FADE IN TRANSITION.
     * 
     * @param el the curtain element
     * @param callbacks the callbacks
     */
    private void fadeIn(final Element el, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(el);

        // init
        if (jq.is(":animated")) {
            jq.stop();
        } else {
            el.getStyle().setOpacity(0);
        }

        // callback
        callbacks.add(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                el.getStyle().clearOpacity();
            }

        });

        // animate
        jq.animate(CURTAIN_FADE_IN_DURATION, new AnimationSettings() {

            {
                setProperty("opacity", CURTAIN_ALPHA);
                setCallbacks(callbacks);
            }
        });
    }

    /**
     * FADE OUT TRANSITION.
     * 
     * @param el the curtain element
     * @param callbacks the callbacks
     */
    private void fadeOut(final Element el, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(el);

        // init
        if (jq.is(":animated")) {
            jq.stop();
        }

        // callback

        // animate
        jq.animate(CURTAIN_FADE_OUT_DURATION, new AnimationSettings() {

            {
                setProperty("opacity", 0);
                setCallbacks(callbacks);
            }
        });
    }
}