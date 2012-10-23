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
    private DummyContentSubApp subApp;

    @Before
    public void setUp() throws Exception {
        appContext = mock(AppContext.class);
        view = mock(ContentAppView.class);
        workbench = mock(ContentWorkbenchPresenter.class);

        ActionbarPresenter actionbar = new ActionbarPresenter(subAppEventBus, appContext);
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        subAppEventBus = mock(EventBus.class);
        this.subApp = new DummyContentSubApp(appContext,view, workbench, subAppEventBus);
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
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.LIST.getText());

        //WHEN
        List<String> tokens = subApp.parseLocationToken(location);

        //THEN
        assertEquals(subApp.getSubAppName(), tokens.get(0));
        assertEquals(path, tokens.get(1));
        assertEquals(ViewType.LIST.getText(), tokens.get(2));

    }

    @Test
    public void testParseLocationTokenSearchViewNoQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName()+ ":" + path + ":" + ViewType.SEARCH.getText());

        //WHEN
        List<String> tokens = subApp.parseLocationToken(location);

        //THEN
        assertEquals(ViewType.SEARCH.getText(), tokens.get(2));

    }

    @Test
    public void testParseLocationTokenSearchViewWithQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.SEARCH.getText() + ";" + query);

        //WHEN
        List<String> tokens = subApp.parseLocationToken(location);

        //THEN
        assertEquals(ViewType.SEARCH.getText()+";"+query, tokens.get(2));

    }

    @Test
    public void testParseLocationTokenWithAdditionalElements() throws Exception {
        //GIVEN
        String additionalElem1 = "baz";
        String additionalElem2 = "qux;blah";
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.LIST.getText() + ":" + additionalElem1 + ":" + additionalElem2);

        //WHEN
        List<String> tokens = subApp.parseLocationToken(location);

        //THEN
        assertEquals(5, tokens.size());
        assertEquals(subApp.getSubAppName(), tokens.get(0));
        assertEquals(path, tokens.get(1));
        assertEquals(ViewType.LIST.getText(), tokens.get(2));
        assertEquals(additionalElem1, tokens.get(3));
        assertEquals(additionalElem2, tokens.get(4));

    }

    @Test
    public void testGetQuery() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.SEARCH.getText() + ";" + query);

        //WHEN
        String retVal = subApp.getQuery(location);

        //THEN
        assertEquals(query, retVal);

    }

    @Test
    public void testGetQueryReturnsNullIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.SEARCH.getText());

        //WHEN
        String retVal = subApp.getQuery(location);

        //THEN
        assertNull(retVal);
    }

    @Test
    public void testGetQueryReturnsNullIfQueryIsPresentButViewIsNotSearch() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.LIST.getText() + ";" + query);

        //WHEN
        String retVal = subApp.getQuery(location);

        //THEN
        assertNull(retVal);
    }

    @Test
    public void testGetSelectedItemPath() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.TREE.getText());

        //WHEN
        String retVal = subApp.getSelectedItemPath(location);

        //THEN
        assertEquals(path, retVal);
    }

    @Test
    public void testGetSelectedItemPathReturnsRootIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName());

        //WHEN
        String retVal = subApp.getSelectedItemPath(location);

        //THEN
        assertEquals("/", retVal);
    }

    @Test
    public void testGetSubAppId() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.TREE.getText());

        //WHEN
        String retVal = subApp.getSubAppId(location);

        //THEN
        assertEquals(subApp.getSubAppName() , retVal);
    }

    @Test
    public void testGetSelectedView() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName() + ":" + path + ":" + ViewType.THUMBNAIL.getText());

        //WHEN
        ViewType retVal = subApp.getSelectedView(location);

        //THEN
        assertEquals(ViewType.THUMBNAIL, retVal);
    }

    @Test
    public void testGetSelectedViewReturnsTreeViewIfNoneIsPresent() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName());

        //WHEN
        ViewType retVal = subApp.getSelectedView(location);

        //THEN
        assertEquals(ViewType.TREE, retVal);
    }

    @Test
    public void testSupportsLocationReturnsFalseIfSubAppIdIsNotMain() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, "foo");

        //WHEN
        boolean retVal = subApp.supportsLocation(location);

        //THEN
        assertFalse(retVal);
    }

    @Test
    public void testSupportsLocationReturnsTrueOnlyIfSubAppIdIsMain() throws Exception {
        //GIVEN
        DefaultLocation location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, subApp.getSubAppName());

        //WHEN
        boolean retVal = subApp.supportsLocation(location);

        //THEN
        assertTrue(retVal);
    }

    @Test
    public void testCreateLocation() throws Exception {
        //GIVEN

        //WHEN
        DefaultLocation location = subApp.createLocation();

        //THEN
        assertEquals(subApp.getSubAppName(), subApp.getSubAppId(location));
        assertEquals("/", subApp.getSelectedItemPath(location));
        assertEquals(ViewType.TREE, subApp.getSelectedView(location));
    }

    @Test
    public void testOnSubAppStartIsCalled() throws Exception {
        //GIVEN see DummyContentSubApp impl of onSubAppStart()
        DummyContentSubApp subApp = new DummyContentSubApp(appContext, view, workbench, subAppEventBus);
        assertTrue(subApp.foo == 0);

        //WHEN
        subApp.start(subApp.createLocation());

        //THEN
        assertTrue(subApp.foo == 1);
    }

    @Test
    public void testReplaceLocationSubstitutesQueryWithWildcard() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":/:search;foo*";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, "baz", DummyContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(subApp.getSubAppName() + ":/:search;baz", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesQueryNoWildcard() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":/:search;foo";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, "baz", DummyContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(subApp.getSubAppName() + ":/:search;baz", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesExistingQueryWithAnEmptyOne() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":/:search;foo";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, "", DummyContentSubApp.TokenElementType.QUERY);

        //THEN
        assertEquals(subApp.getSubAppName() + ":/:search", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesPath() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":/:tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, "/baz/qux", DummyContentSubApp.TokenElementType.PATH);

        //THEN
        assertEquals(subApp.getSubAppName() + ":/baz/qux:tree", newToken);
    }

    @Test
    public void testReplaceLocationSubstitutesViewType() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":/baz/qux:tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, "list", DummyContentSubApp.TokenElementType.VIEW);

        //THEN
        assertEquals(subApp.getSubAppName() + ":/baz/qux:list", newToken);
    }

    @Test
    public void testReplaceLocationWithEmptyPathReturnsCurrentToken() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":"+ path +"tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, null, DummyContentSubApp.TokenElementType.PATH);

        //THEN
        assertEquals(token, newToken);
    }

    @Test
    public void testReplaceLocationWithEmptyViewTypeReturnsCurrentToken() throws Exception {
        //GIVEN
        String token = subApp.getSubAppName() + ":"+ path +"tree";
        DefaultLocation currentLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, DUMMY_APPNAME, token);

        //WHEN
        String newToken = subApp.replaceLocationToken(currentLocation, null, DummyContentSubApp.TokenElementType.VIEW);

        //THEN
        assertEquals(token, newToken);
    }

}
