/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.TextField;

/**
 * Tests for the {@link RestoreVersionAction}.
 */
public class RestoreVersionActionTest extends RepositoryTestCase {

    private final String CREATED_VERSION_BEFORE_RESTORE = "Created automatically before performing restore.";

    private Node node;

    private RestoreVersionActionDefinition definition;

    private FormDialogPresenter formDialogPresenter;
    private UiContext uiContext;

    private EventBus eventBus;
    private SimpleTranslator i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        definition = new RestoreVersionActionDefinition();

        formDialogPresenter = mock(FormDialogPresenter.class);
        uiContext = mock(UiContext.class);
        eventBus = mock(EventBus.class);
        i18n = mock(SimpleTranslator.class);
        when(i18n.translate("ui-contentapp.actions.restoreVersion.comment.restore")).thenReturn(CREATED_VERSION_BEFORE_RESTORE);

        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        node = webSiteSession.getRootNode().addNode("test", NodeTypes.Page.NAME);
        NodeTypes.Created.set(node);
        node.setProperty("mgnl:template", "home");
        node.addNode("areaSubNode", NodeTypes.Area.NAME);
        node.getNode("areaSubNode").setProperty("content", "version");
        webSiteSession.save();
    }

    @Test
    public void testRestoreVersion() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals("section", node.getProperty("mgnl:template").getString());
        assertFalse(node.hasNode("areaSubNode"));

        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, item, i18n, versionMan, eventBus);
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        assertEquals("home", node.getProperty("mgnl:template").getString());
        assertTrue(node.hasNode("areaSubNode"));
        assertEquals("version", node.getNode("areaSubNode").getProperty("content").getString());
    }

    @Test
    public void testCheckVersionCreatedBeforeRestore() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals(2, versionMan.getAllVersions(node).getSize());

        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, item, i18n, versionMan, eventBus);
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        assertEquals(3, versionMan.getAllVersions(node).getSize());
        Version version =versionMan.getVersion(node,"1.1");
        assertEquals(CREATED_VERSION_BEFORE_RESTORE, NodeTypes.Versionable.getComment(version));
        assertEquals("section", version.getProperty("mgnl:template").getString());
        assertFalse(version.hasNode("areaSubNode"));
    }

    @Test
    public void testDoNotCreateVersionBeforeRestoreIfNotAllowed() throws Exception {
        // GIVEN
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals(2, versionMan.getAllVersions(node).getSize());

        definition.setCreateVersionBeforeRestore(false);

        AbstractJcrNodeAdapter item = new JcrNodeAdapter(node);
        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, item, i18n, versionMan, eventBus);
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        assertEquals(2, versionMan.getAllVersions(node).getSize());
    }

    private class MockRestoreVersionAction extends RestoreVersionAction {

        private String version = "1.0";

        public MockRestoreVersionAction(RestoreVersionActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
            super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, versionManager, eventBus);
        }

        @Override
        protected BeanItem<?> getItem() {

            BeanItem<?> item = super.getItem();
            Property property = new TextField();
            property.setValue(version);
            item.addItemProperty("versionName", property);
            return item;
        }

        protected void setSelectedVersion(String version){
            this.version = version;
        }
    }

}
