/**
 * This file Copyright (c) 2003-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom;

import static org.junit.Assert.*;

import info.magnolia.cms.security.operations.OperationPermissionDefinition;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MgnlAreaTest.
 */
public class MgnlAreaTest {

    private MgnlArea area;

    @Before
    public void setUp() {
        area = new MgnlArea(null, null);
        area.setAttributes(new HashMap<String, String>());
    }

    @Test
    public void availabilityOfAddComponentActionForAreaOfSingleTypeWithComponentAdded() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("type", "single");
        area.getAttributes().put(OperationPermissionDefinition.ADDIBLE, "true");
        area.getChildren().add(new MgnlComponent(null, null));

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertFalse(addible);
    }

    @Test
    public void availabilityOfAddComponentActionForAreaOfSingleTypeWithNoComponent() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("availableComponents", "[someComponent]");
        area.getAttributes().put("type", "single");
        area.getAttributes().put(OperationPermissionDefinition.ADDIBLE, "true");
        area.getChildren().clear();

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertTrue(addible);
    }

    @Test
    public void availabilityOfAddComponentActionForCreatedOptionalArea() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("availableComponents", "[someComponent]");
        area.getAttributes().put("type", "list");
        area.getAttributes().put("optional", "true");
        area.getAttributes().put("created", "true");

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertTrue(addible);
    }

    @Test
    public void availabilityOfAddComponentActionForAreaWithNoAvailableComponent() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("availableComponents", "");
        area.getAttributes().put("type", "list");

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertFalse(addible);
    }

    @Test
    public void availabilityOfAddComponentActionForAreaWithMaxComponentReached() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("availableComponents", "[someComponent]");
        area.getAttributes().put("type", "list");
        area.getAttributes().put("showAddButton", "false");
        area.getAttributes().put("showNewComponentArea", "true");

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertFalse(addible);
    }

    @Test
    public void availabilityOfAddComponentActionForNotCreatedOptionalArea() {
        // GIVEN
        area.getAttributes().clear();
        area.getAttributes().put("type", "list");
        area.getAttributes().put("optional", "true");
        area.getAttributes().put("created", "false");

        // WHEN
        boolean addible = area.getTypedElement().getAddible();

        // THEN
        assertFalse(addible);
    }
}
