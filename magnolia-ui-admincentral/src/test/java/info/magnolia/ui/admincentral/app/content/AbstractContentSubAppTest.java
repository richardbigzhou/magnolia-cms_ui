/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.app.content;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;

/**
 * AbstractContentSubAppTest.
 */
public class AbstractContentSubAppTest {

    private static final String DUMMY_APPNAME = "dummy";
    private static final String path = "/foo/bar";
    private static final String query = "qux*";

    private AppContext appContext;
    private ContentAppView view;
    private ContentWorkbenchPresenter workbench;
    private EventBus subAppEventBus;

    @Before
    public void setUp() throws Exception {
        appContext = mock(AppContext.class);
        view = mock(ContentAppView.class);
        workbench = mock(ContentWorkbenchPresenter.class);

        ActionbarPresenter actionbar = new ActionbarPresenter(subAppEventBus, appContext);
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        subAppEventBus = mock(EventBus.class);
    }

    private class DummyContentSubApp extends AbstractContentSubApp {
        public int foo = 0;

        public DummyContentSubApp(AppContext appContext, ContentAppView view, ContentWorkbenchPresenter workbench, EventBus subAppEventBus) {
            super(appContext, view, workbench, subAppEventBus);
        }

        @Override
        public String getCaption() {
            return DUMMY_APPNAME;
        }

        @Override
        protected void onSubAppStart() {
            foo++;
        }
    }

    @Test
    public void testParseLocationToken() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.LIST.getText());

        //WHEN
        List<String> tokens = AbstractContentSubApp.parseLocationToken(location);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID, tokens.get(0));
        assertEquals(path, tokens.get(1));
        assertEquals(ViewType.LIST.getText(), tokens.get(2));

    }

    @Test
    public void testParseLocationTokenSearchViewNoQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.SEARCH.getText());

        //WHEN
        List<String> tokens = AbstractContentSubApp.parseLocationToken(location);

        //THEN
        assertEquals(ViewType.SEARCH.getText(), tokens.get(2));

    }

    @Test
    public void testParseLocationTokenSearchViewWithQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.SEARCH.getText() + ";" + query);

        //WHEN
        List<String> tokens = AbstractContentSubApp.parseLocationToken(location);

        //THEN
        assertEquals(ViewType.SEARCH.getText()+";"+query, tokens.get(2));

    }

    @Test
    public void testParseLocationTokenWithAdditionalElements() throws Exception {
        //GIVEN
        String additionalElem1 = "baz";
        String additionalElem2 = "qux;blah";
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.LIST.getText() + ":" + additionalElem1 + ":" + additionalElem2);

        //WHEN
        List<String> tokens = AbstractContentSubApp.parseLocationToken(location);

        //THEN
        assertEquals(5, tokens.size());
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID, tokens.get(0));
        assertEquals(path, tokens.get(1));
        assertEquals(ViewType.LIST.getText(), tokens.get(2));
        assertEquals(additionalElem1, tokens.get(3));
        assertEquals(additionalElem2, tokens.get(4));

    }

    @Test
    public void testGetQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.SEARCH.getText() + ";" + query);

        //WHEN
        String retVal = AbstractContentSubApp.getQuery(location);

        //THEN
        assertEquals(query, retVal);

    }

    @Test
    public void testGetQueryReturnsNullIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.SEARCH.getText());

        //WHEN
        String retVal = AbstractContentSubApp.getQuery(location);

        //THEN
        assertNull(retVal);
    }

    @Test
    public void testGetQueryReturnsNullIfQueryIsPresentButViewIsNotSearch() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.LIST.getText() + ";" + query);

        //WHEN
        String retVal = AbstractContentSubApp.getQuery(location);

        //THEN
        assertNull(retVal);
    }

    @Test
    public void testGetSelectedItemPath() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.TREE.getText());

        //WHEN
        String retVal = AbstractContentSubApp.getSelectedItemPath(location);

        //THEN
        assertEquals(path, retVal);
    }

    @Test
    public void testGetSelectedItemPathReturnsRootIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID);

        //WHEN
        String retVal = AbstractContentSubApp.getSelectedItemPath(location);

        //THEN
        assertEquals("/", retVal);
    }

    @Test
    public void testGetSubAppId() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.TREE.getText());

        //WHEN
        String retVal = AbstractContentSubApp.getSubAppId(location);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID , retVal);
    }

    @Test
    public void testGetSelectedView() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID + ":" + path + ":" + ViewType.THUMBNAIL.getText());

        //WHEN
        ViewType retVal = AbstractContentSubApp.getSelectedView(location);

        //THEN
        assertEquals(ViewType.THUMBNAIL, retVal);
    }

    @Test
    public void testGetSelectedViewReturnsTreeViewIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID);

        //WHEN
        ViewType retVal = AbstractContentSubApp.getSelectedView(location);

        //THEN
        assertEquals(ViewType.TREE, retVal);
    }

    @Test
    public void testSubAppSupportsLocationReturnsFalse() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, "foo");

        //WHEN
        boolean retVal = AbstractContentSubApp.supportsLocation(location);

        //THEN
        assertFalse(retVal);
    }

    @Test
    public void testSubAppSupportsLocationReturnsTrue() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, AbstractContentSubApp.MAIN_SUBAPP_ID);

        //WHEN
        boolean retVal = AbstractContentSubApp.supportsLocation(location);

        //THEN
        assertTrue(retVal);
    }

    @Test
    public void testCreateLocation() throws Exception {
        //GIVEN

        //WHEN
        DefaultLocation location = AbstractContentSubApp.createLocation();

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID, AbstractContentSubApp.getSubAppId(location));
        assertEquals("/", AbstractContentSubApp.getSelectedItemPath(location));
        assertEquals(ViewType.TREE, AbstractContentSubApp.getSelectedView(location));
    }

    @Test
    public void testOnSubAppStartIsCalled() throws Exception {
        //GIVEN see DummyContentSubApp impl of onSubAppStart()
        DummyContentSubApp subApp = new DummyContentSubApp(appContext, view, workbench, subAppEventBus);
        assertTrue(subApp.foo == 0);

        //WHEN
        subApp.start(AbstractContentSubApp.createLocation());

        //THEN
        assertTrue(subApp.foo == 1);
    }

    @Test
    public void testReplaceLocationSubstitutesQueryWithWildcard() throws Exception {
        //GIVEN
        String token = AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search;foo*";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = AbstractContentSubApp.replaceLocationToken(currentLocation, "baz", AbstractContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search;baz", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesQueryNoWildcard() throws Exception {
        //GIVEN
        String token = AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search;foo";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = AbstractContentSubApp.replaceLocationToken(currentLocation, "baz", AbstractContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search;baz", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesExistingQueryWithAnEmptyOne() throws Exception {
        //GIVEN
        String token = AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search;foo";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = AbstractContentSubApp.replaceLocationToken(currentLocation, "", AbstractContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:search", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesPath() throws Exception {
        //GIVEN
        String token = AbstractContentSubApp.MAIN_SUBAPP_ID + ":/:tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = AbstractContentSubApp.replaceLocationToken(currentLocation, "/baz/qux", AbstractContentSubApp.TokenElementType.PATH);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID + ":/baz/qux:tree", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesViewType() throws Exception {
        //GIVEN
        String token = AbstractContentSubApp.MAIN_SUBAPP_ID + ":/baz/qux:tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = AbstractContentSubApp.replaceLocationToken(currentLocation, "list", AbstractContentSubApp.TokenElementType.VIEW);

        //THEN
        assertEquals(AbstractContentSubApp.MAIN_SUBAPP_ID + ":/baz/qux:list", newToken);
    }

}
