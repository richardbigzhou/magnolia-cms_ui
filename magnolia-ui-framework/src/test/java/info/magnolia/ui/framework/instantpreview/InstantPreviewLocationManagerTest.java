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
import info.magnolia.ui.framework.instantpreview.InstantPreviewLocationManager.PreviewLocationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * InstantPreviewLocationManagerTest.
 */
public class InstantPreviewLocationManagerTest{
    private InstantPreviewLocationManagerImpl manager;

    @Before
    public void setUp() {
        manager = new InstantPreviewLocationManagerImpl();

        for(int i= 0; i < 3; i++) {
            manager.registerInstantPreviewHost();
        }

        PreviewLocationListener listener = mock(PreviewLocationListener.class);
        manager.subscribeTo(manager.getHosts().get(0), listener);
        manager.subscribeTo(manager.getHosts().get(0), listener);
        manager.subscribeTo(manager.getHosts().get(1), listener);
        manager.subscribeTo(manager.getHosts().get(2), listener);
    }

    @After
    public void tearDown() {
        manager = null;
    }

    @Test
    public void registerInstantPreviewHostTest() {
        //GIVEN see setUp

        //WHEN
        String id = manager.registerInstantPreviewHost();

        //THEN
        assertNotNull(id);
        assertEquals(4, manager.getHosts().size());

    }

    @Test
    public void ensureRegisterInstantPreviewHostGeneratesUniqueIdTest() {
        //GIVEN see setUp

        //WHEN
        String newId = manager.registerInstantPreviewHost();

        //THEN
        String id1 = manager.getHosts().get(0);
        String id2 = manager.getHosts().get(1);
        String id3 = manager.getHosts().get(2);
        assertNotSame(newId, id1);
        assertNotSame(newId, id2);
        assertNotSame(newId, id3);
    }


    @Test
    public void unregisterInstantPreviewHostTest() {
        //GIVEN see setUp

        //WHEN
        String hostId = manager.getHosts().get(0);
        manager.unregisterInstantPreviewHost(manager.getHosts().get(0));

        //THEN
        assertEquals(2, manager.getHosts().size());
        assertTrue(manager.getListeners().get(hostId).isEmpty());
    }

    @Test
    public void subscribeToTest() {
        //GIVEN see setUp

        //WHEN
        String hostId = manager.getHosts().get(0);
        manager.subscribeTo(hostId, mock(PreviewLocationListener.class));

        //THEN
        assertEquals(3, manager.getListeners().get(hostId).size());
    }

    @Test(expected=InstantPreviewHostNotFoundException.class)
    public void subscribeToNonExistingHostThrowsExceptionTest() {
        //GIVEN see setUp

        //WHEN
        String hostId = manager.getHosts().get(0);
        manager.unregisterInstantPreviewHost(hostId);
        assertEquals(2, manager.getHosts().size());

        //THEN boooom!
        manager.subscribeTo(hostId, mock(PreviewLocationListener.class));
    }

    @Test
    public void unsubscribeFromTest(){
        //GIVEN see setUp

        //WHEN
        String hostId = manager.getHosts().get(1);
        assertEquals(1, manager.getListeners().get(hostId).size());

        PreviewLocationListener listener = mock(PreviewLocationListener.class);
        manager.subscribeTo(hostId, listener);
        assertEquals(2, manager.getListeners().get(hostId).size());

        manager.unsubscribeFrom(hostId, listener);

        //THEN
        assertEquals(1, manager.getListeners().get(hostId).size());
    }

}
