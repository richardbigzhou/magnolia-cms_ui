/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Test covering the view logic for inserting and aligning child components as expected.
 */
public class StatusBarViewImplTest {

    StatusBarViewImpl view;

    @Before
    public void setUp() {
        view = new StatusBarViewImpl();
    }

    @Test
    public void testAddComponent() {
        // GIVEN
        Label firstLeft = new Label("M");
        Label secondLeft = new Label("A");
        Label firstCentered = new Label("G");
        Label secondCentered = new Label("N");
        Label thirdLeftThenRight = new Label("O");
        Label secondRight = new Label("L");
        Label firstRight = new Label("IA");

        final StringBuilder result = new StringBuilder();

        // WHEN
        view.addComponent(firstLeft, Alignment.MIDDLE_LEFT); // M
        view.addComponent(firstRight, Alignment.MIDDLE_RIGHT); // M IA
        view.addComponent(firstCentered, Alignment.MIDDLE_CENTER); // M G IA
        view.addComponent(secondLeft, Alignment.MIDDLE_LEFT); // M A G IA
        view.addComponent(secondCentered, Alignment.MIDDLE_CENTER); // M A G N IA
        view.addComponent(secondRight, Alignment.MIDDLE_RIGHT); // M A G N L IA
        view.addComponent(thirdLeftThenRight, Alignment.MIDDLE_LEFT); // M A O G N L IA
        view.addComponent(thirdLeftThenRight, Alignment.MIDDLE_RIGHT); // M A G N O L IA

        Iterator<Component> it = view.getComponentIterator();
        while (it.hasNext()) {
            result.append(((Label) it.next()).getValue());
        }

        // THEN
        assertEquals(7, view.getComponentCount());
        assertNotNull(result);
        assertEquals("MAGNOLIA", result.toString());
    }

}
