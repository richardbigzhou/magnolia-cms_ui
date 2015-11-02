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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Testing ability of the presenter to obtain connection info.
 */
public class AboutPresenterTest {

    private MagnoliaConfigurationProperties properties;
    private AboutPresenter presenter;

    @Before
    public void setUp() {
        properties = mock(MagnoliaConfigurationProperties.class);
        presenter = new AboutPresenter(mock(AboutView.class), mock(ServerConfiguration.class), properties, mock(SimpleTranslator.class));
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

    private String getAbsPathOfTestResource(String relPathInResources) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(relPathInResources);
        return url.getPath();
    }

}
