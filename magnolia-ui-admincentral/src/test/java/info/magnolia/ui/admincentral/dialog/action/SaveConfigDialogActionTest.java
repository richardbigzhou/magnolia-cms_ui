/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link SaveConfigDialogAction} and {@link SaveConfigDialogActionDefinition}.
 */
public class SaveConfigDialogActionTest extends MgnlTestCase {

    private final SaveConfigDialogActionDefinition definition = new SaveConfigDialogActionDefinition();
    private EditorCallback callback;
    private EditorValidator validator;
    private EventBus subAppEventBus;
    private Session session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        callback = mock(EditorCallback.class);
        validator = mock(EditorValidator.class);
        doReturn(true).when(validator).isValid();
        subAppEventBus = mock(EventBus.class);

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
        subAppEventBus = null;
        MgnlContext.setInstance(null);
    }

    @Test
    public void executeFiresSelectionChangeEvent() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Node node = session.getRootNode().addNode("underlying");
        node.setProperty("test", "test");
        node.getSession().save();
        Item item = new JcrPropertyAdapter(node.getProperty("test"));
        item.getItemProperty("jcrName").setValue("1");
        SaveConfigDialogAction dialogAction = new SaveConfigDialogAction(definition, item, validator, callback, subAppEventBus);

        // WHEN
        dialogAction.execute();

        // THEN
        verify(subAppEventBus, only()).fireEvent(any(SelectionChangedEvent.class));
    }

}
