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
package info.magnolia.ui.admincentral.dialog.action;

import static org.junit.Assert.*;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link SaveDialogAction} and {@link SaveDialogActionDefinition}.
 */
public class SaveDialogActionTest extends MgnlTestCase {

    private SaveDialogAction dialogAction;
    private SaveDialogActionDefinition dialogActionDefinition;
    private CallbackDialogActionTest.FormDialogPresenterTest presenter;
    private Node node;
    private Item item;
    private MockSession session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        dialogActionDefinition = new SaveDialogActionDefinition();
        dialogActionDefinition.setLabel("label");
        dialogActionDefinition.setName("name");

        presenter = new CallbackDialogActionTest.FormDialogPresenterTest();
        // Init Node
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.setUser(new MgnlUser("userName", "realm", new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>()));
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        dialogActionDefinition = null;
        presenter = null;
        session = null;
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExecuteDefaultExecute() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.getSession().save();
        item = new JcrNodeAdapter(node);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(name)", presenter.getCallbackActionCalled());
    }

    @Test
    public void testExecuteSaveUpdateProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        item = new JcrNodeAdapter(node);
        item.getItemProperty("property").setValue("changed");
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void testExecuteSaveCreateProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");

        item = new JcrNodeAdapter(node);
        item.addItemProperty("property", DefaultPropertyUtil.newDefaultProperty(null, "changed"));
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void testExecuteSaveRemoveProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        item = new JcrNodeAdapter(node);
        item.removeItemProperty("property");
        assertEquals(true, node.hasProperty("property"));
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(false, node.hasProperty("property"));
    }

    @Test
    public void testUpdateLastModified() throws RepositoryException {
        // GIVEN
        node = session.getRootNode().addNode("content", NodeTypes.Content.NAME);

        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);
        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertTrue(NodeTypes.LastModified.NAME + " should have been updated.", past.before(lastModified));
    }

    @Test
    public void testUpdateLastModifiedDoesNothingForFolder() throws RepositoryException {
        // GIVEN
        node = session.getRootNode().addNode("folder", NodeTypes.Folder.NAME);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);

        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertEquals(NodeTypes.LastModified.NAME + " should not have been updated.", past, lastModified);
    }

    @Test
    public void testUpdateLastModifiedTouchesParent() throws RepositoryException {
        // GIVEN
        Node parentOfParent = session.getRootNode().addNode("parentOfParent", NodeTypes.Content.NAME);
        Node parent = parentOfParent.addNode("parent", NodeTypes.ContentNode.NAME);
        node = parent.addNode("node", NodeTypes.ContentNode.NAME);
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);

        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        parent.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertTrue(NodeTypes.LastModified.NAME + " of node should have been updated.", past.before(lastModified));
        lastModified = NodeTypes.LastModified.getLastModified(parent);
        assertTrue(NodeTypes.LastModified.NAME + " of parent should have been updated.", past.before(lastModified));
    }

    @Test
    public void testUpdateLastModifiedDoesNotTouchParentWhenNotWebsiteWorkspace() throws RepositoryException {
        // GIVEN
        // session has to be non-Website for this test
        session = new MockSession(RepositoryConstants.CONFIG);
        Node parentOfParent = session.getRootNode().addNode("parentOfParent", NodeTypes.Content.NAME);
        Node parent = parentOfParent.addNode("parent", NodeTypes.ContentNode.NAME);
        node = parent.addNode("node", NodeTypes.ContentNode.NAME);
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);

        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        parent.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertTrue(NodeTypes.LastModified.NAME + " of node should have been updated.", past.before(lastModified));
        lastModified = NodeTypes.LastModified.getLastModified(parent);
        assertEquals(NodeTypes.LastModified.NAME + " of parent should not have been updated.", past, lastModified);
    }

    @Test
    public void testUpdateLastModifiedDoesNotTouchParentWhenItHasLevel1() throws RepositoryException {
        // GIVEN
        Node parentOfParent = session.getRootNode().addNode("parentOfParent", NodeTypes.ContentNode.NAME);
        Node parent = parentOfParent.addNode("parent", NodeTypes.ContentNode.NAME);
        node = parent.addNode("node", NodeTypes.ContentNode.NAME);
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);

        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        parent.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertTrue(NodeTypes.LastModified.NAME + " of node should have been updated.", past.before(lastModified));
        lastModified = NodeTypes.LastModified.getLastModified(parent);
        assertTrue(NodeTypes.LastModified.NAME + " of parent should have been updated.", past.before(lastModified));
        lastModified = NodeTypes.LastModified.getLastModified(parentOfParent);
        assertTrue(NodeTypes.LastModified.NAME + " of parentOfParent should have been updated.", past.before(lastModified));
    }

    @Test
    public void testUpdateLastModifiedDoesNotTouchParentWhenItIsTypeContent() throws RepositoryException {
        // GIVEN
        Node parentOfParent = session.getRootNode().addNode("parentOfParent", NodeTypes.Content.NAME);
        Node parent = parentOfParent.addNode("parent", NodeTypes.Content.NAME);
        node = parent.addNode("node", NodeTypes.Content.NAME);
        Calendar past = Calendar.getInstance();
        past.set(1970, 3, 3);

        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        parent.setProperty(NodeTypes.LastModified.LAST_MODIFIED, past);
        dialogAction = new SaveDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback());

        // WHEN
        dialogAction.updateLastModified(node);

        // THEN
        Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
        assertTrue(NodeTypes.LastModified.NAME + " of node should have been updated.", past.before(lastModified));
        lastModified = NodeTypes.LastModified.getLastModified(parent);
        assertEquals(NodeTypes.LastModified.NAME + " of parent should not have been updated.", past, lastModified);
    }

}
