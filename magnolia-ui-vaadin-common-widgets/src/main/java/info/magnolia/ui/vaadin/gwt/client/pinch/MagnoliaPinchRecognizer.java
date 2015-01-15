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
package info.magnolia.ui.vaadin.gwt.client.pinch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HasHandlers;
import com.googlecode.mgwt.dom.client.event.touch.Touch;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.recognizer.EventPropagator;
import com.googlecode.mgwt.dom.client.recognizer.pinch.OffsetProvider;

/**
 * MagnoliaPinchRecognizer.
 */
public class MagnoliaPinchRecognizer implements TouchHandler {

    private static EventPropagator DEFAULT_EVENT_PROPAGATOR;

    private final HasHandlers source;

    private EventPropagator eventPropagator;

    private enum State {
        READY, INVALID, ONE_FINGER, TWO_FINGER;
    }

    private State state;

    private int startX1;

    private int startY1;

    private int startX2;

    private int startY2;

    private int touchCount;

    private double distance;

    private final OffsetProvider offsetProvider;

    public MagnoliaPinchRecognizer(HasHandlers source, OffsetProvider offsetProvider) {

        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }
        if (offsetProvider == null) {
            throw new IllegalArgumentException("offsetProvider can not be null");
        }

        this.source = source;
        this.offsetProvider = offsetProvider;
        state = State.READY;

    }

    @Override
    public void onTouchStart(TouchStartEvent event) {
        touchCount = event.getTouches().length();
        GWT.log("Touch start");
        switch (state) {
        case READY:
            // VConsole.log("One finger");
            startX1 = event.getTouches().get(0).getPageX();
            startY1 = event.getTouches().get(0).getPageY();
            state = State.ONE_FINGER;
            break;
        case ONE_FINGER:
            GWT.log("Two fingers");
            startX2 = event.getTouches().get(1).getPageX();
            startY2 = event.getTouches().get(1).getPageY();
            distance = (int) Math.sqrt(Math.pow(startX1 - startX2, 2) + Math.pow(startY1 - startY2, 2));
            state = State.TWO_FINGER;
            event.preventDefault();

            int x = (startX1 + startX2) / 2;
            int y = (startY1 + startY2) / 2;
            getEventPropagator().fireEvent(source, new MagnoliaPinchStartEvent(x, y, 1));
            break;

        default:
            state = State.INVALID;
            break;
        }

    }

    @Override
    public void onTouchMove(TouchMoveEvent event) {
        switch (state) {
        case TWO_FINGER:
            final Touch touch1 = event.getTouches().get(0);
            final Touch touch2 = event.getTouches().get(1);

            int left = offsetProvider.getLeft();
            int top = offsetProvider.getTop();

            int x1 = touch1.getPageX() - left;
            int y1 = touch1.getPageY() - top;
            int x2 = touch2.getPageX() - left;
            int y2 = touch2.getPageY() - top;

            double newDistance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

            int x = (x1 + x2) / 2;
            int y = (y1 + y2) / 2;
            getEventPropagator().fireEvent(source, new MagnoliaPinchMoveEvent(x, y, distance / newDistance));
            distance = newDistance;
            event.preventDefault();
            break;
        default:
            state = State.INVALID;
            break;
        }

    }

    @Override
    public void onTouchEnd(TouchEndEvent event) {
        touchCount--;
        if (touchCount <= 0) {
            reset();
        } else {
            if (state == State.TWO_FINGER) {
                state = State.ONE_FINGER;
            } else {
                if (touchCount == 2) {
                    state = State.TWO_FINGER;
                }
            }
        }

    }

    @Override
    public void onTouchCanceled(TouchCancelEvent event) {
        touchCount--;
        if (touchCount <= 0) {
            reset();
        } else {
            if (state == State.TWO_FINGER) {
                state = State.ONE_FINGER;
            } else {
                if (touchCount == 2) {
                    state = State.TWO_FINGER;
                }
            }
        }
    }

    protected EventPropagator getEventPropagator() {
        if (eventPropagator == null) {
            if (DEFAULT_EVENT_PROPAGATOR == null) {
                DEFAULT_EVENT_PROPAGATOR = GWT.create(EventPropagator.class);
            }
            eventPropagator = DEFAULT_EVENT_PROPAGATOR;
        }
        return eventPropagator;
    }

    protected void setEventPropagator(EventPropagator eventPropagator) {
        this.eventPropagator = eventPropagator;

    }

    private void reset() {
        touchCount = 0;
        state = State.READY;

    }

}
