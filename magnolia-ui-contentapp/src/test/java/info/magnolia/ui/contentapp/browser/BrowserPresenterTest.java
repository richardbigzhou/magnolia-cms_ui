/**
 * This file Copyright (c) 2012-2014 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ActionEvent;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Tests covering business logic in {@link BrowserPresenter}.
 */
public class BrowserPresenterTest {

    private final static String APP_NAME = "browserPresenterTestApp";

    private final static String SUB_APP_NAME = "browser";

    private final static String WORKSPACE = "workspace";

    private final static String ROOT_PATH = "/";

    private final static String DUMMY_NODE_NAME = "johnNode";

    private final static String DUMMY_PROPERTY_NAME = "crashDummy";

    private final static String USER = "penny";

    private EventBus subAppEventBus;

    private MockSession session;

    private BrowserPresenter presenter;

    private ConfiguredBrowserSubAppDescriptor browserSubAppDescriptor;

    private ActionExecutor actionExecutor;

    private WorkbenchPresenter mockWorkbenchPresenter;

    private AvailabilityChecker availabilityChecker = mock(AvailabilityChecker.class);

    @Before
    public void setUp() {
        // spy hooks for session move to avoid unsupported operation exception
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(createMockUser(USER));
        MgnlContext.setInstance(ctx);

        initBrowserPresenter();
    }

    private void initBrowserPresenter() {
        // initialize test instance
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(WORKSPACE);
        workbenchDefinition.setPath(ROOT_PATH);
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());

        browserSubAppDescriptor = new ConfiguredBrowserSubAppDescriptor();
        browserSubAppDescriptor.setName(SUB_APP_NAME);
        browserSubAppDescriptor.setWorkbench(workbenchDefinition);

        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setName(APP_NAME);
        appDescriptor.getSubApps().put(browserSubAppDescriptor.getName(), browserSubAppDescriptor);

        Shell mockShell = mock(Shell.class);
        SubAppContext subAppContext = new SubAppContextImpl(browserSubAppDescriptor, mockShell);

        BrowserView mockView = mock(BrowserView.class);
        subAppEventBus = new SimpleEventBus();

        EventBus adminCentralEventBus = mock(EventBus.class);
        mockWorkbenchPresenter = mock(WorkbenchPresenter.class);
        ActionbarPresenter mockActionbarPresenter = mock(ActionbarPresenter.class);
        actionExecutor = mock(ActionExecutor.class);

        presenter = new BrowserPresenter(actionExecutor, subAppContext, mockView, adminCentralEventBus, subAppEventBus, mockActionbarPresenter, mockWorkbenchPresenter, null, null, availabilityChecker);

        // start presenter (binds event handlers)
        presenter.start();
    }

    private User createMockUser(String name) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        return user;
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testEditItem() throws Exception {
        // GIVEN
        Node node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        node.addMixin(LastModified.NAME);
        node.setProperty(DUMMY_PROPERTY_NAME, false);
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        boolean newValue = true;
        Property<Boolean> itemProperty = adapter.getItemProperty(DUMMY_PROPERTY_NAME);
        itemProperty.setValue(newValue);

        // WHEN
        subAppEventBus.fireEvent(new ActionEvent(SaveConfigDialogActionDefinition.class.getSimpleName(), adapter.getItemId(), DUMMY_PROPERTY_NAME, itemProperty));

        // THEN
        assertEquals(newValue, node.getProperty(DUMMY_PROPERTY_NAME).getBoolean());
    }

    @Test
    public void testEditItemDoesNotUpdateLastModifiedWithoutChanges() throws Exception {
        // GIVEN
        Node node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        LastModified.update(node);
        AbstractJcrNodeAdapter adapter = mock(AbstractJcrNodeAdapter.class);
        when(adapter.applyChanges()).thenReturn(node);

        Calendar firstModified = LastModified.getLastModified(node);
        String firstModifiedBy = LastModified.getLastModifiedBy(node);

        Property<?> itemProperty = adapter.getItemProperty(DUMMY_PROPERTY_NAME);

        // WHEN
        subAppEventBus.fireEvent(new ActionEvent(SaveConfigDialogActionDefinition.class.getSimpleName(), adapter.getItemId(), DUMMY_PROPERTY_NAME, itemProperty));

        // THEN
        assertNotNull(LastModified.getLastModified(node));
        assertNotNull(LastModified.getLastModifiedBy(node));
        assertEquals(firstModified, LastModified.getLastModified(node));
        assertEquals(firstModifiedBy, LastModified.getLastModifiedBy(node));
    }

    @Test
    public void defaultActionIsNotExecutedWhenNotAvailableForSelectedItem() throws Exception {
        // GIVEN
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testDefaultAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        Node node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        Object itemId = JcrItemUtil.getItemId(node);
        AvailabilityDefinition availability = actionExecutor.getActionDefinition("testDefaultAction").getAvailability();
        when(availabilityChecker.isAvailable(availability, Arrays.asList(itemId))).thenReturn(false);
        List<Object> ids = new ArrayList<Object>(1);
        ids.add(new JcrItemId(node.getIdentifier(),WORKSPACE));
        when(mockWorkbenchPresenter.getSelectedIds()).thenReturn(ids);
        when(mockWorkbenchPresenter.getWorkspace()).thenReturn(WORKSPACE);

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(node.getPath()));

        // THEN
        // just verifying that the method has NOT been called with the proper action name, not the Item(s) passed to it
        verify(actionExecutor, never()).execute(eq("testDefaultAction"), anyObject());
    }

    @Test
    public void defaultActionIsExecutedWhenAvailableForSelectedItem() throws Exception {
        // GIVEN
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testDefaultAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        Node node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        Object itemId = JcrItemUtil.getItemId(node);
        AvailabilityDefinition availability = actionExecutor.getActionDefinition("testDefaultAction").getAvailability();
        when(availabilityChecker.isAvailable(availability, Arrays.asList(itemId))).thenReturn(true);

        List<Object> ids = new ArrayList<Object>(1);
        ids.add(node.getIdentifier());
        when(mockWorkbenchPresenter.getSelectedIds()).thenReturn(ids);
        when(mockWorkbenchPresenter.getWorkspace()).thenReturn(WORKSPACE);

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(node.getPath()));

        // THEN
        // just verifying that the method has been called with the proper action name, not the Item(s) passed to it
        verify(actionExecutor).execute(eq("testDefaultAction"), anyObject());
    }

    @Test
    public void passesNullToActionExecutorIsAvailableWhenWorkbenchRootIsSelected() throws Exception {
        // GIVEN
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        Node node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        WorkbenchDefinition wb = mock(WorkbenchDefinition.class);
        when(wb.getWorkspace()).thenReturn(WORKSPACE);
        when(wb.getPath()).thenReturn(node.getPath());
        browserSubAppDescriptor.setWorkbench(wb);
        List<Object> ids = new ArrayList<Object>(1);
        ids.add(node.getIdentifier());
        when(mockWorkbenchPresenter.getSelectedIds()).thenReturn(ids);
        when(mockWorkbenchPresenter.getWorkspace()).thenReturn(WORKSPACE);

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(node.getPath()));

        // THEN
        // just verifying that null is passed to the isAvailable() method instead of the actual item
        AvailabilityDefinition availability = actionExecutor.getActionDefinition("testDefaultAction").getAvailability();
        verify(availabilityChecker).isAvailable(availability, null);
    }

}
