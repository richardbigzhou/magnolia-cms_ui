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
package info.magnolia.security.app.action;

import static org.junit.Assert.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterImpl;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Test class for {@link OpenEditRoleDialogAction}.
 */
public class OpenEditRoleDialogActionTest extends RepositoryTestCase {

    private Session config;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        config = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);

        NodeUtil.createPath(config.getRootNode(), "/modules/security-app/dialogs/role", NodeTypes.ContentNode.NAME).setProperty("label", "someRole");
        NodeUtil.createPath(config.getRootNode(), "/modules/security-app/dialogs/role/form/tabs", NodeTypes.ContentNode.NAME);

        ComponentsTestUtil.setImplementation(FormDefinition.class, ConfiguredFormDefinition.class);
        ComponentsTestUtil.setImplementation(TabDefinition.class, ConfiguredTabDefinition.class);
        ComponentsTestUtil.setImplementation(FormDialogDefinition.class, ConfiguredFormDialogDefinition.class);
    }

    @Test
    public void testOpenRoleDialog() throws Exception {
        // GIVEN
        Session session = new MockSession(RepositoryConstants.USER_ROLES);
        Node jcrNode = session.getRootNode().addNode("someRole");
        JcrNodeAdapter itemToEdit = new JcrNodeAdapter(jcrNode);
        MockFormDialogPresenter formDialogPresenter = new MockFormDialogPresenter();

        OpenEditRoleDialogAction<OpenEditRoleDialogActionDefinition> dialogAction = new OpenEditRoleDialogAction<OpenEditRoleDialogActionDefinition>(null, itemToEdit, formDialogPresenter, null, null, null);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("someRole", formDialogPresenter.getDialogDefinition().getLabel());
    }

    private class MockFormDialogPresenter extends FormDialogPresenterImpl {

        private FormDialogDefinition dialogDefinition;

        public MockFormDialogPresenter() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public DialogView start(final Item item, FormDialogDefinition dialogDefinition, final UiContext uiContext, EditorCallback callback) {
            this.setDialogDefinition(dialogDefinition);
            return null;
        }

        protected FormDialogDefinition getDialogDefinition() {
            return dialogDefinition;
        }

        protected void setDialogDefinition(FormDialogDefinition dialogDefinition) {
            this.dialogDefinition = dialogDefinition;
        }
    }
}
