/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.form.action;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SaveFormAction}.
 */
public class SaveFormActionTest extends RepositoryTestCase {

    private static final SaveFormActionDefinition ACTION_DEFINITION = new SaveFormActionDefinition() {
        {
            setName("name");
            setLabel("label");
        }
    };

    private CallbackFormActionTest.TestEditorCallback callback;
    private CallbackFormActionTest.TestEditorValidator validator;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        callback = new CallbackFormActionTest.TestEditorCallback();
        validator = new CallbackFormActionTest.TestEditorValidator();
        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    }

    @Test
    public void executeDefaultExecuteTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.getSession().save();
        JcrNodeAdapter item = new JcrNodeAdapter(node);
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(name)", this.callback.getCallbackActionCalled());
    }

    @Test
    public void executeSaveChangeNodeNameBasedOnPropertyNameTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("name", "newNodeName");
        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.getItemProperty("name").setValue("newNodeNameChanged");
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode("underlying"));
        assertTrue(session.getRootNode().hasNode("newNodeNameChanged"));
    }

    @Test
    public void executeKeepsNameProperty() throws Exception {
        // GIVEN
        Node node = session.getRootNode().addNode("Culture");
        node.setProperty("name", "Culture");
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, adapter, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertTrue(session.getRootNode().hasNode("Culture"));
        assertTrue(session.getRootNode().getNode("Culture").hasProperty("name"));
        assertEquals("Culture", session.getRootNode().getNode("Culture").getProperty("name").getString());
    }

    @Test
    public void executeKeepsNamePropertyWithSpaces() throws Exception {
        // GIVEN
        Node node = session.getRootNode().addNode("No Culture");
        node.setProperty("name", "No Culture");
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, adapter, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertTrue(session.getRootNode().hasNode("No-Culture"));
        assertTrue(session.getRootNode().getNode("No-Culture").hasProperty("name"));
        assertEquals("No Culture", session.getRootNode().getNode("No-Culture").getProperty("name").getString());
    }

    @Test
    public void executeRenamesNodeAndKeepsNameProperty() throws Exception {
        // GIVEN
        Node node = session.getRootNode().addNode("Culture");
        node.setProperty("name", "Culture");
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        adapter.getItemProperty("name").setValue("Culture and Arts");
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, adapter, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode("Culture"));
        assertTrue(session.getRootNode().hasNode("Culture-and-Arts"));
        assertTrue(session.getRootNode().getNode("Culture-and-Arts").hasProperty("name"));
        assertEquals("Culture and Arts", session.getRootNode().getNode("Culture-and-Arts").getProperty("name").getString());
    }

    @Test
    public void executeSaveChangeNodeNameBasedOnPropertyJcrNameTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("name", "newNodeName");
        node.setProperty("jcrName", "newNodeJcrName");
        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.getItemProperty("name").setValue("newNodeNameChanged");
        item.getItemProperty("jcrName").setValue("newNodeJcrNameChanged");
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode("underlying"));
        assertFalse(session.getRootNode().hasNode("newNodeNameChanged"));
        assertTrue(session.getRootNode().hasNode("newNodeJcrNameChanged"));
    }

    @Test
    public void executeSaveUpdatePropertyTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.getItemProperty("property").setValue("changed");
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void executeSaveCreatePropertyTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");

        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.addItemProperty("property", DefaultPropertyUtil.newDefaultProperty(String.class, "changed"));
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void executeSaveRemovePropertyTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.removeItemProperty("property");
        SaveFormAction formAction = new SaveFormAction(ACTION_DEFINITION, item, callback, validator);
        assertEquals(true, node.hasProperty("property"));
        // WHEN
        formAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(false, node.hasProperty("property"));

    }

}
