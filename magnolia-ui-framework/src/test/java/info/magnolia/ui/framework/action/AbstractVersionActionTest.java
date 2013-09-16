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
package info.magnolia.ui.framework.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.core.version.VersionInfo;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AbstractVersionAction}.
 */
public class AbstractVersionActionTest extends RepositoryTestCase {

    protected static final String CONTEXT_PATH = "/ctx";

    private TestAbstractVersionActionDefinition definition;
    private UiContext uiContext;
    private FormDialogPresenter formDialogPresenter;
    private TestLocationController locationController;
    private VersionManager versionManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockWebContext ctx = (MockWebContext) MgnlContext.getInstance();
        ctx.setUser(new MgnlUser("admin", "admin", Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP, null, null));
        ctx.setContextPath(CONTEXT_PATH);

        MgnlContext.setInstance(ctx);
        MgnlContext.setLocale(Locale.ENGLISH);

        definition = new TestAbstractVersionActionDefinition();
        uiContext = mock(UiContext.class);
        formDialogPresenter = mock(FormDialogPresenter.class);
        locationController = new TestLocationController(mock(EventBus.class), mock(Shell.class));

        versionManager = VersionManager.getInstance();
    }

    @Test
    public void testGetVersionInfoList() throws ActionExecutionException, RepositoryException {
        // GIVEN
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node node = webSiteSession.getRootNode().addNode("testNodeVersionInfo", NodeTypes.Page.NAME);

        TestAbstractVersionAction action = new TestAbstractVersionAction(definition, locationController, uiContext, formDialogPresenter , new JcrNodeAdapter(node));

        Version version = versionManager.addVersion(node);

        // WHEN
        List<VersionInfo> versionInfos = action.getAvailableVersionInfoList();

        // THEN
        assertTrue(!versionInfos.isEmpty());
        assertTrue(versionInfos.size() == 1);
        assertEquals(version.getName(), versionInfos.get(0).getVersionName());
    }

    @Test
    public void testGetVersionLabel() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node node = webSiteSession.getRootNode().addNode("testNodeVersionLabel", NodeTypes.Page.NAME);

        TestAbstractVersionAction action = new TestAbstractVersionAction(definition, locationController, uiContext, formDialogPresenter , new JcrNodeAdapter(node));

        Version version = versionManager.addVersion(node);

        List<VersionInfo> versionInfos = action.getAvailableVersionInfoList();
        VersionInfo versionInfo = versionInfos.get(0);

        // WHEN
        String versionLabel = action.getVersionLabel(versionInfo);

        // THEN
        assertTrue(versionLabel.contains(version.getName()));
    }

    @Test(expected = ActionExecutionException.class)
    public void testNonVersionedNode() throws RepositoryException, ActionExecutionException {
        // GIVEN
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node node = webSiteSession.getRootNode().addNode("testNoVersion", NodeTypes.Page.NAME);

        TestAbstractVersionAction action = new TestAbstractVersionAction(definition, locationController, uiContext, formDialogPresenter , new JcrNodeAdapter(node));

        // WHEN
        action.getAvailableVersionInfoList();

        // THEN
        // Exception
    }

    /**
     * Test implementation for {@link info.magnolia.ui.api.location.LocationController}.
     */
    public static class TestLocationController extends LocationController {
        public Location where;

        public TestLocationController(EventBus eventBus, Shell shell) {
            super(eventBus, shell);
        }

        @Override
        public void goTo(final Location newLocation) {
            where = newLocation;
        }

        @Override
        public Location getWhere() {
            return where;
        }
    }

    /**
     * Action definition for {@link TestAbstractVersionAction}.
     */
    private class TestAbstractVersionActionDefinition extends ConfiguredActionDefinition {
        public TestAbstractVersionActionDefinition() {
            setImplementationClass(TestAbstractVersionAction.class);
        }
    }

    /**
     * Test action extending {@link AbstractVersionAction}.
     */
    private class TestAbstractVersionAction extends AbstractVersionAction<TestAbstractVersionActionDefinition> {

        protected TestAbstractVersionAction(TestAbstractVersionActionDefinition definition, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter) {
            super(definition, locationController, uiContext, formDialogPresenter, nodeAdapter);
        }

        @Override
        protected Class getBeanItemClass() {
            return SimpleBean.class;
        }

        @Override
        protected FormDialogDefinition buildNewComponentDialog() throws ActionExecutionException, RepositoryException {
            ConfiguredFormDefinition form = new ConfiguredFormDefinition();
            ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
            tab.setName("test");

            SelectFieldDefinition selectFrom = new SelectFieldDefinition();
            selectFrom.setName("property");

            // All versions
            for (VersionInfo versionInfo : getAvailableVersionInfoList()) {
                SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                option.setValue(versionInfo.getVersionName());
                option.setLabel(getVersionLabel(versionInfo));
                selectFrom.addOption(option);
            }

            tab.addField(selectFrom);

            ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
            dialog.setId("test");
            dialog.setForm(form);

            CallbackDialogActionDefinition callbackAction = new CallbackDialogActionDefinition();
            callbackAction.setName("commit");
            dialog.getActions().put(callbackAction.getName(), callbackAction);

            form.addTab(tab);

            return dialog;
        }

        @Override
        protected Node getNode() throws RepositoryException {
            return nodeAdapter.getJcrItem();
        }

        @Override
        protected Location getLocation() throws ActionExecutionException {
            return new DefaultLocation("test", "testApp");
        }

    }

    /**
     * Simple bean for test.
     */
    private class SimpleBean {
        private String property;

        private String getProperty() {
            return property;
        }

        private void setProperty(String property) {
            this.property = property;
        }
    }

}
