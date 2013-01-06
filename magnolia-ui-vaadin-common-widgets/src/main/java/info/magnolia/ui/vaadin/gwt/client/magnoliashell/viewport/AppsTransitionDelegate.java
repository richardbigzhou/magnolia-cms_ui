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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.TransitionDelegate.BaseTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

/**
 * The AppsTransitionDelegate provides custom transition logic when launching, closing an app, or
 * switching between apps.
 */
public class AppsTransitionDelegate extends BaseTransitionDelegate {

    private static final double CURTAIN_ALPHA = 0.9;

    private static final int CURTAIN_FADE_IN_DURATION = 500;

    private static final int CURTAIN_FADE_OUT_DURATION = 400;

    private static final int CURTAIN_FADE_OUT_DELAY = 200;

    private Object lock = new Object();
    
    @Override
    public void setVisibleApp(final ViewportWidget viewport, final Widget app) {
        // zoom-in if switching to a different running app, from appslauncher only
        // closing an app doesn't zoom-in the next app
        // running apps are all hidden explicitly except current one
        if (!viewport.isClosing() && isWidgetVisibilityHidden(app)) {
            viewport.doSetVisibleApp(app);
            Util.findConnectorFor(viewport).getConnection().suspendReponseHandling(lock);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    app.addStyleName("zoom-in");
                    new Timer() {
                        @Override
                        public void run() {
                            app.removeStyleName("zoom-in");
                            Util.findConnectorFor(viewport).getConnection().resumeResponseHandling(lock);
                        }
                    }.schedule(500);
                }
            });
        } else {
            viewport.doSetVisibleApp(app);
        }
    }

    public void setCurtainVisible(final AppsViewportWidget viewport, boolean visible) {
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

    public void removeWidget(final AppsViewportWidget viewport, final Widget w) {
        w.addStyleName("zoom-out");
        new Timer() {
            @Override
            public void run() {
                viewport.removeWidgetWithoutTransition(w);
            }
        }.schedule(500);
    }

    private boolean isWidgetVisibilityHidden(final Widget app) {
        return Visibility.HIDDEN.getCssName().equals(app.getElement().getStyle().getVisibility());
    }

    private final JQueryCallback opacityClearCallback = new JQueryCallback() {
        @Override
        public void execute(JQueryWrapper query) {
            query.get(0).getStyle().clearOpacity();
        }
    };

    private void fadeIn(final Element curtainEl, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(curtainEl);

        // init
        if (jq.is(":animated")) {
            jq.stop();
        } else {
            curtainEl.getStyle().setOpacity(0);
        }

        callbacks.add(opacityClearCallback);
        // animate
        jq.animate(CURTAIN_FADE_IN_DURATION, new AnimationSettings() {
            {
                setProperty("opacity", CURTAIN_ALPHA);
                setCallbacks(callbacks);
            }
        });
    }

    private void fadeOut(final Element curtainEl, final Callbacks callbacks) {
        JQueryWrapper jq = JQueryWrapper.select(curtainEl);

        // init
        if (jq.is(":animated")) {
            jq.stop();
        }

        // animate
        jq.animate(CURTAIN_FADE_OUT_DURATION, new AnimationSettings() {
            {
                setProperty("opacity", 0);
                setCallbacks(callbacks);
            }
        });
    }
}