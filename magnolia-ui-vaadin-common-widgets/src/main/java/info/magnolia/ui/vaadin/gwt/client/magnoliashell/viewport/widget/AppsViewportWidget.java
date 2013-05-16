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

import info.magnolia.ui.vaadin.gwt.client.CloseButton;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.AppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Client side implementation of Apps viewport.
 */
public class AppsViewportWidget extends ViewportWidget {

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

    //private final TouchDelegate delegate = new TouchDelegate(this);

    private final ClickHandler closeHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (!isAppClosing()) {
                isAppClosing = true;
                listener.closeCurrentApp();

            }
        }
    };

    private CloseButton closeButton = new CloseButton(closeHandler);

    private Element curtain = DOM.createDiv();

    public AppsViewportWidget(final Listener listener) {
        super();
        this.listener = listener;
        curtain.setClassName("v-curtain v-curtain-green");
        closeButton.addStyleDependentName("app");
        //bindTouchHandlers();
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
        add(closeButton, w.getElement());
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
        if (getWidgetCount() == 2) {
            remove(closeButton);
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
}
