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
package info.magnolia.ui.admincentral.tree.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.DeleteCommand;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Main Test class of {@link DeleteAction}.
 */
public class DeleteActionTest extends RepositoryTestCase {
    private CommandsManager commandsManager;
    private DeleteActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();
    private DeleteCommand deleteCommand;
    private Node referenceNode;
    private EventBus eventBus;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init Command
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        referenceNode = webSiteSession.getRootNode().addNode("referenceNode");
        referenceNode.addNode("article1", NodeTypes.Page.NAME);
        referenceNode.getNode("article1").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article1").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article1").setProperty("property_string", "property");
        referenceNode.addNode("article2", NodeTypes.Page.NAME);
        referenceNode.getNode("article2").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article2").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article2").setProperty("property_string", "property");

        deleteCommand = new DeleteCommand();

        // Init Action and CommandManager
        definition = new DeleteActionDefinition();
        definition.setCommand("delete");

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        // see for why this is needed.
        ComponentsTestUtil.setInstance(Map.class, params);

        CommandsManager commandsManagerTmp = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode().addNode("modules", NodeTypes.ContentNode.NAME).addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME).addNode("delete", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManagerTmp.register(ContentUtil.asContent(exportModuleDef.getParent()));
        commandsManager = spy(commandsManagerTmp);
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "delete")).thenReturn(deleteCommand);
        when(commandsManager.getCommand("delete")).thenReturn(deleteCommand);

        eventBus = mock(EventBus.class);
    }

    @Test
    public void testDeteteNode() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(referenceNode.getNode("article1"));
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, item, commandsManager, eventBus, new TestSubAppContext(true));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertFalse(referenceNode.hasNode("article1"));
    }

    @Test
    public void testDeteteNodeCancelChanges() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(referenceNode.getNode("article1"));
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, item, commandsManager, eventBus, new TestSubAppContext(false));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertTrue(referenceNode.hasNode("article1"));
    }

    @Test
    public void testDeteteProperty() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrPropertyAdapter(referenceNode.getNode("article1").getProperty("property_long"));
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, item, commandsManager, eventBus, new TestSubAppContext(true));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertTrue(referenceNode.hasNode("article1"));
        assertTrue(referenceNode.getNode("article1").hasProperty("property_boolean"));
        assertTrue(referenceNode.getNode("article1").hasProperty("property_string"));
        assertFalse(referenceNode.getNode("article1").hasProperty("property_long"));
    }


    /**
     * Basic Empty implementation of {@link SubAppContext} for test purpose.
     */
    public static class TestSubAppContext implements SubAppContext {
        private boolean validateChanges;

        public TestSubAppContext(boolean validateChanges) {
            this.validateChanges = validateChanges;
        }

        @Override
        public OverlayCloser openOverlay(View view) {
            return null;
        }

        @Override
        public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
            return null;
        }

        @Override
        public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
            if (validateChanges) {
                cb.onSuccess();
            } else {
                cb.onCancel();
            }
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        }

        @Override
        public String getSubAppId() {
            return null;
        }

        @Override
        public SubApp getSubApp() {
            return null;
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public AppContext getAppContext() {
            return null;
        }

        @Override
        public SubAppDescriptor getSubAppDescriptor() {
            return null;
        }

        @Override
        public void setAppContext(AppContext appContext) {

        }

        @Override
        public void setLocation(Location location) {

        }

        @Override
        public void setSubApp(SubApp subApp) {
        }

        @Override
        public void setInstanceId(String instanceId) {

        }

        @Override
        public String getInstanceId() {
            return null;
        }

        @Override
        public void close() {
        }

    }
}
