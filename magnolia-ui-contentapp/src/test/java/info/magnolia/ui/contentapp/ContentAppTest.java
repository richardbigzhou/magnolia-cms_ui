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

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rits.cloning.Cloner;

/**
 * Tests {@link ContentApp}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ContentApp.class)
public class ContentAppTest {

    private ContentApp app;
    private AppContext ctx;
    private GuiceComponentProvider provider;

    @Before
    public void setUp() {
        ctx = mock(AppContext.class);
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        provider = mock(GuiceComponentProvider.class);

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

    }

    /**
     * Tests that the component provider that will be used for choose dialog is created successfully
     * and contains the correct mapping of the {@link EventBus}. It first creates the ComponentProvider and then attempts
     * to create a new instance of a class that needs the choose-dialog scoped eventbus.
     */
    @Test
    public void testCreateChooseDialogComponentProvider() throws Exception {
        //WHEN
        GuiceComponentProvider chooseDialogProvider = (GuiceComponentProvider) app.createChooseDialogComponentProvider();
        MockChooseDialogEventBusClient client = chooseDialogProvider.newInstance(MockChooseDialogEventBusClient.class);

        //THEN
        assertNotNull(client);
    }

    @Test
    public void testTargetTreeRootPathIsSetInWorkbenchDefinition() throws Exception {
        // GIVEN
        Cloner mockCloner = mock(Cloner.class);
        whenNew(Cloner.class).withNoArguments().thenReturn(mockCloner);

        final ComponentProvider componentProvider = mock(ComponentProvider.class);
        app = new ContentApp(ctx, mock(AppView.class), provider) {
            @Override
            ComponentProvider createChooseDialogComponentProvider() {
                return componentProvider;
            }
        };

        UiContext uiContext = mock(UiContext.class);
        ChooseDialogCallback callback = mock(ChooseDialogCallback.class);
        ContentAppDescriptor appDescriptor = mock(ContentAppDescriptor.class);
        ChooseDialogDefinition dialogDefinition = mock(ChooseDialogDefinition.class);
        ChooseDialogDefinition clonedDialogDefinition = mock(ChooseDialogDefinition.class);
        WorkbenchFieldDefinition fieldDefinition = mock(WorkbenchFieldDefinition.class);
        WorkbenchFieldDefinition clonedFieldDefinition = mock(WorkbenchFieldDefinition.class);
        ConfiguredWorkbenchDefinition clonedWorkbenchDefinition = mock(ConfiguredWorkbenchDefinition.class);
        ChooseDialogPresenter presenter = mock(ChooseDialogPresenter.class);
        Class<ChooseDialogPresenter> presenterClass = ChooseDialogPresenter.class;

        when(ctx.getAppDescriptor()).thenReturn(appDescriptor);
        doReturn(presenterClass).when(dialogDefinition).getPresenterClass();
        when(componentProvider.getComponent(ChooseDialogPresenter.class)).thenReturn(presenter);
        when(appDescriptor.getChooseDialog()).thenReturn(dialogDefinition);
        when(dialogDefinition.getField()).thenReturn(fieldDefinition);

        when(mockCloner.deepClone(dialogDefinition)).thenReturn(clonedDialogDefinition);
        when(clonedDialogDefinition.getField()).thenReturn(clonedFieldDefinition);
        when(clonedFieldDefinition.getWorkbench()).thenReturn(clonedWorkbenchDefinition);

        // WHEN
        app.openChooseDialog(uiContext, "/path", "", callback);

        // GIVEN
        verify(clonedWorkbenchDefinition).setPath("/path");
        verify(presenter).start(callback, clonedDialogDefinition, uiContext, "");
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
