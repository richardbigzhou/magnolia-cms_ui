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

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for MgnlElement.
 * 
 * @author cheng.hu
 */
public class MgnlElementTest {
    @Test
    public void testIsInherited() {
        // Base case
        HashMap<String, String> attributes = new HashMap<String, String>();
        MgnlElement mgnlEl = new MgnlArea(null, null);
        mgnlEl.setAttributes(attributes);
        assertFalse(mgnlEl.isInherited());
        mgnlEl.getAttributes().put("inherited", "false");
        assertFalse(mgnlEl.isInherited());
        mgnlEl.getAttributes().put("inherited", "true");
        assertTrue(mgnlEl.isInherited());

        // Induction
        HashMap<String, String> attributesChild = new HashMap<String, String>();

        MgnlElement mgnlElChild = new MgnlComponent(null, null);
        mgnlElChild.setParent(mgnlEl);
        mgnlElChild.setAttributes(attributesChild);
        assertTrue(mgnlElChild.isInherited());
        mgnlElChild.getAttributes().put("inherited", "false");
        assertTrue(mgnlElChild.isInherited());
        mgnlElChild.getAttributes().put("inherited", "true");
        assertTrue(mgnlElChild.isInherited());

        mgnlEl.getAttributes().put("inherited", "false");
        mgnlElChild.getAttributes().put("inherited", "false");
        assertFalse(mgnlElChild.isInherited());
        mgnlElChild.getAttributes().put("inherited", "true");
        assertTrue(mgnlElChild.isInherited());
    }
}
