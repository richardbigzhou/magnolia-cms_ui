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
package info.magnolia.ui.contentapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.browser.ConfiguredBrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.definition.ConfiguredContentSubAppDescriptor;
import info.magnolia.ui.contentapp.renderer.SelectionSensitiveActionRenderer;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.definition.ActionRendererDefinition;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.DefaultContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ContentApp}.
 */
public class ContentAppTest {

    private static final String WORKSPACE = "workspace";

    private AppContext appContext;
    private ComponentProvider componentProvider;

    private static String rootPath;
    private static MockChooseDialogPresenter chooseDialogPresenter;

    @Before
    public void setUp() {
        appContext = mock(AppContext.class);

        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);

        // mock ChooseDialogPresenter to check for path in given definition upon #start
        rootPath = null;
        chooseDialogPresenter = null;

        // componentProvider has to be a GuiceComponentProvider (impl details)
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerInstance(ModuleRegistry.class, moduleRegistry);
        configuration.addTypeMapping(ChooseDialogPresenter.class, MockChooseDialogPresenter.class);
        configuration.registerInstance(ContentConnector.class, DefaultContentConnector.class); // global content connector
        configuration.registerInstance(ImageProvider.class, mock(ImageProvider.class)); // global image provider
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
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        app.openChooseDialog(uiContext, null, null);

