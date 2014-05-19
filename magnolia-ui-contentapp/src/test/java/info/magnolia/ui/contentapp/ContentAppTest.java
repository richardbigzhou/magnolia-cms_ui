/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.contentapp;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests {@link ContentApp}.
 */
public class ContentAppTest {

    private ContentApp app;
    private AppContext ctx;

    @Before
    public void setUp() {
        ctx = mock(AppContext.class);
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        GuiceComponentProvider provider = mock(GuiceComponentProvider.class);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });

        doReturn(injector).when(provider).getInjector();
        doReturn(MockChooseDialogEventBusClient.class).when(provider).getImplementation(MockChooseDialogEventBusClient.class);
        doReturn(moduleRegistry).when(provider).getComponent(ModuleRegistry.class);
        doReturn(new ArrayList<ModuleDefinition>()).when(moduleRegistry).getModuleDefinitions();

        this.app = new ContentApp(ctx, mock(AppView.class), provider);

        MockContext mockContext = new MockContext();
        mockContext.addSession("mgnlVersion", new MockSession("mgnlVersion"));
        ComponentsTestUtil.setImplementation(VersionManager.class, VersionManager.class);
        ComponentsTestUtil.setInstance(SystemContext.class, mockContext);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    /**
     * Tests that the component provider that will be used for choose dialog is created successfully
     * and contains the correct mapping of the {@link EventBus}. It first creates the ComponentProvider and then attempts
     * to create a new instance of a class that needs the choose-dialog scoped eventbus.
     */
    @Test
    public void testCreateChooseDialogComponentProvider() throws Exception {
        //WHEN
        GuiceComponentProvider chooseDialogProvider = (GuiceComponentProvider) app.createChooseDialogComponentProvider(new ConfiguredChooseDialogDefinition());
        MockChooseDialogEventBusClient client = chooseDialogProvider.newInstance(MockChooseDialogEventBusClient.class);

        //THEN
        assertNotNull(client);
    }

    @Test
    public void testTargetTreeRootPathIsSetInContentConnector() throws Exception {
        // GIVEN
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(ModuleRegistry.class, ModuleRegistryImpl.class);
        configuration.registerImplementation(VersionManager.class);
        GuiceComponentProvider componentProvider = new GuiceComponentProviderBuilder().withConfiguration(configuration).build();

        UiContext uiContext = mock(UiContext.class);
        ChooseDialogCallback callback = mock(ChooseDialogCallback.class);
        ContentSubAppDescriptor subAppDescriptor = mock(ContentSubAppDescriptor.class);
        ContentAppDescriptor contentAppDescriptor = mock(ContentAppDescriptor.class);
        ConfiguredChooseDialogDefinition chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
        chooseDialogDefinition.setPresenterClass(TestChooseDialogPresenter.class);
        ConfiguredJcrContentConnectorDefinition jcrContentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();

        when(ctx.getAppDescriptor()).thenReturn(contentAppDescriptor);
        when(contentAppDescriptor.getChooseDialog()).thenReturn(chooseDialogDefinition);
        when(ctx.getDefaultSubAppDescriptor()).thenReturn(subAppDescriptor);
        when(subAppDescriptor.getContentConnector()).thenReturn(jcrContentConnectorDefinition);

        app = new ContentApp(ctx, mock(AppView.class), componentProvider);
        app.openChooseDialog(uiContext, "/test", "", callback);

        // WHEN
        GuiceComponentProvider chooseDialogComponentProvider = (GuiceComponentProvider) app.createChooseDialogComponentProvider(chooseDialogDefinition);
        TestChooseDialogPresenter presenter = chooseDialogComponentProvider.newInstance(TestChooseDialogPresenter.class, chooseDialogComponentProvider);

        // THEN
        JcrContentConnectorDefinition definition = ((JcrContentConnector) presenter.getContentConnector()).getContentConnectorDefinition();
        assertEquals("/test", definition.getRootPath());
    }

    /**
     * A client class that requires a ChooseDialogEventBus.
     */
    public static class MockChooseDialogEventBusClient {
        @Inject
        public MockChooseDialogEventBusClient(@Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        }
    }

    public static class TestChooseDialogPresenter implements ChooseDialogPresenter {

        private ContentConnector contentConnector;

        @Inject
        public TestChooseDialogPresenter(ContentConnector contentConnector) {
            this.contentConnector = contentConnector;
        }

        @Override
        public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, UiContext uiContext, String itemId) {
            return null;
        }

        @Override
        public DialogView start(DialogDefinition definition, UiContext uiContext) {
            return null;
        }

        @Override
        public ChooseDialogView getView() {
            return null;
        }

        @Override
        public ActionAreaPresenter getActionArea() {
            return null;
        }

        @Override
        public void addShortcut(String actionName, int keyCode, int... modifiers) {
        }

        @Override
        public void closeDialog() {
        }

        public ContentConnector getContentConnector() {
            return contentConnector;
        }
    }
}
