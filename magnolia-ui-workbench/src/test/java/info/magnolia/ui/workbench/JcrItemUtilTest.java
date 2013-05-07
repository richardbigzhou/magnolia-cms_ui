/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;

import javax.jcr.Property;

import org.junit.Test;

/**
 * Tests.
 */
public class JcrItemUtilTest {

    @Test
    public void testGetNodeUUIDFrom() throws Exception {
        // GIVEN
        final String nodeUuid = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";
        final String propertyId = nodeUuid + "@" + "name";

        // WHEN
        final String result = JcrItemUtil.getNodeUuidFrom(propertyId);

        // THEN
        assertThat(result, equalTo(nodeUuid));
    }

    @Test
    public void testGetPropertyName() throws Exception {
        // GIVEN
        final String propertyName = "specialProp";
        final String propertyId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2" + "@" + propertyName;

        // WHEN
        final String result = JcrItemUtil.getPropertyName(propertyId);

        // THEN
        assertThat(result, equalTo(propertyName));
    }

    @Test
    public void testIsPropertyIdWithPropertyId() throws Exception {
        // GIVEN
        final String propertyId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2" + "@" + "specialProp";

        // WHEN
        final boolean result = JcrItemUtil.isPropertyId(propertyId);

        // THEN
        assertTrue("Result should be considered to be a propertyId", result);
    }

    @Test
    public void testIsPropertyIdWithNodeId() throws Exception {
        // GIVEN
        final String nodeId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";

        // WHEN
        final boolean result = JcrItemUtil.isPropertyId(nodeId);

        // THEN
        assertFalse("Result should not be considered to be a propertyId", result);
    }

    @Test
    public void testGetItemId() throws Exception {
        // Given
        final String nodeUuid = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";
        final MockNode node = new MockNode();
        node.setIdentifier(nodeUuid);
        final String propertyName = "name";
        final Property property = new MockProperty(propertyName, "theName", node);

        // WHEN
        final String result = JcrItemUtil.getItemId(property);

        assertThat(result, equalTo(nodeUuid + JcrItemUtil.PROPERTY_NAME_AND_UUID_SEPARATOR + propertyName));
    }
}
