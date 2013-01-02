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
package info.magnolia.ui.framework.shell;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test case for {@link Fragmenter}.
 */
public class FragmenterTest {

    @Test
    public void testCanCreateWithNull() {
        Fragmenter fragmenter = new Fragmenter(null);
        assertNull(fragmenter.getSubFragment("foo"));
        assertEquals("", fragmenter.toString());
    }

    @Test
    public void testCanCreateWithEmptyString() {
        Fragmenter fragmenter = new Fragmenter("");
        assertNull(fragmenter.getSubFragment("foo"));
        assertEquals("", fragmenter.toString());
    }

    @Test
    public void testCanAddAfterBeingCreatedWithNull() {
        Fragmenter fragmenter = new Fragmenter(null);
        fragmenter.setSubFragment("foo", "bar");
        assertEquals("foo:bar", fragmenter.toString());
    }

    @Test
    public void testSplitsFragmentCorrectly() {
        Fragmenter fragmenter = new Fragmenter("foo:bar~zed:baz");
        assertEquals("bar", fragmenter.getSubFragment("foo"));
        assertEquals("baz", fragmenter.getSubFragment("zed"));
        assertEquals("foo:bar~zed:baz", fragmenter.toString());
    }

    @Test
    public void testSplitsWithFragmentAsEmptyStringIfColonMissing() {
        Fragmenter fragmenter = new Fragmenter("foo");
        assertEquals("", fragmenter.getSubFragment("foo"));
        assertEquals("foo:", fragmenter.toString());
    }

    @Test
    public void testSplitsWithFragmentAsEmptyStringIfNothingAfterColon() {
        Fragmenter fragmenter = new Fragmenter("foo:");
        assertEquals("", fragmenter.getSubFragment("foo"));
        assertEquals("foo:", fragmenter.toString());
    }

    @Test
    public void testCanAddAfterBeingCreatedWithProperFragment() {
        Fragmenter fragmenter = new Fragmenter("foo:bar");
        assertEquals("bar", fragmenter.getSubFragment("foo"));
        assertEquals("foo:bar", fragmenter.toString());
        fragmenter.setSubFragment("zed", "baz");
        assertEquals("baz", fragmenter.getSubFragment("zed"));
        assertEquals("foo:bar~zed:baz", fragmenter.toString());
    }

    @Test
    public void testCanAddAfterBeingCreatedWithEmptyString() {
        Fragmenter fragmenter = new Fragmenter("");
        assertEquals("", fragmenter.toString());
        fragmenter.setSubFragment("zed", "baz");
        assertEquals("baz", fragmenter.getSubFragment("zed"));
        assertEquals("zed:baz", fragmenter.toString());
    }

    @Test
    public void testCanRemoveSubFragments() {
        Fragmenter fragmenter = new Fragmenter("foo:bar~zed:baz");
        assertEquals("foo:bar~zed:baz", fragmenter.toString());
        assertEquals("bar", fragmenter.getSubFragment("foo"));
        fragmenter.setSubFragment("foo", null);
        assertEquals("zed:baz", fragmenter.toString());
        fragmenter.setSubFragment("zed", null);
        assertEquals("", fragmenter.toString());
    }
}
