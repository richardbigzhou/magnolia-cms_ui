/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link OpenLocationAction}.
 */
public class OpenLocationActionTest {

    private OpenLocationAction action;
    private OpenLocationActionDefinition definition;
    private LocationController locationController;
    private MockSession session;

    @Before
    public void setUp() throws RepositoryException {
        definition = new OpenLocationActionDefinition();
        locationController = mock(LocationController.class);
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockWebContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExecuteWithShellApp() throws Exception {
        // GIVEN
        definition.setAppName("appLauncher");
        definition.setAppType(Location.LOCATION_TYPE_SHELL_APP);
        definition.setParameter("someParameter");
        action = new OpenLocationAction(definition, locationController);

        // WHEN
        action.execute();

        // THEN
        ArgumentCaptor<Location> argument = ArgumentCaptor.forClass(Location.class);
        verify(locationController).goTo(argument.capture());
        Location argumentValue = argument.getValue();
        assertThat(argumentValue.getAppName(), equalTo("appLauncher"));
        assertThat(argumentValue.getAppType(), equalTo(Location.LOCATION_TYPE_SHELL_APP));
        assertThat(argumentValue.getSubAppId(), equalTo(StringUtils.EMPTY));
        assertThat(argumentValue.getParameter(), equalTo("someParameter"));
    }

    @Test
    public void testExecuteWithApp() throws Exception {
        // GIVEN
        definition.setAppType(Location.LOCATION_TYPE_APP);
        definition.setAppName("pages");
        definition.setSubAppId("browser");
        definition.setParameter("/demo-project/about:edit");
        action = new OpenLocationAction(definition, locationController);

        // WHEN
        action.execute();

        // THEN
        ArgumentCaptor<Location> argument = ArgumentCaptor.forClass(Location.class);
        verify(locationController).goTo(argument.capture());
        Location argumentValue = argument.getValue();
        assertThat(argumentValue.getAppName(), equalTo("pages"));
        assertThat(argumentValue.getAppType(), equalTo(Location.LOCATION_TYPE_APP));
        assertThat(argumentValue.getSubAppId(), equalTo("browser"));
        assertThat(argumentValue.getParameter(), equalTo("/demo-project/about:edit"));
    }
}
