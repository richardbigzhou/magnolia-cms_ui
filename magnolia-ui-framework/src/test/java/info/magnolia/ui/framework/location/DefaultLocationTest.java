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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link DefaultLocation}.
 */
public class DefaultLocationTest {

    @Test
    public void testToString() {
        assertEquals("type:prefix:token", new DefaultLocation("type", "prefix", "token").toString());
        assertEquals("type:prefix", new DefaultLocation("type", "prefix", "").toString());
        assertEquals("type:prefix", new DefaultLocation("type", "prefix", null).toString());
        assertEquals("type", new DefaultLocation("type", "", "").toString());
        assertEquals("type", new DefaultLocation("type", null, null).toString());
        assertEquals("", new DefaultLocation("", "", "").toString());
        assertEquals("", new DefaultLocation(null, null, null).toString());
    }

    @Test
    public void testExtractType() {

        assertEquals("type", DefaultLocation.extractType("type:prefix:token:more"));
        assertEquals("type", DefaultLocation.extractType("type:prefix:token"));
        assertEquals("type", DefaultLocation.extractType("type:prefix"));
        assertEquals("type", DefaultLocation.extractType("type:prefix:"));
        assertEquals("type", DefaultLocation.extractType("type:"));
        assertEquals("type", DefaultLocation.extractType("type"));

        assertEquals("", DefaultLocation.extractType(""));
    }

    @Test
    public void testExtractPrefix() {

        assertEquals("prefix", DefaultLocation.extractPrefix("type:prefix:token:more"));
        assertEquals("prefix", DefaultLocation.extractPrefix("type:prefix:token"));
        assertEquals("prefix", DefaultLocation.extractPrefix("type:prefix"));
        assertEquals("prefix", DefaultLocation.extractPrefix("type:prefix:"));

        assertEquals("", DefaultLocation.extractPrefix("type:"));
        assertEquals("", DefaultLocation.extractPrefix("type"));
        assertEquals("", DefaultLocation.extractPrefix(""));
    }

    @Test
    public void testExtractToken() {

        assertEquals("token:more", DefaultLocation.extractToken("type:prefix:token:more"));

        assertEquals("token", DefaultLocation.extractToken("type:prefix:token"));

        assertEquals("", DefaultLocation.extractToken("type:prefix"));
        assertEquals("", DefaultLocation.extractToken("type:prefix:"));
        assertEquals("", DefaultLocation.extractToken("type:"));
        assertEquals("", DefaultLocation.extractToken("type"));
        assertEquals("", DefaultLocation.extractToken(""));
    }
}
