/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.api.location;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockWebContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link info.magnolia.ui.api.location.DefaultLocation}.
 */
public class DefaultLocationTest {

    @Before
    public void setUp() throws Exception {
        MockWebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testToString() {
        assertEquals("appType:appName:subAppId;parameter", new DefaultLocation("appType", "appName", "subAppId", "parameter").toString());
        assertEquals("appType:appName", new DefaultLocation("appType", "appName", "", "").toString());
        assertEquals("appType:appName", new DefaultLocation("appType", "appName", "", null).toString());
        assertEquals("appType", new DefaultLocation("appType", "", "", "").toString());
        assertEquals("appType", new DefaultLocation("appType", null, "", null).toString());
        assertEquals("", new DefaultLocation("", "", "", "").toString());
        assertEquals("", new DefaultLocation(null, null, "", null).toString());
        assertEquals("", new DefaultLocation(null, null, null, null).toString());

        assertEquals("appType:appName:subAppId;parameter one", new DefaultLocation("appType", "appName", "subAppId", "parameter%20one").toString());
        assertEquals("appType:appName:subAppId;parameter one", new DefaultLocation("appType", "appName", "subAppId", "parameter one").toString());
    }

    @Test
    public void testExtractAppType() {

        assertEquals("appType", DefaultLocation.extractAppType("appType:appName:subAppId:more"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appName:subAppId"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appName"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appName:"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:"));
        assertEquals("appType", DefaultLocation.extractAppType("appType"));

        assertEquals("", DefaultLocation.extractAppType(""));
    }

    @Test
    public void testExtractAppName() {

        assertEquals("appName", DefaultLocation.extractAppName("appType:appName:subAppId:more"));
        assertEquals("appName", DefaultLocation.extractAppName("appType:appName:subAppId"));
        assertEquals("appName", DefaultLocation.extractAppName("appType:appName"));
        assertEquals("appName", DefaultLocation.extractAppName("appType:appName:"));

        assertEquals("", DefaultLocation.extractAppName("appType:"));
        assertEquals("", DefaultLocation.extractAppName("appType"));
        assertEquals("", DefaultLocation.extractAppName(""));
    }

    @Test
    public void testExtractSubAppId() {

        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appName:subAppId;parameter:parameter2"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appName:subAppId;"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appName:subAppId"));

        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appName:subAppId"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appName:subAppId"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appName"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appName:;parameter"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appName;parameter"));

        assertEquals("", DefaultLocation.extractSubAppId("appType:"));
        assertEquals("", DefaultLocation.extractSubAppId("appType"));
        assertEquals("", DefaultLocation.extractSubAppId(""));
    }

    @Test
    public void testExtractParameter() {

        assertEquals("parameter:parmeter2:some", DefaultLocation.extractParameter("appType:appName:subAppId;parameter:parmeter2:some"));

        assertEquals("parameter", DefaultLocation.extractParameter("appType:appName;parameter"));

        assertEquals("", DefaultLocation.extractParameter("appType:appName"));
        assertEquals("", DefaultLocation.extractParameter("appType:appName:"));
        assertEquals("", DefaultLocation.extractParameter("appType:"));
        assertEquals("", DefaultLocation.extractParameter("appType"));
        assertEquals("", DefaultLocation.extractParameter(""));
    }

    @Test
    public void testEqualsReturnsFalseOnNull() {
        // GIVEN
        DefaultLocation first = new DefaultLocation("appType", "appName", "subAppId", "parameter");

        // WHEN
        boolean result = first.equals(null);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testEqualsReturnsFalseOnIncompatibleType() {
        // GIVEN
        DefaultLocation first = new DefaultLocation("appType", "appName", "subAppId", "parameter");

        // WHEN
        boolean result = first.equals("this is a string");

        // THEN
        assertFalse(result);
    }

    @Test
    public void testDefaultLocationFromFragment() throws Exception {
        // GIVEN
        DefaultLocation location = new DefaultLocation("appType:appName:subAppId;parameter:parameter2");

        // WHEN
        String appType = location.getAppType();
        String appName = location.getAppName();
        String subAppId = location.getSubAppId();
        String parameter = location.getParameter();

        // THEN
        assertEquals("appType", appType);
        assertEquals("appName", appName);
        assertEquals("subAppId", subAppId);
        assertEquals("parameter:parameter2", parameter);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultLocationFromNullFragmentThrowsException() throws Exception {
        new DefaultLocation(null);
    }

    @Test
    public void testDefaultLocationDecodeFragment() {
        // GIVEN
        String fragment = "appType:appName:subAppId:/more%20parameters";

        // WHEN
        String decodedFragment = DefaultLocation.decodeFragment(fragment);

        // THEN
        assertEquals("appType:appName:subAppId:/more parameters", decodedFragment);
    }

    @Test
    public void testDefaultLocationDecodeFragmentFromFragment() {
        // GIVEN
        DefaultLocation location = new DefaultLocation("invalid%20appType:invalid%20appName:invalid%20subAppId;parameter%20one:parameter%20two");

        // WHEN
        String decodedAppType = location.getAppType();
        String decodedAppName = location.getAppName();
        String decodedSubAppId = location.getSubAppId();
        String decodedParameter = location.getParameter();

        // THEN
        assertEquals("invalid appType", decodedAppType);
        assertEquals("invalid appName", decodedAppName);
        assertEquals("invalid subAppId", decodedSubAppId);
        assertEquals("parameter one:parameter two", decodedParameter);
    }
}
