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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link SaveDialogAction} and {@link SaveDialogActionDefinition}.
 */
public class SaveDialogActionTest extends MgnlTestCase {

    private static final String ACTION_NAME = "commit";

    private final SaveDialogActionDefinition definition = new SaveDialogActionDefinition();
    private EditorCallback callback;
    private EditorValidator validator;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        definition.setName(ACTION_NAME);
        callback = mock(EditorCallback.class);
        validator = mock(EditorValidator.class);
        doReturn(true).when(validator).isValid();

        // Mock session
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        session = null;
        MgnlContext.setInstance(null);
    }

    @Test
    public void executeFiresCallback() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.getSession().save();
        Item item = new JcrNodeAdapter(node);
        SaveDialogAction<SaveDialogActionDefinition> action = new SaveDialogAction<>(definition, item, validator, callback);

        // WHEN
        action.execute();

        // THEN
        verify(callback, only()).onSuccess(eq(ACTION_NAME));
    }

    @Test
    public void executeUpdatesProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        Item item = new JcrNodeAdapter(node);
        item.getItemProperty("property").setValue("changed");
        SaveDialogAction<SaveDialogActionDefinition> action = new SaveDialogAction<>(definition, item, validator, callback);

        // WHEN
        action.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void executeCreatesProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        Item item = new JcrNodeAdapter(node);
        item.addItemProperty("property", DefaultPropertyUtil.newDefaultProperty(String.class, "changed"));
        SaveDialogAction<SaveDialogActionDefinition> action = new SaveDialogAction<>(definition, item, validator, callback);

        // WHEN
        action.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(true, node.hasProperty("property"));
        assertEquals("changed", node.getProperty("property").getString());
    }

    @Test
    public void executeRemovesProperty() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        Item item = new JcrNodeAdapter(node);
        item.removeItemProperty("property");
        assertEquals(true, node.hasProperty("property"));
        SaveDialogAction<SaveDialogActionDefinition> action = new SaveDialogAction<>(definition, item, validator, callback);

        // WHEN
        action.execute();

        // THEN
        node = session.getRootNode().getNode("underlying");
        assertEquals(false, node.hasProperty("property"));
    }
}
