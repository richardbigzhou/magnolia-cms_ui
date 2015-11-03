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
package info.magnolia.about.app;

import static info.magnolia.about.app.AboutPresenter.*;
import static info.magnolia.about.app.AboutView.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.pddescriptor.ProductDescriptorExtractor;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockWebContext;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

/**
 * Testing ability of the presenter to obtain connection info.
 */
public class AboutPresenterTest {

    private static final String TEST_SERVER = "Test Server";

    private AboutView view;
    private MagnoliaConfigurationProperties properties;
    private SimpleTranslator i18n;

    private AboutPresenter presenter;
    private ServerConfiguration serverConfiguration;
    private ProductDescriptorExtractor productDescriptorExtractor;

    @Before
    public void setUp() {
        // mock webcontext for server info
        MockWebContext ctx = new MockWebContext();
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getServerInfo()).thenReturn(TEST_SERVER);
        ctx.setServletContext(servletContext);
        MgnlContext.setInstance(ctx);

        view = mock(AboutView.class);
        serverConfiguration = mock(ServerConfiguration.class);
        properties = mock(MagnoliaConfigurationProperties.class);
        i18n = mock(SimpleTranslator.class);
        productDescriptorExtractor = mock(ProductDescriptorExtractor.class);

        presenter = new AboutPresenter(view, serverConfiguration, properties, i18n, productDescriptorExtractor);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testRepoName() {
        // GIVEN
        when(properties.getProperty("magnolia.repositories.config")).thenReturn("repositories.xml");
        when(properties.getProperty("magnolia.app.rootdir")).thenReturn(new File("target/test-classes").getAbsolutePath());

        // WHEN
        String repoName = presenter.getRepoName();

        // THEN
        assertThat(repoName, is("magnolia"));
    }

    @Test
    public void testConnectionWithAbsolutePathForConfFile() {
        // GIVEN
        // AboutPresenter expects a an absolute path (when it's not starting with WEB-INF)
        String configFilePathAbsPath = getAbsPathOfTestResource("jackrabbit-bundle-derby-search.xml");
        when(properties.getProperty("magnolia.repositories.jackrabbit.config")).thenReturn(configFilePathAbsPath);

        // WHEN
        String[] connection = presenter.getConnectionString();

        // THEN
        assertThat(connection.length, greaterThan(0));
        assertThat(connection[0], is("jdbc:derby:${rep.home}/version/db;create=true"));
    }

    @Test
    public void testConnectionWithRelativePathForConfFile() {
        // GIVEN
        // AboutPresenter expects a relative path starting with "WEB-INF" (or an absolute path)
        String configFileRelPath = "WEB-INF/jackrabbit-bundle-derby-search.xml";
        when(properties.getProperty("magnolia.repositories.jackrabbit.config")).thenReturn(configFileRelPath);
        String fakedMagnoliaAppRootDir = getAbsPathOfTestResource(".");
        when(properties.getProperty("magnolia.app.rootdir")).thenReturn(fakedMagnoliaAppRootDir);

        // WHEN
        String[] connection = presenter.getConnectionString();

        // THEN
        assertThat(connection.length, greaterThan(0));
        assertThat(connection[0], is("jdbc:derby:${rep.home}/version/db;create=true"));
    }

    @Test
    public void startPopulatesViewWithDataSourceAndDoesntAddNullProperties() throws Exception {
        // GIVEN
        FakeAboutView fakeView = new FakeAboutView();
        when(serverConfiguration.isAdmin()).thenReturn(true);
        // stub to return requested key instead of null
        doAnswer(new ReturnsArgumentAt(0)).when(i18n).translate(anyString());

        presenter = new AboutPresenter(fakeView, serverConfiguration, properties, i18n, productDescriptorExtractor);

        // WHEN
        AboutView returnedView = presenter.start();

        // THEN
        assertSame(fakeView, returnedView);
        Item dataSource = fakeView.getDataSource();
        assertThat(dataSource, hasVaadinProperty(MAGNOLIA_EDITION_KEY, COMMUNITY_EDITION_I18N_KEY));
        assertThat(dataSource, hasVaadinProperty(MAGNOLIA_INSTANCE_KEY, INSTANCE_AUTHOR_I18N_KEY));
        assertThat(dataSource, hasVaadinProperty(SERVER_INFO_KEY, TEST_SERVER));
        // sample unresolved property, expect not-null
        assertThat(dataSource, hasVaadinProperty(MAGNOLIA_VERSION_KEY, UNKNOWN_PROPERTY_I18N_KEY));
    }

    private static <T> Matcher<Item> hasVaadinProperty(final String propertyName, final T expectedValue) {
        return new TypeSafeMatcher<Item>() {
            @Override
            protected boolean matchesSafely(Item item) {
                Collection<?> propertyIds = item.getItemPropertyIds();
                if (propertyIds.contains(propertyName)) {
                    Property<?> property = item.getItemProperty(propertyName);
                    if (property.getValue().equals(expectedValue)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("an item containing a property with name '%s' and value '%s'.", propertyName, expectedValue));
            }
        };
    }

    private String getAbsPathOfTestResource(String relPathInResources) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(relPathInResources);
        return url.getPath();
    }

    /**
     * Fake AboutView to retrieve the data-source for assertions.
     */
    private static class FakeAboutView implements AboutView {

        private Item dataSource;

        Item getDataSource() {
            return dataSource;
        }

        @Override
        public void setDataSource(Item dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Component asVaadinComponent() {
            return null;
        }
    }

}
