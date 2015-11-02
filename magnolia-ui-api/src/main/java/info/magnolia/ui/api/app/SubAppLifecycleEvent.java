/**
 * This file Copyright (c) 2015 Magnolia International
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

/**
 * SubAppLifecycleEvent is fired on initiation, termination and activation/de-activation of a sub-app (the exact list of
 * supported lifecycle phases is provided by {@link Type}).
 * <p>
 * Such events are fired in the sub-app scope, hence, handlers in some sub-app are notified of only those lifecycle events
 * that happen in that particular sub-app.
 * <p>
 * Subscription to the SubAppLifecycleEvent allows the developer for managing the sub-app resources in a more fine-grained fashion,
 * e.g. warming-up some caches once sub-app is starting/activating and cleaning them up on termination/de-activation.
 * <p>
 * Please note that this event normally <strong>should not</strong> be fired in a custom app code - the app framework is
 * responsible for that.
 *
 * @see SubAppEventBus
 * @see Type
 */
public class SubAppLifecycleEvent implements Event<SubAppLifecycleEventHandler> {

    /**
     * List of supported sub-app lifecycle phases.
     */
    public enum Type {
        STARTED, STOPPED, FOCUSED, BLURRED
    }

    private final SubAppDescriptor subAppDescriptor;
    private final Type eventType;

    public SubAppLifecycleEvent(SubAppDescriptor app,  Type eventType) {
        this.subAppDescriptor = app;
        this.eventType = eventType;
    }

    public SubAppDescriptor getSubAppDescriptor() {
        return subAppDescriptor;
    }

    @Override
    public void dispatch(SubAppLifecycleEventHandler handler) {
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
        case BLURRED:
            handler.onSubAppBlurred(this);
            break;
        }
    }
}
