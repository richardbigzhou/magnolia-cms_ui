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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.event.Event;
import info.magnolia.ui.framework.event.EventBus;

import javax.inject.Singleton;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Default AppController implementation.
 *
 * @version $Id$
 */
@Singleton
public class AppControllerImpl implements AppController {

    private static Logger log = LoggerFactory.getLogger(AppControllerImpl.class);

    private AppRegistry appRegistry;

    private ComponentProvider componentProvider;

    protected EventBus eventBus;

    private BidiMap runningApps = new DualHashBidiMap();


    @Inject
    public AppControllerImpl(AppRegistry appRegistry, ComponentProvider componentProvider, EventBus eventBus) {
        this.appRegistry = appRegistry;
        this.componentProvider = componentProvider;
        this.eventBus = eventBus;
    }

    @Override
    public void startIfNotAlreadyRunning(String name) {
        doStart(name);
    }

    @Override
    public void startIfNotAlreadyRunningThenFocus(String name) {
        final AppLifecycle lifecycle = doStart(name);
        lifecycle.focus();
        sendEvent(new AppLifecycleEvent(lifecycle, AppEventType.FOCUS_EVENT));
    }

    private AppLifecycle doStart(String name) {
        final AppDescriptor descriptor = appRegistry.getAppDescriptor(name);
        AppLifecycle lifecycle = (AppLifecycle)runningApps.get(descriptor);
        if (lifecycle == null) {
            lifecycle = componentProvider.newInstance(descriptor.getAppClass());
            lifecycle.start();
            runningApps.put(descriptor, lifecycle);
            sendEvent(new AppLifecycleEvent(lifecycle, AppEventType.START_EVENT));
        }
        return lifecycle;
    }

    @Override
    public AppDescriptor getAppDescriptor(AppLifecycle app) {
        return (AppDescriptor)runningApps.getKey(app);
    }

    @Override
    public AppLifecycle getLifecycleByName(String name) {
        for (final Object obj : runningApps.keySet()) {
            final AppDescriptor descriptor = (AppDescriptor)obj;
            if (descriptor.getName().equals(name)) {
                return (AppLifecycle)runningApps.get(obj);
            }
        }
        return null;
    }

    @Override
    public void stopApplication(String name) {
        doStop(name);
    }

    private void doStop(String name) {
        final AppDescriptor descriptor = appRegistry.getAppDescriptor(name);
        AppLifecycle lifecycle = (AppLifecycle)runningApps.get(descriptor);
        if (lifecycle != null) {
            lifecycle.stop();
            runningApps.remove(descriptor);
            sendEvent(new AppLifecycleEvent(lifecycle, AppEventType.STOP_EVENT));
        }
    }

    /**
     * Send Event to the EventBuss.
     */
    private void sendEvent(Event<? extends AppLifecycleEventHandler>  event) {
        log.debug("AppControlelr: send Event "+event.getClass().getName());
        eventBus.fireEvent(event);
    }
}
