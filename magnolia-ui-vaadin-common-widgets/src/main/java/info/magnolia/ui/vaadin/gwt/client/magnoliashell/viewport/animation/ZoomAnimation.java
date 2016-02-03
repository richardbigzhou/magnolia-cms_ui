/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * CSS3 Zoom Animation.
 */
public class ZoomAnimation extends Animation {

    private final static String ZOOM_OUT_CLASS_NAME = "zoom-out";

    private final static String ZOOM_IN_CLASS_NAME = "zoom-in";

    private boolean isZoomIn;

    private Element element;

    public ZoomAnimation(boolean isZoomIn) {
        this.isZoomIn = isZoomIn;
    }

    @Override
    public void run(final int duration, final double startTime, final Element element) {
        this.element = element;
        element.getStyle().setVisibility(Style.Visibility.HIDDEN);
        if (isZoomIn) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    doRun(element, duration, startTime);
                }
            });
        } else {
            doRun(element, duration, startTime);
        }
    }

    private void doRun(Element element, int duration, double startTime) {
        element.getStyle().setVisibility(Style.Visibility.VISIBLE);
        element.addClassName(isZoomIn ? ZOOM_IN_CLASS_NAME : ZOOM_OUT_CLASS_NAME);
        ZoomAnimation.super.run(duration, startTime, element);
    }

    @Override
    protected void onComplete() {
        element.removeClassName(ZoomAnimation.this.isZoomIn ? ZOOM_IN_CLASS_NAME : ZOOM_OUT_CLASS_NAME);
        super.onComplete();
    }

    @Override
    protected void onUpdate(double progress) {
    }

    public Element getElement() {
        return element;
    }
}
