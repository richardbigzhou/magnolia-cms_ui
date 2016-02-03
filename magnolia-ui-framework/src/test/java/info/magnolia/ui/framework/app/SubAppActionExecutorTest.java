/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.app.SubAppContext;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for SubAppActionExecutor.
 */
public class SubAppActionExecutorTest extends MgnlTestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
    }

    @Test
    public void testCanResolveActionFromSubAppDescriptor() {

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");

        ConfiguredSubAppDescriptor subAppDescriptor = new ConfiguredSubAppDescriptor();
        subAppDescriptor.getActions().put(actionDefinition.getName(), actionDefinition);

        SubAppContext subAppContext = mock(SubAppContext.class);
        when(subAppContext.getSubAppDescriptor()).thenReturn(subAppDescriptor);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(new ComponentProviderConfiguration());
        GuiceComponentProvider componentProvider = builder.build();
        SubAppActionExecutor actionExecutor = new SubAppActionExecutor(componentProvider, subAppContext);

        // WHEN
        ActionDefinition returnedActionDefinition = actionExecutor.getActionDefinition("foobar");

        // THEN
        assertNotNull(returnedActionDefinition);
        assertSame(actionDefinition, returnedActionDefinition);
    }
}
