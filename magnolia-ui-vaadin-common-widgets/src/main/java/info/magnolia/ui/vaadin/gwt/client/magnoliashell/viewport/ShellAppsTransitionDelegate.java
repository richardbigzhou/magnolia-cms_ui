/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.SlideAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.Util;

/**
 * The ShellAppsTransitionDelegate provides custom transition logic when activating viewport or a
 * specific app. It also defines its own slide and fade transitions (not those from JQueryWrapper)
 * because it might animate other CSS properties and can then result in CSS3 transitions through the
 * jquery.animate-enhanced.min.js plugin.
 */
public class ShellAppsTransitionDelegate implements TransitionDelegate {

    private final static int SLIDE_DURATION = 600;

    private final static int FADE_DURATION = 600;

    private final static int ALPHA_MIN = 0;

    private final static int ALPHA_MAX = 1;

    private SlideAnimation slideUpAnimation;

    private SlideAnimation slideDownAnimation;

    private FadeAnimation fadeOutAnimation;

    private FadeAnimation fadeInAnimation;

    private ShellAppsViewportWidget viewport;

    public ShellAppsTransitionDelegate(final ShellAppsViewportWidget viewport) {
        this.viewport = viewport;
        initAnimations();
    }

    private void initAnimations() {
        this.fadeInAnimation = new FadeAnimation(ALPHA_MAX, false) {
            @Override
            protected void onStart() {
                super.onStart();
                Style style = getCurrentElement().getStyle();
                String currentOpacity = style.getOpacity();
                if (currentOpacity == null || currentOpacity.isEmpty()) {
                    style.setOpacity(0d);
                }
                style.clearDisplay();
            }
        };
        this.fadeInAnimation.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                viewport.onShellAppLoaded(Util.<Widget>findWidget(query.get(0), null));
            }
        });


        this.slideUpAnimation = new SlideAnimation(false);
        this.slideDownAnimation = new SlideAnimation(false);

        this.slideDownAnimation.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                if (!slideDownAnimation.isCancelled()) {
                    viewport.onShellAppLoaded(viewport.getVisibleChild());
                }
            }
        });

        this.slideUpAnimation.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                if (!slideUpAnimation.isCancelled()) {
                    viewport.onShellAppsHidden();
                }
            }
        });
        this.fadeOutAnimation = new FadeAnimation(ALPHA_MIN, false);
    }

    /**
     * Slides down if active, fades out if inactive - except if the viewport is closing.
     */
    @Override
    public void setActive(final ViewportWidget viewport, boolean active) {
        final Element viewportElement = viewport.getElement();
        if (active) {
            boolean isViewportCurrentlyVisible = viewport.isVisible();
            viewport.setVisible(true);
            // The Shell App viewport might not be visible due 'display' value,
            // but it's top could still be equal to 0.
            if (!isViewportCurrentlyVisible) {
                viewportElement.getStyle().setTop(-viewportElement.getOffsetHeight(), Style.Unit.PX);
            }
            slideUpAnimation.cancel();
            slideDownAnimation.setTargetValue(0);
            slideDownAnimation.run(SLIDE_DURATION, viewportElement);
        } else {
            slideDownAnimation.cancel();
            slideUpAnimation.setTargetValue(-viewport.getOffsetHeight());
            slideUpAnimation.run(SLIDE_DURATION, viewportElement);
        }
    }

    /**
     * Cross-fades between shell apps.
     */
    @Override
    public void setVisibleChild(final ViewportWidget viewport, final Widget visibleChild) {
        if (viewport.getVisibleChild() == null || !((ShellAppsViewportWidget)viewport).isActive()) {
            // do not fade if first widget or viewport not active yet
            viewport.showChildNoTransition(visibleChild);
        } else {
            fadeInAnimation.run(FADE_DURATION, visibleChild.getElement());
            fadeOutAnimation.run(FADE_DURATION, viewport.getVisibleChild().getElement());
        }
    }

    @Override
    public boolean inProgress() {
        return fadeInAnimation.isRunning() ||
               fadeOutAnimation.isRunning() ||
               slideDownAnimation.isRunning() ||
               slideUpAnimation.isRunning();
    }
}
