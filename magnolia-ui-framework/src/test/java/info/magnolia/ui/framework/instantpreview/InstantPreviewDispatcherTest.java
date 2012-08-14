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
package info.magnolia.ui.framework.instantpreview;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent.Handler;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.shell.Shell;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * InstantPreviewDispatcherTest.
 */
public class InstantPreviewDispatcherTest {

    private InstantPreviewLocationManagerImpl manager;
    private EventBus eventBus;
    private Shell shell;
    private LocationController controller;
    private InstantPreviewDispatcher dispatcher;
    private ArrayList<LocationChangedEvent> events;

    @Before
    public void setUp() {
        manager = new InstantPreviewLocationManagerImpl();
        eventBus = new SimpleEventBus();
        shell = mock(Shell.class);
        controller = new LocationController(eventBus, shell);
        dispatcher = new InstantPreviewDispatcher(manager, controller, shell, eventBus);
        events = new ArrayList<LocationChangedEvent>();
        eventBus.addHandler(LocationChangedEvent.class, new CollectingLocationChangedEventHandler(events));
    }

    @After
    public void tearDown() {
        manager = null;
        shell = null;
        eventBus = null;
        controller = null;
        dispatcher = null;
        events = null;
     }

    @Test
    public void onPreviewLocationReceivedTest() throws InterruptedException {
        // GIVEN see SetUp

        // WHEN
        String path1 = "/foo/bar";
        dispatcher.onPreviewLocationReceived(path1);
        Thread.sleep(100);

        String path2 = "/baz/qux";
        dispatcher.onPreviewLocationReceived(path2);
        Thread.sleep(100);

        // THEN
        assertEquals(2, events.size());
        assertEquals(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", path1), events.get(0).getNewLocation());
        assertEquals(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", path2), events.get(1).getNewLocation());
    }

    @Test
    public void isSharingTest() {
        // GIVEN see also setUp
        dispatcher.share();

        // WHEN
        boolean isSharing = dispatcher.isSharing();

        //THEN
        assertTrue(isSharing);
    }

    @Test
    public void shareTest() {
        // GIVEN see setUp

        // WHEN
        String id = dispatcher.share();

        //THEN
        assertNotNull(id);
    }

    @Test
    public void unshareTest() {
        // GIVEN see also setUp
        String hostId = dispatcher.share();
        assertTrue(dispatcher.isSharing());

        // WHEN
        dispatcher.unshare(hostId);

        //THEN
        boolean isSharing = dispatcher.isSharing();
        assertFalse(isSharing);
    }

    @Test
    public void subscribeToTest() {
        // GIVEN see also setUp
        String hostId = dispatcher.share();
        assertTrue(dispatcher.isSharing());
        assertEquals(0, manager.getListeners().get(hostId).size());

        //WHEN
        dispatcher.subscribeTo(hostId);

        //THEN
        assertEquals(1, manager.getListeners().get(hostId).size());

    }

    @Test
    public void unsubscribeFromTest() {
        // GIVEN see also setUp
        String hostId = dispatcher.share();
        assertTrue(dispatcher.isSharing());
        dispatcher.subscribeTo(hostId);
        assertEquals(1, manager.getListeners().get(hostId).size());

        //WHEN
        dispatcher.unshare(hostId);

        //THEN
        assertEquals(0, manager.getListeners().get(hostId).size());

    }

    private static final class CollectingLocationChangedEventHandler implements Handler {
        private final List<LocationChangedEvent> events;

        public CollectingLocationChangedEventHandler(List<LocationChangedEvent> events) {
            this.events = events;
        }

        @Override
        public void onLocationChanged(LocationChangedEvent event) {
            events.add(event);
        }
    }

}
