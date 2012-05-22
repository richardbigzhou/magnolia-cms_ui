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
package info.magnolia.m5admincentral.app;

import info.magnolia.ui.framework.event.Event;
import info.magnolia.ui.framework.event.EventHandler;


/**
 * App Event used to notify a Start or Stop event.
 *
 * @author erichechinger
 * @version $Id$
 *
 */
public class AppEvent implements Event<AppEvent.Handler>{


    /**
     * Listens to {@link AppEvent}s.
     */
    public interface Handler extends EventHandler {
        void onStopApp(AppLifecycle app);
        void onStartApp(AppLifecycle app);
    }

    private final AppLifecycle app;
    private final AppEventType eventType;

    public AppEvent(AppLifecycle app, AppEventType eventType) {
        System.out.println("AppEvent: create Event for app type: "+app.getClass().getName()+ " and type" +eventType);
        this.app = app;
        this.eventType = eventType;
    }

    public AppLifecycle getApp() {
        return app;
    }

    public AppEventType getEventType() {
        return eventType;
    }

    @Override
    public void dispatch(Handler handler) {
        if(eventType == null) {
            return;
        }

        if(eventType.equals(AppEventType.STOP_EVENT)) {
            handler.onStopApp(app);
        } else {
            handler.onStartApp(app);
        }
    }
}
