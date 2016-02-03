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
package info.magnolia.ui.contentapp.browser;


import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
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
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.contentapp.browser.action.SaveItemPropertyAction;
import info.magnolia.ui.contentapp.browser.action.SaveItemPropertyActionDefinition;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
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

    private ConfiguredBrowserSubAppDescriptor browserSubAppDescriptor = new ConfiguredBrowserSubAppDescriptor();

    private WorkbenchPresenter workbenchPresenter;

    private AvailabilityChecker availabilityChecker = mock(AvailabilityChecker.class);

    private ActionExecutor actionExecutor;

    private JcrContentConnector contentConnector;
    private Node node;
    private JcrNodeAdapter adapter;
    private ConfiguredActionDefinition defaultActionDefinition;
    private ConfiguredJcrContentConnectorDefinition connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
    private EventBus admincentralEventBus;

    @Before
    public void setUp() throws RepositoryException {
        // spy hooks for session move to avoid unsupported operation exception
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(createMockUser(USER));
        MgnlContext.setInstance(ctx);

        initBrowserPresenter();
    }

    private void initBrowserPresenter() throws RepositoryException {

        // initialize test instance
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());

        browserSubAppDescriptor.setName(SUB_APP_NAME);
        browserSubAppDescriptor.setWorkbench(workbenchDefinition);

        defaultActionDefinition = new ConfiguredActionDefinition();

        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setName(APP_NAME);
        appDescriptor.getSubApps().put(browserSubAppDescriptor.getName(), browserSubAppDescriptor);

        Shell mockShell = mock(Shell.class);
        SubAppContext subAppContext = new SubAppContextImpl(browserSubAppDescriptor, mockShell);

        BrowserView browserView = mock(BrowserView.class);
        subAppEventBus = new SimpleEventBus();

        admincentralEventBus = new SimpleEventBus();
        workbenchPresenter = mock(WorkbenchPresenter.class);
        ActionbarPresenter actionBarPresenter = mock(ActionbarPresenter.class);

        actionExecutor = mock(ActionExecutor.class);

        connectorDefinition.setWorkspace(WORKSPACE);
        connectorDefinition.setRootPath(ROOT_PATH);
        contentConnector = mock(JcrContentConnector.class);
        doReturn(connectorDefinition).when(contentConnector).getContentConnectorDefinition();
        node = session.getRootNode().addNode(DUMMY_NODE_NAME);
        adapter = new JcrNodeAdapter(node);
        doReturn(adapter).when(contentConnector).getItem(anyObject());

        ImageProvider imageProvider = mock(ImageProvider.class);

        presenter = new BrowserPresenter(browserView, subAppContext, actionExecutor, admincentralEventBus, subAppEventBus, contentConnector, imageProvider, workbenchPresenter, actionBarPresenter, availabilityChecker);

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
        SaveItemPropertyActionDefinition definition = new SaveItemPropertyActionDefinition();
        definition.setImplementationClass(SaveItemPropertyAction.class);
        browserSubAppDescriptor.getActions().put("SaveItemPropertyAction", definition);

        node.addMixin(LastModified.NAME);
        node.setProperty(DUMMY_PROPERTY_NAME, false);
        Set<Object> ids = new HashSet<Object>();
        ids.add(adapter.getItemId());
        doReturn(true).when(availabilityChecker).isAvailable(eq(definition.getAvailability()), anyList());

        boolean newValue = true;
        Property<Boolean> itemProperty = adapter.getItemProperty(DUMMY_PROPERTY_NAME);
        itemProperty.setValue(newValue);
        final SaveItemPropertyAction action = new SaveItemPropertyAction(definition, mock(EventBus.class), contentConnector, ids, DUMMY_PROPERTY_NAME, itemProperty);
        doReturn(definition).when(actionExecutor).getActionDefinition("SaveItemPropertyAction");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                action.execute();
                return false;
            }
        }).when(actionExecutor).execute(eq("SaveItemPropertyAction"), anyVararg());

        // WHEN
        subAppEventBus.fireEvent(new ActionEvent(SaveItemPropertyAction.class.getSimpleName(), adapter.getItemId(), DUMMY_PROPERTY_NAME, itemProperty));

        // THEN
        assertEquals(newValue, node.getProperty(DUMMY_PROPERTY_NAME).getBoolean());
    }

    @Test
    public void testEditItemDoesNotUpdateLastModifiedWithoutChanges() throws Exception {
        // GIVEN
        node.setProperty(DUMMY_PROPERTY_NAME, false);
        LastModified.update(node);

        Property<?> itemProperty = adapter.getItemProperty(DUMMY_PROPERTY_NAME);

        Calendar firstModified = LastModified.getLastModified(node);
        String firstModifiedBy = LastModified.getLastModifiedBy(node);

        Set<Object> ids = new HashSet<Object>();
        ids.add(adapter.getItemId());

        SaveItemPropertyActionDefinition definition = new SaveItemPropertyActionDefinition();
        final SaveItemPropertyAction action = new SaveItemPropertyAction(definition, mock(EventBus.class), contentConnector, ids, DUMMY_PROPERTY_NAME, itemProperty);
        doReturn(definition).when(actionExecutor).getActionDefinition("SaveItemPropertyAction");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                action.execute();
                return false;
            }
        }).when(actionExecutor).execute(eq("SaveItemPropertyAction"), anyVararg());

        // WHEN
        subAppEventBus.fireEvent(new ActionEvent(SaveItemPropertyAction.class.getSimpleName(), adapter.getItemId(), DUMMY_PROPERTY_NAME, itemProperty));

        // THEN
        assertNotNull(LastModified.getLastModified(node));
        assertNotNull(LastModified.getLastModifiedBy(node));
        assertEquals(firstModified, LastModified.getLastModified(node));
        assertEquals(firstModifiedBy, LastModified.getLastModifiedBy(node));
    }

    @Test
    public void defaultActionIsNotExecutedWhenNotAvailableForSelectedItem() throws Exception {
        // GIVEN
        doReturn(defaultActionDefinition).when(actionExecutor).getActionDefinition("testDefaultAction");
        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testDefaultAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        when(availabilityChecker.isAvailable(eq(defaultActionDefinition.getAvailability()), anyList())).thenReturn(false);

        List<Object> ids = new ArrayList<Object>(1);
        ids.add(new JcrItemId(node.getIdentifier(), WORKSPACE));
        when(workbenchPresenter.getSelectedIds()).thenReturn(ids);

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(node.getPath()));

        // THEN
        // just verifying that the method has NOT been called with the proper action name, not the Item(s) passed to it
        verify(actionExecutor, never()).execute(eq("testDefaultAction"), anyObject());
    }

    @Test
    public void defaultActionIsExecutedWhenAvailableForSelectedItem() throws Exception {
        // GIVEN
        doReturn(defaultActionDefinition).when(actionExecutor).getActionDefinition(anyString());


        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testDefaultAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        AvailabilityDefinition availability = actionExecutor.getActionDefinition("testDefaultAction").getAvailability();
        when(availabilityChecker.isAvailable(eq(availability), anyList())).thenReturn(true);

        List<Object> ids = new ArrayList<Object>(1);
        ids.add(node.getIdentifier());
        when(workbenchPresenter.getSelectedIds()).thenReturn(ids);

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(node.getPath()));

        // THEN
        // just verifying that the method has been called with the proper action name, not the Item(s) passed to it
        verify(actionExecutor).execute(eq("testDefaultAction"), anyVararg());
    }

    @Test
    public void passesNullToActionExecutorIsAvailableWhenWorkbenchRootIsSelected() throws Exception {
        // GIVEN
        ConfiguredActionDefinition testActionDefinition = new ConfiguredActionDefinition();
        doReturn(testActionDefinition).when(actionExecutor).getActionDefinition(anyString());

        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setDefaultAction("testAction");
        browserSubAppDescriptor.setActionbar(actionbar);

        WorkbenchDefinition wb = mock(WorkbenchDefinition.class);
        //when(wb.getWorkspace()).thenReturn(WORKSPACE);
        //when(wb.getPath()).thenReturn(node.getPath());
        browserSubAppDescriptor.setWorkbench(wb);
        List<Object> ids = new ArrayList<Object>(1);
        ids.add(node.getIdentifier());
        when(workbenchPresenter.getSelectedIds()).thenReturn(ids);

        AvailabilityDefinition availability = testActionDefinition.getAvailability();

        // WHEN
        subAppEventBus.fireEvent(new ItemDoubleClickedEvent(JcrItemUtil.getItemId(node)));

        // THEN
        // just verifying that null is passed to the isAvailable() method instead of the actual item

        verify(availabilityChecker).isAvailable(eq(availability), anyList());
    }

    @Test
    public void handleMultiSelectedItemsOnActionClick() throws Exception {

        // GIVEN
        Node node1 = session.getRootNode().addNode("node1");
        Node node2 = session.getRootNode().addNode("node2");
        List<Object> ids = new ArrayList<Object>();
        ids.add(node1.getIdentifier());
        ids.add(node2.getIdentifier());
        when(presenter.getSelectedItemIds()).thenReturn(ids);

        List<Item> selectedItems = new ArrayList<Item>();
        selectedItems.add(contentConnector.getItem(ids.get(0)));
        selectedItems.add(contentConnector.getItem(ids.get(1)));

        ConfiguredActionDefinition myActionDefinition = new ConfiguredActionDefinition();
        myActionDefinition.setName("myAction");
        doReturn(myActionDefinition).when(actionExecutor).getActionDefinition(myActionDefinition.getName());
        when(availabilityChecker.isAvailable(myActionDefinition.getAvailability(), ids)).thenReturn(true);

        // WHEN
        presenter.onActionbarItemClicked(myActionDefinition.getName());

        // THEN
        verify(actionExecutor).execute(eq(myActionDefinition.getName()), eq(selectedItems));
    }

    @Test
    public void handleOneSelectedItemOnActionClick() throws Exception {

        // GIVEN
        List<Object> ids = new ArrayList<Object>();
        ids.add(node.getIdentifier());
        when(presenter.getSelectedItemIds()).thenReturn(ids);

        List<Item> selectedItems = new ArrayList<Item>();
        selectedItems.add(contentConnector.getItem(ids.get(0)));

        ConfiguredActionDefinition myActionDefinition = new ConfiguredActionDefinition();
        myActionDefinition.setName("myAction");

        doReturn(myActionDefinition).when(actionExecutor).getActionDefinition(myActionDefinition.getName());
        when(availabilityChecker.isAvailable(myActionDefinition.getAvailability(), ids)).thenReturn(true);

        // WHEN
        presenter.onActionbarItemClicked(myActionDefinition.getName());

        // THEN
        verify(actionExecutor).execute(eq(myActionDefinition.getName()), eq(selectedItems.get(0)), eq(selectedItems));
    }

    @Test
    public void contentChangedEventCheckIfItemCanBeHandledByContentConnector() throws Exception {
        // GIVEN
        given(contentConnector.canHandleItem(node.getIdentifier())).willReturn(true);
        given(contentConnector.getItem(anyObject())).willReturn(null);
        given(presenter.getSelectedItemIds()).willReturn(Lists.<Object>newArrayList(node.getIdentifier()));

        // Save the deletion request.
        node.remove();
        session.save();

        // WHEN
        admincentralEventBus.fireEvent(new ContentChangedEvent(node.getIdentifier()));

        // THEN
        verify(workbenchPresenter).select(eq(Collections.emptyList()));
    }
}
