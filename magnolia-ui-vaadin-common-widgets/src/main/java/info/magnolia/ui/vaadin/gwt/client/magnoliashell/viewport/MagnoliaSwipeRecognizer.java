/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HasHandlers;
import com.googlecode.mgwt.dom.client.event.touch.Touch;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.recognizer.EventPropagator;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEndEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeEvent.DIRECTION;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeMoveEvent;
import com.googlecode.mgwt.dom.client.recognizer.swipe.SwipeStartEvent;

/**
 * MagnoliaSwipeHandler.
 */
public class MagnoliaSwipeRecognizer implements TouchHandler {

    private static final int DEFAULT_TOUCH_TRESHOLD = 3;

    private static EventPropagator DEFAULT_EVENT_PROPAGATOR;

    private final HasHandlers source;

    private EventPropagator eventPropagator;

    private final int minDistance;

    private final int threshold;

    private int touchCount;

    private int desiredTouchCount;

    private enum State {
        INVALID, READY, FINDER_DOWN, FOUND_DIRECTION
    }

    private State state;

    private DIRECTION direction;

    private int lastDistance;

    private int touchStartX = Integer.MAX_VALUE;

    private int touchStartY = Integer.MIN_VALUE;

    public MagnoliaSwipeRecognizer(HasHandlers source) {
        this(source, 40);
    }

    public MagnoliaSwipeRecognizer(HasHandlers source, int minDistance) {
        this(source, minDistance, 10);
    }

    public MagnoliaSwipeRecognizer(HasHandlers source, int minDistance, int threshold) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }

        if (minDistance <= 0 || minDistance < threshold) {
            throw new IllegalArgumentException("minDistance > 0 and minDistance > threshold");
        }

        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold > 0");
        }

        this.desiredTouchCount = DEFAULT_TOUCH_TRESHOLD;
        this.source = source;
        this.minDistance = minDistance;
        this.threshold = threshold;
        this.touchCount = 0;
        this.state = State.READY;
    }

    @Override
    public void onTouchStart(TouchStartEvent event) {
        touchCount = event.getTouches().length();
        switch (state) {
        case INVALID:
            break;
        case READY:
            if (touchCount == desiredTouchCount) {
                state = State.FINDER_DOWN;
                touchStartX = event.getTouches().get(0).getPageX();
                touchStartY = event.getTouches().get(0).getPageY();
            }
            break;
        case FINDER_DOWN:
        default:
            break;
        }
    }

    @Override
    public void onTouchMove(TouchMoveEvent event) {
        Touch touch = event.getTouches().get(0);

        switch (state) {
        case INVALID:
        case READY:
            break;
        case FINDER_DOWN:
            if (Math.abs(touch.getPageX() - touchStartX) >= threshold) {
                state = State.FOUND_DIRECTION;
                direction = touch.getPageX() - touchStartX > 0 ? DIRECTION.LEFT_TO_RIGHT : DIRECTION.RIGHT_TO_LEFT;
                SwipeStartEvent swipeStartEvent = new SwipeStartEvent(touch, touch.getPageX() - touchStartX, direction);
                getEventPropagator().fireEvent(source, swipeStartEvent);

            } else {
                if (Math.abs(touch.getPageY() - touchStartY) >= threshold) {
                    state = State.FOUND_DIRECTION;
                    direction = touch.getPageY() - touchStartY > 0 ? DIRECTION.TOP_TO_BOTTOM : DIRECTION.BOTTOM_TO_TOP;
                    SwipeStartEvent swipeStartEvent = new SwipeStartEvent(touch, touch.getPageY() - touchStartY, direction);
                    getEventPropagator().fireEvent(source, swipeStartEvent);

                }
            }
            break;

        case FOUND_DIRECTION:
            DIRECTION currentDirection = null;
            switch (direction) {
            case TOP_TO_BOTTOM:
            case BOTTOM_TO_TOP:
                lastDistance = Math.abs(touch.getPageY() - touchStartY);
                currentDirection = touch.getPageY() - touchStartY > 0 ? DIRECTION.TOP_TO_BOTTOM : DIRECTION.BOTTOM_TO_TOP;
                break;
            case LEFT_TO_RIGHT:
            case RIGHT_TO_LEFT:
                lastDistance = Math.abs(touch.getPageX() - touchStartX);
                currentDirection = touch.getPageX() - touchStartX > 0 ? DIRECTION.LEFT_TO_RIGHT : DIRECTION.RIGHT_TO_LEFT;
                break;
            default:
                break;
            }
            getEventPropagator().fireEvent(source, new SwipeMoveEvent(touch, lastDistance > minDistance, lastDistance, currentDirection));
            break;
        default:
            break;
        }

    }

    @Override
    public void onTouchEnd(TouchEndEvent event) {
        touchCount = event.getTouches().length();

        switch (state) {
        case FOUND_DIRECTION:
            if (touchCount < desiredTouchCount) {
                getEventPropagator().fireEvent(source, new SwipeEndEvent(lastDistance > minDistance, lastDistance, direction));
                reset();
            }
            break;

        default:
            reset();
            break;
        }

    }

    @Override
    public void onTouchCanceled(TouchCancelEvent event) {
        touchCount--;
        if (touchCount <= 0) {
            reset();
        }

    }

    public int getThreshold() {
        return threshold;
    }

    public int getMinDistance() {
        return minDistance;
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
        state = State.READY;
        touchCount = 0;
    }

}
