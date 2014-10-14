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
package info.magnolia.ui.contentapp.browser.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.UI;

/**
 * Tests for {@link DownloadVersionedBinaryAction}.
 */
public class DownloadVersionedBinaryActionTest extends RepositoryTestCase {

    private Session session;
    private AbstractJcrNodeAdapter item;

    private MockShowVersionsAction action;
    private DownloadVersionedBinaryActionDefinition definition = new DownloadVersionedBinaryActionDefinition();

    private Page page;

    private AppContext appContext;
    private LocationController locationController;
    private UiContext uiContext;
    private FormDialogPresenter formDialogPresenter;
    private SimpleTranslator i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appContext = mock(AppContext.class);
        locationController = mock(LocationController.class);
        uiContext = mock(UiContext.class);
        formDialogPresenter = mock(FormDialogPresenter.class);
        i18n = mock(SimpleTranslator.class);

        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    }

    @Test
    public void testVersionedBinaryDownload() throws Exception {
        // GIVEN
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("magnolia.png");
        Node root = session.getRootNode().addNode("test", NodeTypes.ContentNode.NAME);
        Node node = root.addNode(JcrConstants.JCR_CONTENT, NodeTypes.ContentNode.NAME);
        node.setProperty("fileName", "magnolia.png");
        node.setProperty("extension", "png");
        node.setProperty(JcrConstants.JCR_DATA, new BinaryImpl(inputStream));
        session.save();

        VersionManager versionManager = VersionManager.getInstance();
        Version version = versionManager.addVersion(root, new Rule(new String[] {NodeTypes.ContentNode.NAME}));

        item = new JcrNodeAdapter(root);

        action = new MockShowVersionsAction(definition, appContext, locationController, uiContext, formDialogPresenter, item, i18n, versionManager);
        action.setVersionName(version.getName());

        // WHEN
        action.execute();

        // THEN
        verify(page, times(1)).open(any(StreamResource.class), (String) isNull(), eq(false));
    }

    private void setCurrentUI() {
        UI ui = mock(UI.class);
        page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);
        UI.setCurrent(ui);
    }

    /**
     * Sub class of {@link DownloadVersionedBinaryAction} to simplify testing.
     */
    private class MockShowVersionsAction extends DownloadVersionedBinaryAction {

        private String versionName;

        public MockShowVersionsAction(DownloadVersionedBinaryActionDefinition definition, AppContext appContext, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter, SimpleTranslator i18n, VersionManager versionManager) {
            super(definition, appContext, locationController, uiContext, formDialogPresenter, nodeAdapter, i18n, versionManager);
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        @Override
        protected String getVersionName() {
            return versionName;
        }

        @Override
        public void execute() throws ActionExecutionException {
            super.execute();

            setCurrentUI();

            getEditorCallback().onSuccess("success");
        }

    }

}
