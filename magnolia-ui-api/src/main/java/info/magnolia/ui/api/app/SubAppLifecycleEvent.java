/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.api.app;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;

/**
 * Event fired when the lifecycle of a sub-app changes.
 *
 */
public class SubAppLifecycleEvent implements Event<SubAppLifecycleEvent.Handler> {

    /**
     * Handler interface.
     */
    public interface Handler extends EventHandler {

        void onSubAppFocused(SubAppLifecycleEvent event);

        void onSubAppStopped(SubAppLifecycleEvent event);

        void onSubAppStarted(SubAppLifecycleEvent event);

        /**
         * Simple stub so in case not all the methods need to be implemented.
         */
        public static class Adapter implements Handler {

            @Override
            public void onSubAppFocused(SubAppLifecycleEvent event) {}

            @Override
            public void onSubAppStopped(SubAppLifecycleEvent event) {}

            @Override
            public void onSubAppStarted(SubAppLifecycleEvent event) {}

        }

    }

    /**
     * Possible lifecycle states.
     */
    public static enum Type {
        STARTED, STOPPED, FOCUSED;
    }

    public SubAppLifecycleEvent(SubAppContext subAppContext, Type type) {
        this.subAppContext = subAppContext;
        this.eventType = type;
    }
    private SubAppContext subAppContext;

    private Type eventType;

    @Override
    public void dispatch(Handler handler) {
        if (eventType == null) {
            return;
        }

        switch (eventType) {
        case STARTED:
            handler.onSubAppStarted(this);
            break;
        case FOCUSED:
            handler.onSubAppFocused(this);
            break;
        case STOPPED:
            handler.onSubAppStopped(this);
            break;
        }
    }

    public Type getEventType() {
        return eventType;
    }

    public SubAppContext getSubAppContext() {
        return subAppContext;
    }
}
