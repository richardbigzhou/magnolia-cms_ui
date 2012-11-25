/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.form.action;

import com.vaadin.data.Item;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * SaveFormActionTest.
 */
public class SaveFormActionTest extends RepositoryTestCase {
    private SaveFormActionDefinition formActionDefinition;
    private CallbackFormActionTest.FormPresenterTest presenter;
    private MockSession session;
    private Node node;
    private Item item;
    private SaveFormAction formAction;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.formActionDefinition = new SaveFormActionDefinition();
        this.presenter = new CallbackFormActionTest.FormPresenterTest();
        //Init Node
        session = new MockSession("config");
        MockContext ctx = new MockContext();
        ctx.setUser(new MgnlUser("userName", "realm", new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>()));
        ctx.addSession("config", session);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void executeDefaultExecuteTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.getSession().save();
        item = new JcrNodeAdapter(node);
        this.presenter.setTestItem(item);
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, presenter);

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(name)", this.presenter.getCallbackActionCalled());
    }

    @Test
    public void executeSaveUpdatePropertyTest() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("property", "initial");
        item = new JcrNodeAdapter(node);
        item.getItemProperty("property").setValue("changed");
        this.presenter.setTestItem(item);
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, presenter);

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
        item.addItemProperty("property", DefaultPropertyUtil.newDefaultProperty("property", null, "changed"));
        this.presenter.setTestItem(item);
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, presenter);

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
        this.presenter.setTestItem(item);
        initDefinition("name", "label");
        formAction = new SaveFormAction(formActionDefinition, presenter);
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
