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
package info.magnolia.ui.admincentral.tree.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.framework.event.EventBus;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests covering execution of the {@link AddPropertyAction}.
 */
public class AddPropertyActionTest {

    private final static String WORKSPACE = "workspace";

    private final static String JOHN_NODE_NAME = "johnNode";

    private final static String UNTITLED_PROPERTY_NAME = "untitled";

    private final static String UNTITLED_PROPERTY_VALUE = "preset";

    private static final AddPropertyActionDefinition DEFINITION = new AddPropertyActionDefinition();

    private EventBus eventBus;

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        MgnlContext.setInstance(ctx);

        eventBus = mock(EventBus.class);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExecute_onRoot_Once() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        long propertiesCount = root.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(DEFINITION, root, eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(propertiesCount + 1, root.getProperties().getSize());
    }

    @Test
    public void testExecute_onRoot_Twice() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        long propertiesCount = root.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(DEFINITION, root, eventBus);

        // WHEN
        action.execute();
        action.execute();

        // THEN
        assertEquals(propertiesCount + 2, root.getProperties().getSize());
    }

    @Test
    public void testExecute_onJohnNode_Once() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node john = root.addNode(JOHN_NODE_NAME);
        long propertiesCount = john.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(DEFINITION, john, eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(propertiesCount + 1, john.getProperties().getSize());
    }

    @Test
    public void testExecute_onJohnNode_Twice() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node john = root.addNode(JOHN_NODE_NAME);
        long propertiesCount = john.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(DEFINITION, john, eventBus);

        // WHEN
        action.execute();
        action.execute();

        // THEN
        assertEquals(propertiesCount + 2, john.getProperties().getSize());
    }

    @Test
    public void testExecute_onJohnNode_WithExistingUntitled() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node john = root.addNode(JOHN_NODE_NAME);
        john.setProperty(UNTITLED_PROPERTY_NAME, UNTITLED_PROPERTY_VALUE);
        long propertiesCount = john.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(DEFINITION, john, eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(propertiesCount + 1, john.getProperties().getSize());
        assertEquals(john.getProperty(UNTITLED_PROPERTY_NAME).getString(), UNTITLED_PROPERTY_VALUE);
    }

}
