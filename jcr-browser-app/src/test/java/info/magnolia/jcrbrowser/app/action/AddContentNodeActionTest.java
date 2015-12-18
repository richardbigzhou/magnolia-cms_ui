/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.jcrbrowser.app.action;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;

public class AddContentNodeActionTest {

    private AddContentNodeAction addContentNodeAction;
    private FormDialogPresenter formDialogPresenter;
    private MockSession session;
    private AddContentNodeActionDefinition definition;

    @Before
    public void setUp() throws Exception {
        final MockContext context = new MockContext();
        MgnlContext.setInstance(context);

        this.definition = new AddContentNodeActionDefinition();
        definition.setDialogName("addContentNode");
        final UiContext uiContext = mock(UiContext.class);

        this.session = new MockSession(RepositoryConstants.CONFIG);
        context.addSession(RepositoryConstants.CONFIG, session);

        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(session.getRootNode());

        this.formDialogPresenter = mock(FormDialogPresenter.class);

        final FormDialogPresenterFactory formDialogPresenterFactory = mock(FormDialogPresenterFactory.class);
        doReturn(formDialogPresenter).when(formDialogPresenterFactory).createFormDialogPresenter(anyString());

        this.addContentNodeAction = new AddContentNodeAction(definition, nodeAdapter, uiContext, formDialogPresenterFactory, mock(SimpleTranslator.class), mock(EventBus.class));
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void createsContentNodeWithSpecifiedNameAndType() throws Exception {
        // GIVEN
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                final Item item = (Item) inv.getArguments()[0];

                item.addItemProperty(AddContentNodeAction.NAME_PID, new ObjectProperty<>("foo"));
                item.addItemProperty(AddContentNodeAction.TYPE_PID, new ObjectProperty<>(NodeTypes.ContentNode.NAME));

                final EditorCallback editorCallback = (EditorCallback) inv.getArguments()[3];
                editorCallback.onSuccess("commit");
                return null;
            }
        }).when(formDialogPresenter).start(any(Item.class), anyString(), any(UiContext.class), any(EditorCallback.class));

        // WHEN
        addContentNodeAction.execute();

        // THEN
        assertThat(session.getRootNode(), hasNode("foo", NodeTypes.ContentNode.NAME));
        verify(formDialogPresenter).closeDialog();
    }
    
    @Test
    public void cancellingActionLeavesNoSideEffects() throws Exception {
        // GIVEN
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                final EditorCallback editorCallback = (EditorCallback) inv.getArguments()[3];
                editorCallback.onCancel();
                return null;
            }
        }).when(formDialogPresenter).start(any(Item.class), anyString(), any(UiContext.class), any(EditorCallback.class));

        // WHEN
        addContentNodeAction.execute();

        // THEN
        assertThat(session.getRootNode().getNodes().getSize(), equalTo(0l));
        verify(formDialogPresenter).closeDialog();
    }
}