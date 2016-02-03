/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.repository.RepositoryConstants;
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
 * SaveFormActionTest.
 */
public class SaveFormActionTest extends RepositoryTestCase {
    private SaveFormActionDefinition formActionDefinition;
    private CallbackFormActionTest.TestEditorCallback callback;
    private CallbackFormActionTest.TestEditorValidator validator;
    private Session session;
    private Node node;
    private JcrNodeAdapter item;
    private SaveFormAction formAction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        this.formActionDefinition = new SaveFormActionDefinition();
        this.callback = new CallbackFormActionTest.TestEditorCallback();
        this.validator = new CallbackFormActionTest.TestEditorValidator();
        // Init Node
        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    }

    @Test
    public void executeDefaultExecuteTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.getSession().save();
        item = new JcrNodeAdapter(node);
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(name)", this.callback.getCallbackActionCalled());
    }

    @Test
    public void executeSaveChangeNodeNameBasedOnPropertyNameTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("name", "newNodeName");
        item = new JcrNodeAdapter(node);
        item.getItemProperty("name").setValue("newNodeNameChanged");
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, item, callback, validator);

        // WHEN
        formAction.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode("underlying"));
        assertTrue(session.getRootNode().hasNode("newNodeNameChanged"));
    }

    @Test
    public void executeSaveChangeNodeNameBasedOnPropertyJcrNameTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("name", "newNodeName");
        node.setProperty("jcrName", "newNodeJcrName");
        item = new JcrNodeAdapter(node);
        item.getItemProperty("name").setValue("newNodeNameChanged");
        item.getItemProperty("jcrName").setValue("newNodeJcrNameChanged");
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, item, callback, validator);

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
        node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        item = new JcrNodeAdapter(node);
        item.getItemProperty("property").setValue("changed");
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition,item, callback, validator);

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
        node = session.getRootNode().addNode("underlying");

        item = new JcrNodeAdapter(node);
        item.addItemProperty("property", DefaultPropertyUtil.newDefaultProperty(null, "changed"));
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, item, callback, validator);

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
        node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        item = new JcrNodeAdapter(node);
        item.removeItemProperty("property");
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, item, callback, validator);
        assertEquals(true, node.hasProperty("property"));
        // WHEN
        formAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(false, node.hasProperty("property"));

    }

    /**
     * Init the Definition.
     */
    private void initDefinition(String name, String label) {
        this.formActionDefinition.setLabel(label);
        this.formActionDefinition.setName(name);
    }
}
