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
package info.magnolia.ui.framework.app;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.ui.framework.view.View;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AbstractSubApp}.
 */
public class AbstractSubAppTest {

    private static final String APP_LABEL = "App label";
    private static final String SUBAPP_LABEL = "SubApp label";
    private AbstractSubApp subApp;
    private SubAppDescriptor subAppDescriptor;
    private AppDescriptor appDescriptor;

    @Before
    public void setUp() throws Exception {
        //INIT
        appDescriptor = mock(AppDescriptor.class);
        when(appDescriptor.getLabel()).thenReturn(APP_LABEL);
        when(appDescriptor.getName()).thenReturn("App1");

        subAppDescriptor = mock(SubAppDescriptor.class);
        when(subAppDescriptor.getName()).thenReturn("SubApp1");

        AppContext appContext = mock(AppContext.class);
        when(appContext.getAppDescriptor()).thenReturn(appDescriptor);

        SubAppContext subAppContext = mock(SubAppContext.class);
        when(subAppContext.getAppContext()).thenReturn(appContext);
        when(subAppContext.getSubAppDescriptor()).thenReturn(subAppDescriptor);

        View view = mock(View.class);

        subApp = new DummySubApp(subAppContext, view);
    }

    @Test
    public void testGetCaptionReturnsConfiguredSubAppLabelValue() throws Exception {
        //GIVEN
        when(subAppDescriptor.getLabel()).thenReturn(SUBAPP_LABEL);

        //WHEN
        String caption = subApp.getCaption();

        //THEN
        assertEquals(SUBAPP_LABEL, caption);
    }

    @Test
    public void testGetCaptionFallsBackToConfiguredAppLabelValue() throws Exception {
        //GIVEN
        when(subAppDescriptor.getLabel()).thenReturn("");

        //WHEN
        String caption = subApp.getCaption();

        //THEN
        assertEquals(APP_LABEL, caption);
    }

    @Test
    public void testGetCaptionReturnsEmptyStringIfNoConfiguredLabelValueIsFound() throws Exception {
        //GIVEN
        when(subAppDescriptor.getLabel()).thenReturn("");
        when(appDescriptor.getLabel()).thenReturn("");

        //WHEN
        String caption = subApp.getCaption();

        //THEN
        assertEquals("", caption);
    }

    private class DummySubApp extends AbstractSubApp {

        public DummySubApp(SubAppContext subAppContext, View view) {
            super(subAppContext, view);
        }

    }

}
