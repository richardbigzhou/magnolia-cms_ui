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
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic Implementation of the App lifecycle.
 * Defined as Abstract: public void focus() not implemented.
 * Create Events for start() and stop().
 * @author erichechinger
 * @version $Id$
 */
public abstract class AbstractAppLifecycle implements AppLifecycle {
    //Define Logger
    private static Logger log = LoggerFactory.getLogger(AbstractAppLifecycle.class);
    //Define Injected Classes
    protected PlaceController placeController;
    protected EventBus eventBus;

    @Inject
    public AbstractAppLifecycle(PlaceController placeController, EventBus eventBus) {
        this.placeController = placeController;
        this.eventBus = eventBus;
    }

    @Override
    public void focus() {
        log.debug("App focused");
        sendEvent(new AppLifecycleEvent(this, AppEventType.FOCUS_EVENT));
    }
    
    @Override
    public void start() {
        log.debug("App Start, will send Start Event");
        //Create Start Event
        //Send Event to the EventBus
        sendEvent(new AppLifecycleEvent(this, AppEventType.START_EVENT));

    }

    @Override
    public void stop() {
        log.debug("App Stop, will send Stop Event");
        //Create Stop Event
        //Send Event to the EventBus
        sendEvent(new AppLifecycleEvent(this, AppEventType.STOP_EVENT));
    }

    /**
     * Send Event to the EventBuss.
     */
    private void sendEvent(Event<? extends AppLifecycleEventHandler>  event) {
        log.debug("AppLifecycle: send Event "+event.getClass().getName());
        eventBus.fireEvent(event);
    }

}
