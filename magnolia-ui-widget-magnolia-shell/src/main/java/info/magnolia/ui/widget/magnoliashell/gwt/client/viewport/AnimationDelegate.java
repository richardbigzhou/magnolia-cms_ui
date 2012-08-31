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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.Widget;


/**
 * Viewports might have different ways of displaying the content. This interface helps to define
 * them from outside.
 */
public interface AnimationDelegate {

    final static int FADE_SPEED = 400;

    final static int SLIDE_IN_SPEED = 600;

    final static int SLIDE_OUT_SPEED = 600;

    void show(final Widget w, final Callbacks callbacks);

    void hide(final Widget w, final Callbacks callbacks);

    final static AnimationDelegate SLIDING_DELEGATE = new AnimationDelegate() {

        @Override
        public void show(final Widget w, final Callbacks callbacks) {
            // first reset inline style to calculate initial and target positions
            w.getElement().getStyle().setProperty("opacity", "");
            w.getElement().getStyle().setProperty("top", "");
            final JQueryWrapper jq = JQueryWrapper.select(w);
            final int initialTop = Integer.valueOf(jq.css("top").replaceAll("px", ""));
            jq.setCssPx("top", -w.getOffsetHeight() + initialTop);

            // only then set it visible
            w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI);

            jq.animate(SLIDE_IN_SPEED, new AnimationSettings() {

                {
                    setProperty("top", "+=" + w.getOffsetHeight());
                    callbacks.add(new JQueryCallback() {

                        @Override
                        public void execute(JQueryWrapper query) {
                            // remove inline style when animation complete
                            w.getElement().getStyle().setProperty("top", "");
                        }

                    });
                    setCallbacks(callbacks);
                }
            });
        }

        @Override
        public void hide(final Widget w, final Callbacks callbacks) {
            // keep z-index top-most because other viewport may immediately take z-index hi
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI + 10);

            final JQueryWrapper jq = JQueryWrapper.select(w);
            jq.animate(SLIDE_OUT_SPEED, new AnimationSettings() {

                {
                    setProperty("top", "-=" + w.getOffsetHeight());
                    callbacks.add(new JQueryCallback() {

                        @Override
                        public void execute(JQueryWrapper query) {
                            // set hidden once animation complete
                            w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_LO);
                        }

                    });
                    setCallbacks(callbacks);
                }
            });
        }

        @Override
        public String toString() {
            return "SLIDING_DELEGATE";
        };
    };

    final static AnimationDelegate FADING_DELEGATE = new AnimationDelegate() {

        @Override
        public void show(final Widget w, final Callbacks callbacks) {
            final JQueryWrapper jq = JQueryWrapper.select(w);
            jq.setCss("opacity", "0");

            w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI);

            jq.animate(FADE_SPEED, new AnimationSettings() {

                {
                    setProperty("opacity", 1d);
                    setCallbacks(callbacks);
                }
            });
        }

        @Override
        public void hide(final Widget w, final Callbacks callbacks) {
            // keep z-index top-most because other viewport may immediately take z-index hi
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI + 10);

            JQueryWrapper.select(w).animate(FADE_SPEED, new AnimationSettings() {

                {
                    setProperty("opacity", 0);
                    callbacks.add(new JQueryCallback() {

                        @Override
                        public void execute(JQueryWrapper query) {
                            // set hidden once animation complete
                            w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_LO);
                        }

                    });
                    setCallbacks(callbacks);
                }
            });
        }

        @Override
        public String toString() {
            return "FADING_DELEGATE";
        };
    };

    final static AnimationDelegate ZOOMING_DELEGATE = new AnimationDelegate() {

        @Override
        public void show(final Widget w, final Callbacks callbacks) {
            w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI);

            w.removeStyleName("zoom-out");
            w.addStyleName("zoom-in");
            callbacks.fire();
        }

        @Override
        public void hide(final Widget w, final Callbacks callbacks) {
            // keep z-index top-most because other viewport may immediately take z-index hi
            w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_HI + 10);

            w.removeStyleName("zoom-in");
            w.addStyleName("zoom-out");
            callbacks.add(new JQueryCallback() {

                @Override
                public void execute(JQueryWrapper query) {
                    // set hidden once animation complete
                    w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                    w.getElement().getStyle().setZIndex(VShellViewport.Z_INDEX_LO);
                }

            });

            JQueryWrapper.select(w).animate(FADE_SPEED, new AnimationSettings() {

                {
                    setProperty("opacity", 1d);
                    setCallbacks(callbacks);
                }
            });
        }

        @Override
        public String toString() {
            return "ZOOMING_DELEGATE";
        };
    };
}
