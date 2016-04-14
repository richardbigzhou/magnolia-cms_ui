/**
 * This file Copyright (c) 2016 Magnolia International
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

import static org.mockito.Mockito.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.TextField;

public class ShowVersionsActionTest extends RepositoryTestCase {

    private ShowVersionsActionDefinition definition;
    private LocationController locationController;
    private ContentConnector contentConnector;
    private VersionManager versionManager;
    private SimpleTranslator i18n;
    private AppContext appContext;
    private UiContext uiContext;
    private Node node;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        contentConnector = mock(ContentConnector.class);
        ComponentsTestUtil.setInstance(ContentConnector.class, contentConnector);

        versionManager = Components.getComponent(VersionManager.class);
        locationController = mock(LocationController.class);
        uiContext = mock(UiContext.class);
        i18n = mock(SimpleTranslator.class);

        appContext = mock(AppContext.class);
        when(appContext.getName()).thenReturn("appName");

        definition = new ShowVersionsActionDefinition();

        Session websiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node firstLevel = websiteSession.getRootNode().addNode("1st", NodeTypes.Content.NAME);
        node = firstLevel.addNode("test", NodeTypes.Page.NAME);
        NodeTypes.Created.set(node);
        node.setProperty("mgnl:template", "home");
        node.addNode("areaSubNode", NodeTypes.Area.NAME);
        node.getNode("areaSubNode").setProperty("content", "version");
        websiteSession.save();
    }

    @Test
    public void showVersionWhenSingleVersionExists() throws Exception {
        // GIVEN
        versionManager.addVersion(node);
        doReturn("/test").when(contentConnector).getItemUrlFragment(anyObject());

        ShowVersionsAction showVersionsAction = new ShowVersionsAction(definition, appContext, locationController, uiContext, mockFormDialogPresenter("1.0"), new JcrNodeAdapter(node), i18n, contentConnector);

        // WHEN
        showVersionsAction.execute();

        // THEN
        // Make sure that the action use a ContentConnector to get the location and open it in detail sub-app.
        verify(locationController).goTo(eq(new DetailLocation("appName", "detail", DetailView.ViewType.VIEW, "/test", "1.0")));
    }

    @Test
    public void showVersionsWhenMultipleVersionsExist() throws Exception {
        // GIVEN
        versionManager.addVersion(node);
        versionManager.addVersion(node);
        versionManager.addVersion(node);
        doReturn("/test").when(contentConnector).getItemUrlFragment(anyObject());

        ShowVersionsAction showVersionsAction = new ShowVersionsAction(definition, appContext, locationController, uiContext, mockFormDialogPresenter("2.0"), new JcrNodeAdapter(node), i18n, contentConnector);

        // WHEN
        showVersionsAction.execute();

        // THEN
        // Make sure that the action use a ContentConnector to get the location and open it in detail sub-app with specified version.
        verify(locationController).goTo(eq(new DetailLocation("appName", "detail", DetailView.ViewType.VIEW, "/test", "2.0")));
    }

    private FormDialogPresenter mockFormDialogPresenter(final String versionName) {
        FormDialogPresenter formDialogPresenter = mock(FormDialogPresenter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                final Object[] args = inv.getArguments();
                final EditorCallback callback = (EditorCallback) args[3];
                final Item item = (Item) args[0];
                Property property = new TextField();
                property.setValue(versionName);
                item.addItemProperty(ShowVersionsAction.VersionName.PROPERTY_NAME_VERSION_NAME, property);
                callback.onSuccess("commit");
                return null;
            }
        }).when(formDialogPresenter).start(any(Item.class), any(FormDialogDefinition.class), any(UiContext.class), any(EditorCallback.class), any(ContentConnector.class));
        return formDialogPresenter;
    }
}