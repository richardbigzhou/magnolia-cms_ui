/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.VersionConfig;
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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.TextField;

/**
 * Tests for the {@link RestoreVersionAction}.
 */
public class RestoreVersionActionTest extends RepositoryTestCase {

    private final String CREATED_VERSION_BEFORE_RESTORE = "ui-contentapp.actions.restoreVersion.comment.restore";

    private Node node;

    private RestoreVersionActionDefinition definition;

    private FormDialogPresenter formDialogPresenter;
    private UiContext uiContext;

    private EventBus eventBus;
    private SimpleTranslator i18n;

    private VersionManager versionManager;
    private VersionConfig versionConfig;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        versionManager = VersionManager.getInstance();
        versionConfig = new VersionConfig();
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
        versionManager.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals("section", node.getProperty("mgnl:template").getString());
        assertFalse(node.hasNode("areaSubNode"));

        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, new JcrNodeAdapter(node), i18n, versionManager, eventBus, versionConfig);
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
        versionManager.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals(2, versionManager.getAllVersions(node).getSize());

        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, new JcrNodeAdapter(node), i18n, versionManager, eventBus, versionConfig);
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        assertEquals(3, versionManager.getAllVersions(node).getSize());
        Version version = versionManager.getVersion(node, "1.1");
        assertEquals(CREATED_VERSION_BEFORE_RESTORE, NodeTypes.Versionable.getComment(version));
        assertEquals("section", version.getProperty("mgnl:template").getString());
        assertFalse(version.hasNode("areaSubNode"));
    }

    @Test
    public void testDoNotCreateVersionBeforeRestoreIfNotAllowed() throws Exception {
        // GIVEN
        versionManager.addVersion(node);

        // change test node before restore
        node.setProperty("mgnl:template", "section");
        node.getNode("areaSubNode").remove();
        node.getSession().save();

        assertEquals(2, versionManager.getAllVersions(node).getSize());

        definition.setCreateVersionBeforeRestore(false);

        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, new JcrNodeAdapter(node), i18n, versionManager, eventBus, versionConfig);
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        assertEquals(2, versionManager.getAllVersions(node).getSize());
    }

    /**
     * We expect the second confirmation to be shown, as we're restoring the oldest version. However the restore will
     * not trigger the creation of another version just before the restore itself.
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-3220">MGNLUI-3220</a>
     */
    @Test
    public void testRestoreVersionWhenMultipleVersionsExistsAndRestoreOldest() throws Exception {
        // GIVEN

        // This Version will go as the version store will be full
        createVersion(node, "prop", "test 1"); // 1.0
        assertThat(versionManager.getAllVersions(node).getSize(), is(2L));

        // This is the version we are going to restore (the oldest one)
        Version version1_1 = createVersion(node, "prop", "test 2"); // 1.1
        assertThat(versionManager.getAllVersions(node).getSize(), is(3L));

        // Add another version to fill the version store
        Version version1_2 = createVersion(node, "prop", "test 3"); // 1.2
        assertThat(versionManager.getAllVersions(node).getSize(), is(4L));

        // Add another version to fill the version store
        Version version1_3 = createVersion(node, "prop", "test 4"); // 1.3
        assertThat(versionManager.getAllVersions(node).getSize(), is(4L));

        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, new JcrNodeAdapter(node), i18n, versionManager, eventBus, versionConfig);
        restoreVersionAction.setSelectedVersion(version1_1.getName());
        restoreVersionAction.execute();

        restoreVersionAction.getEditorCallback().onSuccess(""); // First callback confirm

        // Test before
        VersionIterator versionIterator = versionManager.getAllVersions(node);
        assertThat(node.getProperty("prop").getString(), is("test 4")); // no restore yet
        assertThat(versionIterator.getSize(), is(4L));
        // Versions store shouldn't change
        assertThat(versionIterator.nextVersion().getName(), is("jcr:rootVersion"));
        assertThat(versionIterator.nextVersion().getName(), is(version1_1.getName()));
        assertThat(versionIterator.nextVersion().getName(), is(version1_2.getName()));
        assertThat(versionIterator.nextVersion().getName(), is(version1_3.getName()));

        // WHEN
        restoreVersionAction.getConfirmationCallback().onSuccess(); // Second callback confirm

        // THEN
        // Test after
        versionIterator = versionManager.getAllVersions(node);
        assertThat(node.getProperty("prop").getString(), is("test 2")); // 1.1 was restored
        assertThat(versionIterator.getSize(), is(4L));
        // Versions store shouldn't change
        assertThat(versionIterator.nextVersion().getName(), is("jcr:rootVersion"));
        assertThat(versionIterator.nextVersion().getName(), is(version1_1.getName()));
        assertThat(versionIterator.nextVersion().getName(), is(version1_2.getName()));
        assertThat(versionIterator.nextVersion().getName(), is(version1_3.getName()));
    }

    /**
     * We expect the second confirmation to not be shown, as we're not restoring the oldest version.
     */
    @Test
    public void testRestoreVersionWhenMultipleVersionsExistsAndRestoreMiddle() throws Exception {
        // GIVEN

        // This Version will go as the version store will be full
        createVersion(node, "prop", "test 1"); // 1.0
        assertThat(versionManager.getAllVersions(node).getSize(), is(2L));

        // This version will be replace with the version created before restore
        createVersion(node, "prop", "test 2"); // 1.1
        assertThat(versionManager.getAllVersions(node).getSize(), is(3L));

        // This version will be restored
        Version version1_2 = createVersion(node, "prop", "test 3"); // 1.2
        assertThat(versionManager.getAllVersions(node).getSize(), is(4L));

        // This version should stay is the version store
        Version version1_3 = createVersion(node, "prop", "test 4"); // 1.3
        assertThat(versionManager.getAllVersions(node).getSize(), is(4L));

        MockRestoreVersionAction restoreVersionAction = new MockRestoreVersionAction(definition, null, null, uiContext, formDialogPresenter, new JcrNodeAdapter(node), i18n, versionManager, eventBus, versionConfig);
        restoreVersionAction.setSelectedVersion(version1_2.getName());
        restoreVersionAction.execute();

        // WHEN
        restoreVersionAction.getEditorCallback().onSuccess("");

        // THEN
        VersionIterator versionIterator = versionManager.getAllVersions(node);
        assertThat(node.getProperty("prop").getString(), is("test 3"));
        assertThat(versionIterator.getSize(), is(4L));
        assertThat(versionIterator.nextVersion().getName(), is("jcr:rootVersion"));
        assertThat(versionIterator.nextVersion().getName(), is(version1_2.getName()));
        assertThat(versionIterator.nextVersion().getName(), is(version1_3.getName()));
        assertThat(versionIterator.nextVersion().getName(), is("1.4")); // Version created before restore
    }

    private Version createVersion(final Node node, final String property, final String value) throws RepositoryException {
        node.setProperty(property, value);
        node.getSession().save();

        return versionManager.addVersion(node);
    }

    /**
     * Test class of {@link RestoreVersionAction}.
     */
    private class MockRestoreVersionAction extends RestoreVersionAction {

        private String version = "1.0";

        public MockRestoreVersionAction(RestoreVersionActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, VersionConfig versionConfig) {
            super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, versionManager, eventBus, versionConfig);
        }

        @Override
        protected BeanItem<?> getItem() {
            BeanItem<?> item = super.getItem();
            Property property = new TextField();
            property.setValue(version);
            item.addItemProperty(VersionName.PROPERTY_NAME_VERSION_NAME, property);
            return item;
        }

        protected void setSelectedVersion(String version) {
            this.version = version;
        }
    }

}
