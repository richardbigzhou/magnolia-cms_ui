/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.contentapp.autosuggest.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogPresenter;
import info.magnolia.ui.contentapp.autosuggest.ConfiguredTemplateTestDefinition;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationApp;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * AddDefinitionActionTest.
 */
public class AddDefinitionActionTest extends RepositoryTestCase {

    private Session session;
    private Node rootNode;

    private OpenAddDefinitionDialogActionDefinition definition;

    private AppContext appContext;

    private SubAppContext subAppContext;

    private SubAppDescriptor subAppDescriptor;

    private UiContext uiContext;

    private EventBus eventBus;

    private ContentConnector contentConnector;

    private OverlayCloser closeHandle;

    private DialogView dialogView;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        dialogView = mock(DialogView.class);

        when(dialogView.getModalityLevel()).thenReturn(ModalityLevel.LIGHT);

        uiContext = mock(UiContext.class);
        eventBus = mock(EventBus.class);
        contentConnector = mock(ContentConnector.class);

        appContext = mock(AppContext.class);
        subAppContext = mock(SubAppContext.class);
        subAppDescriptor = mock(BrowserSubAppDescriptor.class);

        when(appContext.getActiveSubAppContext()).thenReturn(subAppContext);
        when(subAppContext.getSubAppDescriptor()).thenReturn(subAppDescriptor);

        closeHandle = mock(OverlayCloser.class);
        when(appContext.openOverlay(dialogView, ModalityLevel.LIGHT)).thenReturn(closeHandle);

        definition = new OpenAddDefinitionDialogActionDefinition();
        definition.setAutoSuggesterClass(AutoSuggesterForConfigurationApp.class);
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();
    }

    @Ignore
    @Test
    public void testActionWithEmptyContentNode() throws ActionExecutionException, AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages", NodeTypes.Content.NAME, true);
        Node stkForum = NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages/stkForum", NodeTypes.ContentNode.NAME, true);
        AddDefinitionDialogPresenterMockImpl presenter = createPresenter();
        OpenAddDefinitionDialogAction action = createAction(new JcrNodeAdapter(stkForum), presenter);

        // WHEN
        action.execute();
        Collection<String> subNodeNames = presenter.getSubNodeNames();
        Collection<String> propertyNames = presenter.getPropertyNames();

        // THEN
        assertNotNull(subNodeNames);
        assertNotNull(propertyNames);
        assertEquals(5, subNodeNames.size());
        assertEquals(17, propertyNames.size());
        assertContains(subNodeNames,
                "templateAvailability",
                "autoGeneration",
                "areas",
                "parameters",
                "variations");
        assertContains(propertyNames,
                "renderType",
                "visible",
                "extends",
                "templateScript",
                "writable",
                "moveable",
                "deletable",
                "class",
                "editable",
                "autoPopulateFromRequest",
                "id",
                "title",
                "modelClass",
                "description",
                "name",
                "i18nBasename",
                "dialog");
    }

    @Ignore
    @Test
    public void testActionWithContentNodeAndClassProperty() throws ActionExecutionException, AccessDeniedException, PathNotFoundException, RepositoryException {

        // GIVEN
        NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages", NodeTypes.Content.NAME, true);
        Node stkForum = NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages/stkForum", NodeTypes.ContentNode.NAME, true);
        PropertyUtil.setProperty(stkForum, "class", ConfiguredTemplateTestDefinition.class.getName());
        AddDefinitionDialogPresenterMockImpl presenter = createPresenter();
        OpenAddDefinitionDialogAction action = createAction(new JcrNodeAdapter(stkForum), presenter);

        // WHEN
        action.execute();
        Collection<String> subNodeNames = presenter.getSubNodeNames();
        Collection<String> propertyNames = presenter.getPropertyNames();

        // THEN
        assertNotNull(subNodeNames);
        assertNotNull(propertyNames);
        assertEquals(6, subNodeNames.size());
        assertEquals(17, propertyNames.size());
        assertContains(subNodeNames,
                "templateAvailability",
                "autoGeneration",
                "areas",
                "parameters",
                "variations");
        assertContains(propertyNames,
                "renderType",
                "visible",
                "extends",
                "templateScript",
                "writable",
                "moveable",
                "deletable",
                "editable",
                "autoPopulateFromRequest",
                "id",
                "title",
                "modelClass",
                "description",
                "name",
                "i18nBasename",
                "dialog",
                "testProperty");

    }

    @Ignore
    @Test
    public void testActionWithContentNodeAndClassPropertyAndOtherSubItems() throws ActionExecutionException, AccessDeniedException, PathNotFoundException, RepositoryException {

        // GIVEN
        NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages", NodeTypes.Content.NAME, true);
        Node stkForum = NodeUtil.createPath(rootNode, "/modules/standard-templating-kit/templates/pages/stkForum", NodeTypes.ContentNode.NAME, true);

        stkForum.addNode("templateAvailability", NodeTypes.ContentNode.NAME);
        stkForum.addNode("autoGeneration", NodeTypes.ContentNode.NAME);
        stkForum.addNode("areas", NodeTypes.ContentNode.NAME);

        PropertyUtil.setProperty(stkForum, "class", ConfiguredTemplateTestDefinition.class.getName());
        PropertyUtil.setProperty(stkForum, "templateScript", "");
        PropertyUtil.setProperty(stkForum, "renderType", "");
        PropertyUtil.setProperty(stkForum, "description", "");

        AddDefinitionDialogPresenterMockImpl presenter = createPresenter();
        OpenAddDefinitionDialogAction action = createAction(new JcrNodeAdapter(stkForum), presenter);

        // WHEN
        action.execute();
        Collection<String> subNodeNames = presenter.getSubNodeNames();
        Collection<String> propertyNames = presenter.getPropertyNames();

        // THEN
        assertNotNull(subNodeNames);
        assertNotNull(propertyNames);
        assertEquals(3, subNodeNames.size());
        assertEquals(14, propertyNames.size());
        assertContains(subNodeNames,
                "parameters",
                "variations");
        assertContains(propertyNames,
                "visible",
                "extends",
                "writable",
                "moveable",
                "deletable",
                "editable",
                "autoPopulateFromRequest",
                "id",
                "title",
                "modelClass",
                "name",
                "i18nBasename",
                "dialog",
                "testProperty");

    }

    private void assertContains(Collection<String> items, String... expected) {
        for (String expectedValue : expected) {
            assertTrue(items.contains(expectedValue));
        }
    }

    private OpenAddDefinitionDialogAction createAction(JcrItemAdapter node, AddDefinitionDialogPresenter presenter) {
        return new OpenAddDefinitionDialogAction(definition, appContext, node, presenter);
    }

    private AddDefinitionDialogPresenterMockImpl createPresenter() {
        return new AddDefinitionDialogPresenterMockImpl(dialogView);
    }
}
