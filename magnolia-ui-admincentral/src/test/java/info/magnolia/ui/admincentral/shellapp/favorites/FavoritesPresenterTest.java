/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.favorites;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static info.magnolia.ui.framework.AdmincentralNodeTypes.Favorite.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;


public class FavoritesPresenterTest {

    private MockSession session;
    private MockWebContext ctx;
    private FavoritesPresenter presenter;

    @Before
    public void setUp() throws Exception {

        ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);

        session = new MockSession(FavoriteStore.WORKSPACE_NAME);
        ctx.addSession(FavoriteStore.WORKSPACE_NAME, session);

        FavoritesView view = mock(FavoritesView.class);

        FavoritesManager favoritesManager = mock(FavoritesManager.class);
        doAnswer(new Answer<JcrNewNodeAdapter>() {

            @Override
            public JcrNewNodeAdapter answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                JcrNewNodeAdapter newFavorite = new JcrNewNodeAdapter(session.getRootNode(), NAME);
                newFavorite.addItemProperty(TITLE, new ObjectProperty<String>((String) args[1]));
                newFavorite.addItemProperty(URL, new ObjectProperty<String>((String) args[0]));
                newFavorite.addItemProperty(ICON, new ObjectProperty<String>(StringUtils.defaultIfEmpty((String) args[2], "icon-app")));
                return newFavorite;
            }
        }).when(favoritesManager).createFavoriteSuggestion(anyString(), anyString(), anyString());

        /**
         * We mock a sample descriptor that would be returned when favorites presenter will ask.
         * We do not set the name/title for sake of testing the i18n functionality.
         */
        AppDescriptorRegistry registry = mock(AppDescriptorRegistry.class);
        ConfiguredAppDescriptor descriptor = new ConfiguredAppDescriptor();
        descriptor.setName("favoritesRandomApp");
        doReturn(descriptor).when(registry).getAppDescriptor(anyString());
        I18nizer i18nizer = mock(I18nizer.class); // simple I18nizer mock which only decorates based on appDescriptor name
        doAnswer(new Answer<AppDescriptor>() {

            @Override
            public AppDescriptor answer(InvocationOnMock invocation) throws Throwable {
                ConfiguredAppDescriptor appDescriptor = (ConfiguredAppDescriptor) invocation.getArguments()[0];
                appDescriptor.setIcon("icon-" + appDescriptor.getName());
                appDescriptor.setLabel(StringUtils.capitalize(appDescriptor.getName()));
                return appDescriptor;
            }
        }).when(i18nizer).decorate(any());

        presenter = new FavoritesPresenter(view, favoritesManager, registry, i18nizer);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        UI.setCurrent(null);
    }

    /**
     * Test that when we determine favorite location from a previous fragment,
     * we do not put the "null" string in the beginning (see http://jira.magnolia-cms.com/browse/MGNLUI-2175).
     */
    @Test
    public void determinePreviousLocationDoesNotContainNull() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        JcrNodeAdapter node = presenter.determinePreviousLocation();
        node.applyChanges();

        // THEN
        assertThat(node.getJcrItem(), hasProperty("title"));
        assertThat(node.getJcrItem().getProperty("title").getString(), not(containsString("null")));
    }

    @Test
    public void getWebAppRootURI() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        final String result = presenter.getWebAppRootURI();

        // THEN
        assertThat(result, equalTo("http://localhost:8080/myWebApp/.magnolia/admincentral"));
    }

    @Test
    public void getCompleteURIFromFragment() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        final String result = presenter.getCompleteURIFromFragment("#app:pages:browser;/:treeview:");

        // THEN
        assertThat("Fragment should have been completed.", result, equalTo("http://localhost:8080/myWebApp/.magnolia/admincentral#app:pages:browser;/:treeview:"));
    }

    @Test
    public void getCompleteURIFromFragmentWithAbsoluteURI() throws URISyntaxException {
        // GIVEN
        initializeVaadinUI();

        final String completeUri = "http://www.magnolia-cms.com/magnolia-cms.html";

        // WHEN
        final String result = presenter.getCompleteURIFromFragment(completeUri);

        // THEN
        assertThat("Complete URIs should be returned unchanged.", result, equalTo(completeUri));
    }

    @Test
    public void getUrlFragmentFrom() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        final String result = presenter.getUrlFragmentFromURI(new URI("http://localhost:8080/myWebApp/.magnolia/admincentral#app:pages:detail;/demo-project/about:edit"));

        // THEN
        assertThat(result, equalTo("#app:pages:detail;/demo-project/about:edit"));
    }

    @Test
    public void testGetUrlFragmentFromHttps() throws Exception {
        // GIVEN
        String webAppUri = "https://localhost/myWebApp/.magnolia/admincentral";
        initializeVaadinUI(new URI(webAppUri));

        String fragment = "#app:pages:detail;/demo-project/about:edit;";

        // WHEN
        final String result = presenter.getUrlFragmentFromURI(new URI(webAppUri + fragment));

        // THEN
        assertThat(result, equalTo(fragment));
    }

    @Test
    public void getUrlFragmentWithQueryParameter() throws Exception {
        // GIVEN
        String webAppUri = "https://localhost/myWebApp/.magnolia/admincentral?restartApplication";
        initializeVaadinUI(new URI(webAppUri));

        String fragment = "#app:pages:detail;/demo-project/about:edit;";

        // WHEN
        final String result = presenter.getUrlFragmentFromURI(new URI(webAppUri + fragment));

        // THEN
        assertThat(result, equalTo(fragment));
    }

    @Test
    public void getCompleteUriFromFragmentWithInitialQueryParameter() throws Exception {
        // GIVEN
        String webAppUri = "https://localhost:443/myWebApp/.magnolia/admincentral?restartApplication";
        initializeVaadinUI(new URI(webAppUri));

        String fragment = "#app:pages:detail;/demo-project/about:edit;";

        // WHEN
        final String result = presenter.getCompleteURIFromFragment(fragment);

        // THEN
        assertThat(result, equalTo(webAppUri + fragment));
    }

    private void initializeVaadinUI() throws URISyntaxException {
        initializeVaadinUI(new URI("http://localhost:8080/myWebApp/.magnolia/admincentral#shell:applauncher:;"));
    }

    private void initializeVaadinUI(final URI returnURI) {
        UI.setCurrent(new UI() {
            @Override
            protected void init(VaadinRequest request) {
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }

            @Override
            public Page getPage() {
                Page page = mock(Page.class);
                doReturn(returnURI).when(page).getLocation();
                return page;
            }
        });
    }

    /**
     * Test translation service.
     */
    public static class TestTranslationService implements TranslationService {

        @Override
        public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
            return "translated with key [" + keys[0] + "] and basename [" + basename + "] and locale [" + localeProvider.getLocale() + "]";
        }

        @Override
        public String translate(LocaleProvider localeProvider, String[] keys) {
            return "translated with key [" + keys[0] + "] and locale [" + localeProvider.getLocale() + "]";
        }

        @Override
        public void reloadMessageBundles() {
        }

    }
}
