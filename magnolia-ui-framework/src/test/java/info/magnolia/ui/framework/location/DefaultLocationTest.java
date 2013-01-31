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
package info.magnolia.ui.framework.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Test case for {@link DefaultLocation}.
 */
public class DefaultLocationTest {

    @Test
    public void testToString() {
        assertEquals("appType:appId:subAppId;parameter", new DefaultLocation("appType", "appId", "subAppId", "parameter").toString());
        assertEquals("appType:appId", new DefaultLocation("appType", "appId", "", "").toString());
        assertEquals("appType:appId", new DefaultLocation("appType", "appId", "", null).toString());
        assertEquals("appType", new DefaultLocation("appType", "", "", "").toString());
        assertEquals("appType", new DefaultLocation("appType", null, "", null).toString());
        assertEquals("", new DefaultLocation("", "", "", "").toString());
        assertEquals("", new DefaultLocation(null, null, "", null).toString());
    }

    @Test
    public void testExtractAppType() {

        assertEquals("appType", DefaultLocation.extractAppType("appType:appId:subAppId:more"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appId:subAppId"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appId"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:appId:"));
        assertEquals("appType", DefaultLocation.extractAppType("appType:"));
        assertEquals("appType", DefaultLocation.extractAppType("appType"));

        assertEquals("", DefaultLocation.extractAppType(""));
    }

    @Test
    public void testExtractAppId() {

        assertEquals("appId", DefaultLocation.extractAppId("appType:appId:subAppId:more"));
        assertEquals("appId", DefaultLocation.extractAppId("appType:appId:subAppId"));
        assertEquals("appId", DefaultLocation.extractAppId("appType:appId"));
        assertEquals("appId", DefaultLocation.extractAppId("appType:appId:"));

        assertEquals("", DefaultLocation.extractAppId("appType:"));
        assertEquals("", DefaultLocation.extractAppId("appType"));
        assertEquals("", DefaultLocation.extractAppId(""));
    }

    @Test
    public void testExtractSubAppId() {

        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appId:subAppId;parameter:parameter2"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appId:subAppId;"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appId:subAppId"));

        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appId:subAppId"));
        assertEquals("subAppId", DefaultLocation.extractSubAppId("appType:appId:subAppId"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appId"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appId:;parameter"));
        assertEquals("", DefaultLocation.extractSubAppId("appType:appId;parameter"));

        assertEquals("", DefaultLocation.extractSubAppId("appType:"));
        assertEquals("", DefaultLocation.extractSubAppId("appType"));
        assertEquals("", DefaultLocation.extractSubAppId(""));
    }

    @Test
    public void testExtractParameter() {

        assertEquals("parameter:parmeter2:some", DefaultLocation.extractParameter("appType:appId:subAppId;parameter:parmeter2:some"));

        assertEquals("parameter", DefaultLocation.extractParameter("appType:appId;parameter"));

        assertEquals("", DefaultLocation.extractParameter("appType:appId"));
        assertEquals("", DefaultLocation.extractParameter("appType:appId:"));
        assertEquals("", DefaultLocation.extractParameter("appType:"));
        assertEquals("", DefaultLocation.extractParameter("appType"));
        assertEquals("", DefaultLocation.extractParameter(""));
    }

    @Test
    public void testEqualsReturnsFalseOnNull() {
        // Given
        DefaultLocation first = new DefaultLocation("appType", "appId", "subAppId", "parameter");

        // WHEN
        boolean result = first.equals(null);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testEqualsReturnsFalseOnIncompatibleType() {
        // Given
        DefaultLocation first = new DefaultLocation("appType", "appId", "subAppId", "parameter");

        // WHEN
        boolean result = first.equals("this is a string");

        // THEN
        assertFalse(result);
    }
}