        // THEN
        assertNotNull(chooseDialogPresenter);
        assertTrue(chooseDialogPresenter.eventBus instanceof SimpleEventBus);
    }

    @Test
    public void testAddAvailabilityActionRenderer() throws Exception {
        // GIVEN
        ConfiguredChooseDialogDefinition definition = mock(ConfiguredChooseDialogDefinition.class);
        Map<String, ActionRendererDefinition> rendereres = new HashMap<String, ActionRendererDefinition>();

        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);
        Method privateMethod = ContentApp.class.getDeclaredMethod("addAvailabilityActionRenderer", ChooseDialogDefinition.class);
        privateMethod.setAccessible(true);

        // WHEN
        when(definition.getActionArea()).thenReturn(mock(EditorActionAreaDefinition.class));
        when(definition.getActionArea().getActionRenderers()).thenReturn(rendereres);
        definition = (ConfiguredChooseDialogDefinition) privateMethod.invoke(app, definition);

        // THEN
        assertTrue(rendereres.size() > 0);
        assertTrue(definition.getActionArea().getActionRenderers().containsKey("commit"));
        assertEquals(SelectionSensitiveActionRenderer.class, definition.getActionArea().getActionRenderers().get("commit").getRendererClass());
    }

    @Test
    public void testCreateComponentProviderWithImageProviderBinding() throws Exception {
        // GIVEN
        // add imageProvider config
        ConfiguredImageProviderDefinition imageProviderDefinition = new ConfiguredImageProviderDefinition();
        imageProviderDefinition.setImageProviderClass(MockImageProvider.class);
        ((ConfiguredContentSubAppDescriptor) appContext.getDefaultSubAppDescriptor()).setImageProvider(imageProviderDefinition);

        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        app.openChooseDialog(uiContext, null, null);

        // THEN
        assertNotNull(chooseDialogPresenter);
        ImageProvider imageProvider = chooseDialogPresenter.imageProvider;
        assertEquals(MockImageProvider.class, imageProvider.getClass());
    }

    @Test
    public void testCreateComponentProviderWithDependentChooseDialogBindings() throws Exception {
        // GIVEN
        // add imageProvider config
        ConfiguredImageProviderDefinition imageProviderDefinition = new ConfiguredImageProviderDefinition();
        imageProviderDefinition.setImageProviderClass(MockImageProvider.class);
        ((ConfiguredContentSubAppDescriptor) appContext.getDefaultSubAppDescriptor()).setImageProvider(imageProviderDefinition);

        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        app.openChooseDialog(uiContext, null, null);

        // THEN
        assertNotNull(chooseDialogPresenter);
        ImageProvider imageProvider = chooseDialogPresenter.imageProvider;
        assertEquals(MockImageProvider.class, imageProvider.getClass());
        assertEquals(MockContentConnector.class, ((MockImageProvider) imageProvider).contentConnector.getClass());

    }

    @Test
    public void testOpenChooseDialogWithDefaultRootPathFromSubApp() throws Exception {
        // GIVEN
        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        app.openChooseDialog(uiContext, null, null);

        // THEN
        assertEquals("/root", rootPath);
    }

    @Test
    public void testOpenChooseDialogWithGivenRootPath() throws Exception {
        // GIVEN
        ContentApp app = new ContentApp(appContext, mock(AppView.class), componentProvider);
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        app.openChooseDialog(uiContext, "/test", null, null);

        // THEN
        assertEquals("/test", rootPath);
    }

    private AppDescriptor initConfiguration() {
        ConfiguredContentAppDescriptor app = new ConfiguredContentAppDescriptor();
        ConfiguredBrowserSubAppDescriptor browser = new ConfiguredBrowserSubAppDescriptor();
        ConfiguredChooseDialogDefinition chooseDialog = new ConfiguredChooseDialogDefinition();
        ConfiguredWorkbenchDefinition workbench = new ConfiguredWorkbenchDefinition();
        ConfiguredJcrContentConnectorDefinition contentConnector = new ConfiguredJcrContentConnectorDefinition();
        contentConnector.setWorkspace(WORKSPACE);
        contentConnector.setRootPath("/root");
        contentConnector.setImplementationClass(MockContentConnector.class);
        browser.setWorkbench(workbench);
        browser.setContentConnector(contentConnector);
        app.addSubApp(browser);
        app.setChooseDialog(chooseDialog);

        when(appContext.getAppDescriptor()).thenReturn(app);
        when(appContext.getDefaultSubAppDescriptor()).thenReturn(browser);

        return app;
    }

    /**
     * A dummy {@link ImageProvider} to test injection within choose-dialogs.
     */
    public static class MockImageProvider implements ImageProvider {

        private final ContentConnector contentConnector;

        @Inject
        public MockImageProvider(ContentConnector contentConnector) {
            // make sure guice provider for ImageProvider itself uses correct choose-dialog component provider
            this.contentConnector = contentConnector;
        }

        @Override
        public String getPortraitPath(Object itemId) {
            return null;
        }

        @Override
        public String getThumbnailPath(Object itemId) {
            return null;
        }

        @Override
        public String resolveIconClassName(String mimeType) {
            return null;
        }

        @Override
        public Object getThumbnailResource(Object itemId, String generator) {
            return null;
        }
    }

    /**
     * A dummy {@link ContentConnector} to test injecting dependent choose-dialogs providers.
     */
    public static class MockContentConnector extends DefaultContentConnector {
    }

    /**
     * A dummy {@link ChooseDialogPresenter} to check the incoming {@link ChooseDialogDefinition},
     * and to test injecting from within the choose-dialog ioc container.
     */
    private static class MockChooseDialogPresenter implements ChooseDialogPresenter {

        private final EventBus eventBus;
        private final ImageProvider imageProvider;

        @Inject
        public MockChooseDialogPresenter(@Named(ChooseDialogEventBus.NAME) EventBus eventBus, ImageProvider imageProvider) {
            this.eventBus = eventBus;
            this.imageProvider = imageProvider;
        }

        @Override
        public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, UiContext uiContext, String itemId) {
            ContentAppTest.rootPath = ((JcrContentConnectorDefinition) definition.getContentConnector()).getRootPath();
            ContentAppTest.chooseDialogPresenter = this;
            return null;
        }

        @Override
        public DialogView start(DialogDefinition definition, UiContext uiContext) {
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

        @Override
        public ChooseDialogView getView() {
            return null;
        }
    }

}
