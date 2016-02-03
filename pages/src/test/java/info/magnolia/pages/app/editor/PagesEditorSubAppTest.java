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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.pages.app.editor.location.PagesLocation;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.definition.ConfiguredEditorDefinition;
import info.magnolia.ui.contentapp.detail.ConfiguredDetailSubAppDescriptor;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import info.magnolia.ui.workbench.StatusBarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
    private AbstractElement element;
    private ConfiguredTemplateDefinition definition;
    private VersionManager versionManager;
    private ConfiguredDetailSubAppDescriptor descriptor;
    private SimpleTranslator i18n;
    private ConfiguredEditorDefinition editorDefinition;
    private StatusBarView statusBarView;
    private PagesEditorSubApp editor;
    private Session session;

    @Before
    public void setUp() throws Exception {

        // GIVEN
        MockWebContext ctx = new MockWebContext();
        session = mock(Session.class);
        MockNode component = new MockNode();
        component.setProperty(NodeTypes.Renderable.TEMPLATE, "someTemplate");
        when(session.getNode(anyString())).thenReturn(component);
        ctx.addSession(null, session);
        User user = mock(User.class);
        Collection<String> groups = new ArrayList<String>();
        groups.add("this-user-group");
        when(user.getAllGroups()).thenReturn(groups);
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);

        actionExecutor = mock(ActionExecutor.class);

        descriptor = new ConfiguredDetailSubAppDescriptor();
        versionManager = null;
        descriptor.setEditor(new ConfiguredEditorDefinition());
        subAppContext = new SubAppContextImpl(descriptor, null);
        view = mock(PagesEditorSubAppView.class);
        eventBus = new SimpleEventBus();
        adminCentralEventBus = new SimpleEventBus();
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
                i18NAuthoringSupport, i18nContentSupport, versionManager, i18n, statusBarView);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testButtonsVisibilityIsNotChangedForOtherThenComponentElement() {
        // GIVEN
        element = new AreaElement(null, null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection(PagesEditorSubApp.ALL_SECTIONS);
        verify(actionbarPresenter).showSection(PagesEditorSubApp.SECTION_AREA);
        verify(actionbarPresenter).disable(PageEditorPresenter.ACTION_CANCEL_MOVE_COMPONENT, PageEditorPresenter.ACTION_COPY_COMPONENT,
                PageEditorPresenter.ACTION_PASTE_COMPONENT, PageEditorPresenter.ACTION_UNDO, PageEditorPresenter.ACTION_REDO);
        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testHidingButtonsBasedOnOperationPermissionsForComponent() {
        // GIVEN
        ComponentElement element = new ComponentElement(null, null, null);
        element.setMoveable(true);
        element.setDeletable(false);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection(PagesEditorSubApp.ALL_SECTIONS);
        verify(actionbarPresenter).showSection("componentActions");
        verify(actionbarPresenter).disable(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT, PageEditorListener.ACTION_COPY_COMPONENT,
                PageEditorListener.ACTION_PASTE_COMPONENT, PageEditorListener.ACTION_UNDO, PageEditorListener.ACTION_REDO);
        verify(actionbarPresenter).disable(PageEditorListener.ACTION_DELETE_COMPONENT);
        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testHidingButtonsBasedOnOperationPermissionsForArea() {
        // GIVEN
        AreaElement element = new AreaElement(null, null, null, null);
        element.setAddible(false);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection(PagesEditorSubApp.ALL_SECTIONS);
        verify(actionbarPresenter).showSection(PagesEditorSubApp.SECTION_AREA);
        verify(actionbarPresenter).disable(PageEditorListener.ACTION_ADD_COMPONENT);
        verify(actionbarPresenter).disable(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT, PageEditorListener.ACTION_COPY_COMPONENT,
                PageEditorListener.ACTION_PASTE_COMPONENT, PageEditorListener.ACTION_UNDO, PageEditorListener.ACTION_REDO);
        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testEnableOrDisablePagePreviewActionsBasedOnUserPermissions() {
        // GIVEN
        String unavailableAction = "bar";
        String availableAction = "foo";

        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName(PagesEditorSubApp.SECTION_PAGE_PREVIEW);

        ConfiguredActionbarGroupDefinition group = new ConfiguredActionbarGroupDefinition();
        ConfiguredActionbarItemDefinition item = new ConfiguredActionbarItemDefinition();
        item.setName(unavailableAction);
        group.addItem(item);

        ConfiguredActionbarItemDefinition item2 = new ConfiguredActionbarItemDefinition();
        item2.setName(availableAction);
        group.addItem(item2);

        section.addGroup(group);
        actionbar.addSection(section);
        descriptor.setActionbar(actionbar);

        PageElement element = new PageElement(null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);

        when(actionExecutor.isAvailable(unavailableAction, null)).thenReturn(false);
        when(actionExecutor.isAvailable(availableAction, null)).thenReturn(true);

        // param 'view' means preview
        editor.start(new PagesLocation("/:view"));

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).showSection(PagesEditorSubApp.SECTION_PAGE_PREVIEW);
        verify(actionbarPresenter).disable(unavailableAction);
        verify(actionbarPresenter).enable(availableAction);
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
    public void testActionAvailabilityIsProcessedAlsoForAreaSection() {
        // GIVEN
        element = new AreaElement(null, null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);

        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        ConfiguredActionbarGroupDefinition group = new ConfiguredActionbarGroupDefinition();
        ConfiguredActionbarItemDefinition item = new ConfiguredActionbarItemDefinition();

        item.setName(PageEditorListener.ACTION_ADD_COMPONENT);
        group.addItem(item);
        section.setName(PagesEditorSubApp.SECTION_AREA);
        section.addGroup(group);
        actionbar.addSection(section);
        descriptor.setActionbar(actionbar);

        ConfiguredAvailabilityDefinition availability = new ConfiguredAvailabilityDefinition();
        availability.setRoot(false);
        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setAvailability(availability);
        when(actionExecutor.getActionDefinition(PageEditorListener.ACTION_ADD_COMPONENT)).thenReturn(actionDefinition);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).disable(PageEditorListener.ACTION_ADD_COMPONENT);
    }

    @Test
    public void testDifferentI18NContentSupportSettingsForDifferentPages() throws RepositoryException {
        // GIVEN
        List<Locale> locales1 = Arrays.asList(new Locale[] {Locale.GERMAN, Locale.FRENCH});
        List<Locale> locales2 = Arrays.asList(new Locale[] {Locale.ENGLISH, Locale.JAPAN});
        MockNode node1 = new MockNode();
        MockNode node2 = new MockNode();
        when(session.getNode("/test1")).thenReturn(node1);
        when(session.getNode("/test2")).thenReturn(node2);

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

        editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus,
                pageEditorPresenter, actionbarPresenter, pageBarView, i18NAuthoringSupport, i18nContentSupport,
                versionManager, i18n, statusBarView);

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

}
