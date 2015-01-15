/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.contentapp.detail.action;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.SupportsCreation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Item;

/**
 * Tests for the {@link CreateItemAction}.
 */
public class CreateItemActionTest {

    public static final String PARENT_PATH = "/swiss-phones";
    public static final String NEW_ITEM_PATH = PARENT_PATH + "/untitled";
    private Location location;
    private LocationController locationController;
    private ContentConnector contentConnector;
    private CreateItemActionDefinition definition;
    private Object parentItemId = new Object();
    private Item parentItem = new NullItem();
    private Object newItemId = new Object();

    @Before
    public void setUp() throws Exception {
        MockContext context = new MockWebContext();
        MgnlContext.setInstance(context);

        // action definition
        definition = new CreateItemActionDefinition();
        definition.setName("createPhone");
        definition.setAppName("phones");
        definition.setSubAppId("detail");

        // mock location controller to simply update the test's location field
        locationController = mock(LocationController.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                location = (Location) invocation.getArguments()[0];
                return null;
            }
        }).when(locationController).goTo(any(Location.class));

        // mock content connector
        contentConnector = mock(ContentConnector.class, withSettings().extraInterfaces(SupportsCreation.class));
        doReturn(newItemId).when((SupportsCreation)contentConnector).getNewItemId(any(), any());
        doReturn(parentItem).when(contentConnector).getItemId(parentItem);
        doReturn(true).when(contentConnector).canHandleItem(any());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void executeTriggersLocationChange() throws Exception {
        // GIVE
        doReturn(PARENT_PATH).when(contentConnector).getItemUrlFragment(parentItemId);
        doReturn(NEW_ITEM_PATH).when(contentConnector).getItemUrlFragment(newItemId);

        CreateItemAction action = new CreateItemAction(definition, locationController, parentItem, contentConnector);

        // WHEN
        action.execute();

        // THEN
        assertTrue(location instanceof DetailLocation);
        assertEquals("phones", location.getAppName());
        assertEquals("detail", location.getSubAppId());
        assertEquals(NEW_ITEM_PATH, ((DetailLocation) location).getNodePath());
    }

}
