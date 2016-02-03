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
package info.magnolia.ui.contentapp.detail;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.definition.ConfiguredEditorDefinition;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.framework.app.SubAppContextImpl;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * The DetailSubAppTest.
 */
public class DetailSubAppTest {

    private static final String WORKSPACE = "workspace";

    private EditorDefinition editorDefinition;

    private Session session;

    private DetailSubApp detailSubApp;

    private String editedNodePath;

    @Before
    public void setUp() {
        session = new MockSession(WORKSPACE);
        MockWebContext ctx = new MockWebContext();
        ctx.addSession(WORKSPACE, session);
        MgnlContext.setInstance(ctx);

        initDetailSubapp();
    }

    private void initDetailSubapp() {

        ContentSubAppView view = mock(ContentSubAppView.class);
        EventBus adminCentralEventBus = mock(EventBus.class);
        SimpleTranslator i18n = mock(SimpleTranslator.class);

        // mock subAppContext to return basic definitions
        SubAppContext subAppContext = new SubAppContextImpl(new ConfiguredDetailSubAppDescriptor() {

            @Override
            public EditorDefinition getEditor() {
                return editorDefinition;
            };
        }, null);

        // mock presenter so that we can assess on currently edited itemId
        DetailEditorPresenter presenter = mock(DetailEditorPresenter.class);
        doAnswer(new Answer<View>() {

            @Override
            public View answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                editedNodePath = (String) args[0];
                return null;
            }

        }).when(presenter).start(anyString(), any(DetailView.ViewType.class));

        editedNodePath = null;
        detailSubApp = new DetailSubApp(subAppContext, view, adminCentralEventBus, presenter, i18n);
    }

    @Test
    public void testStart() throws Exception {
        // GIVEN
        editorDefinition = new ConfiguredEditorDefinition();
        Node node = NodeUtil.createPath(session.getRootNode(), "node", NodeTypes.ContentNode.NAME);

        // WHEN
        detailSubApp.start(new DetailLocation("appName", "subAppId", "/node"));

        // THEN
        assertEquals(node.getPath(), editedNodePath);
    }

}
