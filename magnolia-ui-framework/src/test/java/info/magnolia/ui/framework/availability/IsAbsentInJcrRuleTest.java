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
package info.magnolia.ui.framework.availability;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import info.magnolia.resourceloader.classpath.ClasspathOrigin;
import info.magnolia.resourceloader.file.FileSystemOrigin;
import info.magnolia.resourceloader.jcr.JcrOrigin;
import info.magnolia.resourceloader.layered.LayeredResourcePath;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests
 */
public class IsAbsentInJcrRuleTest {

    private IsAbsentInJcrRule rule;
    private LayeredResourcePath resourcePath;
    private ClasspathOrigin classpathOrigin;
    private JcrOrigin jcrOrigin;
    private FileSystemOrigin fileSystemOrigin;

    @Before
    public void setUp() throws Exception {
        rule = new IsAbsentInJcrRule();
        resourcePath = mock(LayeredResourcePath.class);
        classpathOrigin = mock(ClasspathOrigin.class);
        jcrOrigin = mock(JcrOrigin.class);
        fileSystemOrigin = mock(FileSystemOrigin.class);

        given(fileSystemOrigin.getName()).willReturn("filesystem");
        given(classpathOrigin.getName()).willReturn("classpath");
        given(jcrOrigin.getName()).willReturn("jcr");
        given(resourcePath.getFirst()).willReturn(resourcePath);
    }

    @Test
    public void isAvailableForClasspathOrigin() {
        // GIVEN
        given(resourcePath.getFirst().getOrigin()).willReturn(classpathOrigin);

        // WHEN
        boolean availableForItem = rule.isAvailableForItem(resourcePath);

        // THEN
        assertThat(availableForItem, is(true));
    }

    @Test
    public void isAvailableForFileSystemOrigin() {
        // GIVEN
        given(resourcePath.getFirst().getOrigin()).willReturn(fileSystemOrigin);

        // WHEN
        boolean availableForItem = rule.isAvailableForItem(resourcePath);

        // THEN
        assertThat(availableForItem, is(true));
    }

    @Test
    public void isNotAvailableForJcrOrigin() {
        // GIVEN
        given(resourcePath.getFirst().getOrigin()).willReturn(jcrOrigin);

        // WHEN
        boolean availableForItem = rule.isAvailableForItem(resourcePath);

        // THEN
        assertThat(availableForItem, is(false));
    }
}