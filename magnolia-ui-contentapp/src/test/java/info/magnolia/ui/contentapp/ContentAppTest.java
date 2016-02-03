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
package info.magnolia.ui.contentapp;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.browser.ConfiguredBrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests {@link ContentApp}.
 */
public class ContentAppTest {

    private static final String WORKSPACE = "workspace";

    private AppContext appContext;
    private ComponentProvider componentProvider;

    private String rootPath;

    @Before
    public void setUp() {
        appContext = mock(AppContext.class);

        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);

        // mock ChooseDialogPresenter to check for path in given definition upon #start
        rootPath = null;
        ChooseDialogPresenter chooseDialogPresenter = mock(ChooseDialogPresenter.class);
        doAnswer(new Answer<ChooseDialogView>() {

            @Override
            public ChooseDialogView answer(InvocationOnMock invocation) throws Throwable {
                ChooseDialogDefinition definition = (ChooseDialogDefinition) invocation.getArguments()[1];
                rootPath = ((WorkbenchFieldDefinition) definition.getField()).getWorkbench().getPath();
                return null;
            }
        }).when(chooseDialogPresenter).start(any(ChooseDialogCallback.class), any(ChooseDialogDefinition.class), any(UiContext.class), anyString());

        // componentProvider has to be a GuiceComponentProvider (impl details)
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerInstance(ModuleRegistry.class, moduleRegistry);
        configuration.registerInstance(ChooseDialogPresenter.class, chooseDialogPresenter);
        componentProvider = new GuiceComponentProviderBuilder().withConfiguration(configuration).build();

        initConfiguration();
    }

    /**
     * Tests that the component provider that will be used for choose dialog is created successfully
     * and contains the correct mapping of the {@link EventBus}. It first creates the ComponentProvider and then attempts
     * to create a new instance of a class that needs the choose-dialog scoped eventbus.
     */
    @Test
    public void testCreateChooseDialogComponentProvider() throws Exception {
        // GIVEN
        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);

        //WHEN
        GuiceComponentProvider chooseDialogProvider = (GuiceComponentProvider) app.createChooseDialogComponentProvider();
        MockChooseDialogEventBusClient client = chooseDialogProvider.newInstance(MockChooseDialogEventBusClient.class);

        //THEN
        assertNotNull(client);
    }

    @Test
    public void testOpenChooseDialogWithDefaultRootPathFromSubApp() throws Exception {
        // GIVEN
        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);

        // WHEN
        app.openChooseDialog(null, null, null);

        // THEN
        assertEquals("/root", rootPath);
    }

    @Test
    public void testOpenChooseDialogWithGivenRootPath() throws Exception {
        // GIVEN
        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);

        // WHEN
        app.openChooseDialog(null, "/test", null, null);

        // THEN
        assertEquals("/test", rootPath);
    }

    private AppDescriptor initConfiguration() {
        ConfiguredContentAppDescriptor app = new ConfiguredContentAppDescriptor();
        ConfiguredBrowserSubAppDescriptor browser = new ConfiguredBrowserSubAppDescriptor();
        ConfiguredChooseDialogDefinition chooseDialog = new ConfiguredChooseDialogDefinition();
        ConfiguredWorkbenchDefinition workbench = new ConfiguredWorkbenchDefinition();
        workbench.setWorkspace(WORKSPACE);
        workbench.setPath("/root");
        browser.setWorkbench(workbench);
        app.addSubApp(browser);
        app.setChooseDialog(chooseDialog);

        when(appContext.getAppDescriptor()).thenReturn(app);
        when(appContext.getDefaultSubAppDescriptor()).thenReturn(browser);

        return app;
    }

    /**
     * A client class that requires a ChooseDialogEventBus.
     */
    public static class MockChooseDialogEventBusClient {
        @Inject
        public MockChooseDialogEventBusClient(@Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        }
    }
}
