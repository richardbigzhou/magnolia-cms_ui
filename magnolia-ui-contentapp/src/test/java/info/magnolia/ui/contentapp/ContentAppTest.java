/**
 * This file Copyright (c) 2013 Magnolia International
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.event.ChooseDialogEventBus;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

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

    @Before
    public void setUp() {
        AppContext ctx = mock(AppContext.class);
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        GuiceComponentProvider provider = mock(GuiceComponentProvider.class);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {}
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


    public static class MockChooseDialogEventBusClient {
        @Inject
        public MockChooseDialogEventBusClient(@Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        }
    }
}
