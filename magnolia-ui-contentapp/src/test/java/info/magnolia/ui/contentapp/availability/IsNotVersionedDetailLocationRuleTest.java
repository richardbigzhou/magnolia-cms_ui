/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.contentapp.availability;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.detail.DetailLocation;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link IsNotVersionedDetailLocationRule}.
 */
public class IsNotVersionedDetailLocationRuleTest {

    private IsNotVersionedDetailLocationRule isNotVersionedDetailLocationRule;
    private AppContext appContext;
    private SubAppContext subAppContext;

    private Location location;

    @Before
    public void setUp() {
        MockContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);

        appContext = mock(AppContext.class);
        subAppContext = mock(SubAppContext.class);
        doAnswer(new Answer<Location>() {

            @Override
            public Location answer(InvocationOnMock invocation) throws Throwable {
                return location;
            }
        }).when(subAppContext).getLocation();
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                location = (Location) invocation.getArguments()[0];
                return null;
            }
        }).when(subAppContext).setLocation(any(Location.class));

        when(appContext.getActiveSubAppContext()).thenReturn(subAppContext);
    }

    @Test
    public void testNonVersionedDetailLocation() {
        // GIVEN
        Object nullItem = null;
        DetailLocation detailLocation = new DetailLocation("testAppName", "testSubAppId", "testParameter");
        subAppContext.setLocation(detailLocation);

        isNotVersionedDetailLocationRule = new IsNotVersionedDetailLocationRule(appContext);

        // WHEN
        boolean isAvailable = isNotVersionedDetailLocationRule.isAvailable(Arrays.asList(nullItem));

        // THEN
        assertFalse(detailLocation.hasVersion());
        assertTrue("We expect the rule to be available for non-versioned locations", isAvailable);
    }

    @Test
    public void testVersionedDetailLocation() {
        // GIVEN
        Object nullItem = null;
        DetailLocation detailLocation = new DetailLocation("testAppName", "testSubAppId", "/test:view:1.0");
        subAppContext.setLocation(detailLocation);

        isNotVersionedDetailLocationRule = new IsNotVersionedDetailLocationRule(appContext);

        // WHEN
        boolean isAvailable = isNotVersionedDetailLocationRule.isAvailable(Arrays.asList(nullItem));

        // THEN
        assertTrue(detailLocation.hasVersion());
        assertFalse("We expect the rule to be unavailable for non-versioned locations", isAvailable);
    }

}
