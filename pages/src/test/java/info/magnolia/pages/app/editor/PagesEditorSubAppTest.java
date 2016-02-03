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
package info.magnolia.pages.app.editor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.Event;
import info.magnolia.event.EventBus;
import info.magnolia.event.EventHandler;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.pages.app.editor.location.PagesLocation;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.definition.ConfiguredEditorDefinition;
import info.magnolia.ui.contentapp.detail.ConfiguredDetailSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.StatusBarView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PagesEditorSubApp}.
 */
public class PagesEditorSubAppTest {

    private PageEditorPresenter pageEditorPresenter;
    private ActionExecutor actionExecutor;
    private SubAppContext subAppContext;
    private PagesEditorSubAppView view;
    private EventBus eventBus;
    private EventBus adminCentralEventBus;
    private ActionbarPresenter actionbarPresenter;
    private PageBarView pageBarView;
    private I18NAuthoringSupport i18NAuthoringSupport;
    private I18nContentSupport i18nContentSupport;
    private ConfiguredTemplateDefinition definition;
    private VersionManager versionManager;
    private ConfiguredDetailSubAppDescriptor descriptor;
    private SimpleTranslator i18n;
    private AvailabilityChecker availabilityChecker;
    private ConfiguredEditorDefinition editorDefinition;

    private ConfiguredJcrContentConnectorDefinition connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
    private JcrContentConnector contentConnector;
    private StatusBarView statusBarView;
    private PagesEditorSubApp editor;
    private MockSession session;
    private MockNode root;

    @Before
    public void setUp() throws Exception {

        // GIVEN
        MockWebContext ctx = new MockWebContext();
        session = new MockSession(RepositoryConstants.WEBSITE);
        root = new MockNode(session);

        ctx.addSession(null, session);
        User user = mock(User.class);
        Collection<String> groups = new ArrayList<String>();
        groups.add("this-user-group");
        when(user.getAllGroups()).thenReturn(groups);
        ctx.setUser(user);

        MockRepositoryAcquiringStrategy strategy = new MockRepositoryAcquiringStrategy();
        strategy.addSession(RepositoryConstants.WEBSITE, session);
        ctx.setRepositoryStrategy(strategy);
        MgnlContext.setInstance(ctx);

        actionExecutor = mock(ActionExecutor.class);

        availabilityChecker = mock(AvailabilityChecker.class);
        descriptor = new ConfiguredDetailSubAppDescriptor();
        versionManager = null;
        editorDefinition = new ConfiguredEditorDefinition();

        connectorDefinition.setWorkspace(RepositoryConstants.WEBSITE);
        connectorDefinition.setRootPath("/");
        contentConnector = new JcrContentConnector(versionManager, connectorDefinition, null);

        descriptor.setEditor(editorDefinition);
        subAppContext = new SubAppContextImpl(descriptor, null);
        view = mock(PagesEditorSubAppView.class);
        eventBus = new SimpleEventBus();
        adminCentralEventBus = new ExceptionThrowingEventBus();
        pageEditorPresenter = mock(PageEditorPresenter.class);
        TemplateDefinitionRegistry registry = mock(TemplateDefinitionRegistry.class);
        when(registry.getTemplateDefinition(anyString())).thenReturn(definition);
        actionbarPresenter = mock(ActionbarPresenter.class);

        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);

        i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        when(i18NAuthoringSupport.createI18NURI(any(Node.class), any(Locale.class))).thenReturn("/");

        pageBarView = mock(PageBarView.class);
        statusBarView = mock(StatusBarView.class);

        ComponentsTestUtil.setImplementation(TemplateAvailability.class, ConfiguredTemplateAvailability.class);
        definition = new ConfiguredTemplateDefinition(new ConfiguredTemplateAvailability());

        i18n = mock(SimpleTranslator.class);

        editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus, pageEditorPresenter, actionbarPresenter, pageBarView,
                i18NAuthoringSupport, i18nContentSupport, versionManager, i18n, availabilityChecker, contentConnector, statusBarView);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testPagePreviewSetMgnlPreviewRequestParameter() {
        // GIVEN

        // WHEN
        // param 'view' means preview
        editor.start(new PagesLocation("/:view"));

        // THEN
        assertTrue(editor.getParameters().getUrl().contains("mgnlPreview=true"));
    }

    @Test
    public void testPageEditSetsMgnlPreviewToFalse() {
        // GIVEN
        editor.start(new PagesLocation("/:view"));
        assertTrue(editor.getParameters().getUrl().contains("mgnlPreview=true"));

        // WHEN
        editor.locationChanged(new PagesLocation("/:edit"));

        // THEN
        assertTrue(editor.getParameters().getUrl().contains("mgnlPreview=false"));
    }

    @Test
    public void testPagePreviewSetsMgnlPreviewToTrue() {
        // GIVEN
        editor.start(new PagesLocation("/:edit"));
        assertTrue(editor.getParameters().getUrl().contains("mgnlPreview=false"));

        // WHEN
        editor.locationChanged(new PagesLocation("/:view"));

        // THEN
        assertTrue(editor.getParameters().getUrl().contains("mgnlPreview=true"));
    }

    @Test
    public void testDifferentI18NContentSupportSettingsForDifferentPages() throws RepositoryException {
        // GIVEN
        List<Locale> locales1 = Arrays.asList(new Locale[] {Locale.GERMAN, Locale.FRENCH});
        List<Locale> locales2 = Arrays.asList(new Locale[] {Locale.ENGLISH, Locale.JAPAN});
        Node node1 = root.addNode("test1");
        Node node2 = root.addNode("test2");

        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);

        DefaultI18NAuthoringSupport i18NAuthoringSupport = mock(DefaultI18NAuthoringSupport.class);
        when(i18NAuthoringSupport.createI18NURI(eq(node1), any(Locale.class))).thenReturn("/test1");
        when(i18NAuthoringSupport.createI18NURI(eq(node2), any(Locale.class))).thenReturn("/test2");
        when(i18NAuthoringSupport.getAvailableLocales(node1)).thenReturn(locales1);
        when(i18NAuthoringSupport.getAvailableLocales(node2)).thenReturn(locales2);
        when(i18NAuthoringSupport.getDefaultLocale(node1)).thenReturn(Locale.GERMAN);
        when(i18NAuthoringSupport.getDefaultLocale(node2)).thenReturn(Locale.ENGLISH);

        editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus, pageEditorPresenter, actionbarPresenter, pageBarView,
                i18NAuthoringSupport, i18nContentSupport, versionManager, i18n, availabilityChecker, contentConnector, statusBarView);

        // WHEN
        editor.start(new PagesLocation("/test1:edit"));

        // THEN
        verify(pageBarView).setAvailableLanguages(locales1);
        verify(pageBarView).setCurrentLanguage(Locale.GERMAN);

        // WHEN
        editor.locationChanged(new PagesLocation("/test2:view"));

        // THEN
        verify(pageBarView).setAvailableLanguages(locales2);
        verify(pageBarView).setCurrentLanguage(Locale.ENGLISH);
    }

    @Test
    public void testNodeSelectedUpdatesActionBar() throws Exception {
        // GIVEN

        String testSection = "testSection";
        String actionName = "testAction";

        root.addNode("page", NodeTypes.Page.NAME).addNode("area", NodeTypes.Area.NAME);

        ActionbarDefinition actionbarDefinition = mock(ActionbarDefinition.class);
        descriptor.setActionbar(actionbarDefinition);

        final ActionbarSectionDefinition sectionDefinition = mock(ActionbarSectionDefinition.class);
        final ActionbarItemDefinition itemDefinition = mock(ActionbarItemDefinition.class);
        final ActionbarGroupDefinition groupDefinition = mock(ActionbarGroupDefinition.class);
        ActionDefinition action = mock(ActionDefinition.class);

        when(actionExecutor.getActionDefinition(actionName)).thenReturn(action);
        when(actionbarDefinition.getSections()).thenReturn(new LinkedList<ActionbarSectionDefinition>() {{
            add(sectionDefinition);
        }});
        when(groupDefinition.getItems()).thenReturn(new LinkedList<ActionbarItemDefinition>() {{
            add(itemDefinition);
        }});
        when(sectionDefinition.getGroups()).thenReturn(new LinkedList<ActionbarGroupDefinition>() {{
            add(groupDefinition);
        }});
        when(sectionDefinition.getName()).thenReturn(testSection);
        when(itemDefinition.getName()).thenReturn(actionName);
        when(availabilityChecker.isAvailable(any(AvailabilityDefinition.class), anyList())).thenReturn(true);
        AreaElement element = mock(AreaElement.class);
        when(element.getPath()).thenReturn("/page/area");

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter, times(1)).showSection(testSection);
        verify(actionbarPresenter, times(1)).enable(actionName);
    }

    @Test
    public void testContentChangedExistingNode() throws Exception {
        // GIVEN
        String nodePath = "/existing/node";
        NodeUtil.createPath(root, nodePath, NodeTypes.Page.NAME);

        String instanceId = "someId";
        AppContext appContext = mock(AppContext.class);
        subAppContext.setInstanceId(instanceId);
        subAppContext.setAppContext(appContext);

        subAppContext.setLocation(new DetailLocation("pages", "detail", nodePath + ":edit"));

        JcrItemId itemId = mock(JcrItemId.class);
        when(itemId.getWorkspace()).thenReturn(RepositoryConstants.WEBSITE);

        // WHEN
        adminCentralEventBus.fireEvent(new ContentChangedEvent(itemId));

        // THEN
        verify(appContext, times(0)).closeSubApp(instanceId);
    }

    @Test
    public void testContentChangedOnRemovedNode() throws Exception {
        // GIVEN
        String instanceId = "someId";
        AppContext appContext = mock(AppContext.class);
        subAppContext.setInstanceId(instanceId);
        subAppContext.setAppContext(appContext);

        subAppContext.setLocation(new DetailLocation("pages", "detail", "/non/existing/node:edit"));

        JcrItemId itemId = mock(JcrItemId.class);
        when(itemId.getWorkspace()).thenReturn(RepositoryConstants.WEBSITE);

        // WHEN
        adminCentralEventBus.fireEvent(new ContentChangedEvent(itemId));

        // THEN
        verify(appContext, times(1)).closeSubApp(instanceId);
    }

    @Test
    public void testContentChangedOnNonJcrItemIdDoesNotThrowCCE() throws Exception {
        // GIVEN
        this.editor = spy(editor);
        String itemId = "randomItemId";

        // WHEN
        try {
            adminCentralEventBus.fireEvent(new ContentChangedEvent(itemId));
        }
        catch (ClassCastException e) {
            fail("We must be able to check for all kinds of items without throwing CCE.");
        }

        // THEN - we don't fail

    }

    /**
     * Instead of catching and logging exceptions caught when dispatching events, we throw them.
     */
    private class ExceptionThrowingEventBus extends SimpleEventBus {
        @Override
        public <H extends EventHandler> void fireEvent(Event<H> event) {
            for (H eventHandler : getHandlers(event)) {
                event.dispatch(eventHandler);
            }
        }

        private <H extends EventHandler> Collection<H> getHandlers(Event<H> event) {
            try {
                Method method = SimpleEventBus.class.getDeclaredMethod("internalGetHandlers", Event.class);
                method.setAccessible(true);
                return (Collection) method.invoke(this, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
