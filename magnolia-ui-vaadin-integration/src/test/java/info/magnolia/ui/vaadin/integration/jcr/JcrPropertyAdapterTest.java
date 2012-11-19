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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;


/**
 * Main test class for {@link JcrPropertyAdapter}.
 */
public class JcrPropertyAdapterTest {

    private final String workspaceName = "workspace";

    private final String propertyName = "property";

    private final String propertyValue = "value";

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetItemProperty() throws Exception {
        // GIVEN
        Node node = session.getRootNode();
        node.setProperty(propertyName, propertyValue);

        JcrPropertyAdapter adapter = new JcrPropertyAdapter(node.getProperty(propertyName));

        // WHEN
        Property nameProperty = adapter.getItemProperty(JcrItemAdapter.JCR_NAME);
        Property valueProperty = adapter.getItemProperty(JcrPropertyAdapter.VALUE_COLUMN);
        Property typeProperty = adapter.getItemProperty(JcrPropertyAdapter.TYPE_COLUMN);

        // THEN
        assertEquals(propertyName, nameProperty.getValue());
        assertEquals(propertyValue, valueProperty.getValue());
        assertEquals(PropertyType.nameFromValue(PropertyType.STRING), typeProperty.getValue());
        assertNotSame(nameProperty, adapter.getItemProperty(JcrItemAdapter.JCR_NAME));
    }

}
