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
package info.magnolia.ui.framework.app;

import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.framework.app.embedded.EmbeddedPageSubApp;
import info.magnolia.ui.framework.app.embedded.EmbeddedPageSubAppDescriptor;
import info.magnolia.ui.framework.app.embedded.EmbeddedPageView;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link EmbeddedPageSubApp}.
 */
public class EmbeddedPageSubAppTest extends MgnlTestCase {

    private EmbeddedPageSubAppDescriptor subAppDescriptor;
    private EmbeddedPageSubApp subApp;
    private EmbeddedPageView view;
    private String CONTEXT_PATH = "testContext";

    @Before
    public void setUp() throws Exception {
        super.setUp();


        MockWebContext ctx = (MockWebContext) MgnlContext.getInstance();
        ctx.setContextPath(CONTEXT_PATH);

        subAppDescriptor = mock(EmbeddedPageSubAppDescriptor.class);
        when(subAppDescriptor.getUrl()).thenReturn("/path/to/internal");

        SubAppContext subAppContext = mock(SubAppContext.class);

        when(subAppContext.getSubAppDescriptor()).thenReturn(subAppDescriptor);

        view = mock(EmbeddedPageView.class);

        subApp = new EmbeddedPageSubApp(subAppContext, view);
    }

    @Test
    public void testStartOpensFromDescriptor() throws Exception {

        // GIVEN
        String url = "/path/to/internal";
        when(subAppDescriptor.getName()).thenReturn(url);
        DefaultLocation location = new DefaultLocation();

        // WHEN
        subApp.start(location);

        // THEN
        verify(view).setUrl(CONTEXT_PATH + url);
    }

    @Test
    public void testStartOpensFromLocation() throws Exception {

        // GIVEN
        String url = "/path/to/internal2";
        DefaultLocation location = new DefaultLocation();
        location.setParameter(url);

        // WHEN
        subApp.start(location);

        // THEN
        verify(view).setUrl(CONTEXT_PATH + url);
    }

    @Test
    public void testStartOpensFromLocationWhenBothGiven() throws Exception {

        // GIVEN
        String url = "/path/to/internal";
        String url2 = "/path/to/internal2";

        when(subAppDescriptor.getName()).thenReturn(url);

        DefaultLocation location = new DefaultLocation();
        location.setParameter(url2);

        // WHEN
        subApp.start(location);

        // THEN
        verify(view).setUrl(CONTEXT_PATH + url2);
    }


    @Test
    public void testLocationChanged() throws Exception {

        // GIVEN
        String url = "/path/to/internal";
        String url2 = "/path/to/internal2";

        DefaultLocation location = new DefaultLocation();
        location.setParameter(url);
        subApp.start(location);

        // WHEN
        DefaultLocation location2 = new DefaultLocation();
        location2.setParameter(url2);
        subApp.locationChanged(location2);

        // THEN
        verify(view).setUrl(CONTEXT_PATH + url);
        verify(view).setUrl(CONTEXT_PATH + url2);

    }
}
