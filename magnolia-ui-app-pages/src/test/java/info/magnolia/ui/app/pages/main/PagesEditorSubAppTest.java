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
package info.magnolia.ui.app.pages.main;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.app.pages.editor.NodeSelectedEvent;
import info.magnolia.ui.app.pages.editor.PageEditorPresenter;
import info.magnolia.ui.app.pages.editor.PagesEditorSubApp;
import info.magnolia.ui.app.pages.editor.PagesEditorSubAppView;
import info.magnolia.ui.contentapp.definition.ConfiguredEditorDefinition;
import info.magnolia.ui.contentapp.detail.ConfiguredDetailSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Session;

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
    private ActionbarPresenter actionbarPresenter;
    private AbstractElement element;
    private final ConfiguredTemplateDefinition definition = new ConfiguredTemplateDefinition();

    @Before
    public void setUp() throws Exception {

        // GIVEN
        MockWebContext ctx = new MockWebContext();
        Session session = mock(Session.class);
        MockNode component = new MockNode();
        component.setProperty("mgnl:template", "someTemplate");
        when(session.getNode(anyString())).thenReturn(component);
        ctx.addSession(null, session);
        User user = mock(User.class);
        Collection<String> groups = new ArrayList<String>();
        groups.add("this-user-group");
        when(user.getAllGroups()).thenReturn(groups);
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);

        actionExecutor = mock(ActionExecutor.class);
        ConfiguredDetailSubAppDescriptor descriptor = new ConfiguredDetailSubAppDescriptor();
        descriptor.setEditor(new ConfiguredEditorDefinition());
        subAppContext = new SubAppContextImpl(descriptor, null);
        view = mock(PagesEditorSubAppView.class);
        eventBus = new SimpleEventBus();
        pageEditorPresenter = mock(PageEditorPresenter.class);
        TemplateDefinitionRegistry registry = mock(TemplateDefinitionRegistry.class);
        when(pageEditorPresenter.getTemplateDefinitionRegistry()).thenReturn(registry);
        when(registry.getTemplateDefinition(anyString())).thenReturn(definition);
        actionbarPresenter = mock(ActionbarPresenter.class);
    }

    @Test
    public void testButtonsVisibilityIsNotChangedForOtherThenComponentElement() {
        // GIVEN
        element = new AreaElement(null, null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, eventBus, pageEditorPresenter, actionbarPresenter);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions");
        verify(actionbarPresenter).showSection("areaActions");
        verify(actionbarPresenter).disable("moveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testButtonsShouldBeEnabledWhenTheRightsAreNotDefinedInTemplateDefinition() {
        // GIVEN
        element = new ComponentElement(null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, eventBus, pageEditorPresenter, actionbarPresenter);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions");
        verify(actionbarPresenter).showSection("componentActions");
        verify(actionbarPresenter).disable("moveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verify(actionbarPresenter).enable(PagesEditorSubApp.ACTION_DELETE_COMPONENT);
        verify(actionbarPresenter).enable(PagesEditorSubApp.ACTION_EDIT_COMPONENT);
        verify(actionbarPresenter).enable(PagesEditorSubApp.ACTION_MOVE_COMPONENT);

        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testButtonsShouldBeEnabledAccordingToRightsFromTemplateDefinition() {
        // GIVEN
        element = new ComponentElement(null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        final Location location = new DetailLocation(null, null, "");
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, eventBus, pageEditorPresenter, actionbarPresenter);

        definition.setCanDelete("some-other-group,someNextGroup");
        definition.setCanEdit("this-user-group");
        definition.setCanMove("some-other-group,this-user-group");

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions");
        verify(actionbarPresenter).showSection("componentActions");
        verify(actionbarPresenter).disable("moveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verify(actionbarPresenter).disable(PagesEditorSubApp.ACTION_DELETE_COMPONENT);
        verify(actionbarPresenter).enable(PagesEditorSubApp.ACTION_EDIT_COMPONENT);
        verify(actionbarPresenter).enable(PagesEditorSubApp.ACTION_MOVE_COMPONENT);

        verifyNoMoreInteractions(actionbarPresenter);
    }
}
