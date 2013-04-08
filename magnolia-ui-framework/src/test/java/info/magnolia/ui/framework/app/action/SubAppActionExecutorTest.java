/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.framework.app.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.model.action.AbstractActionExecutorTest;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * SubAppActionExecutorTest.
 */
public class SubAppActionExecutorTest extends AbstractActionExecutorTest {

    private SubAppContext subAppContext;

    @Override
    @Before
    public void setUp() throws Exception {

        this.subAppContext = mock(SubAppContext.class);
        this.params = new TestActionParameters();

        when(subAppContext.getSubAppDescriptor()).thenReturn(createSubAppDescriptor());

        ComponentProvider componentProvider = initComponentProvider();

        actionExecutor = componentProvider.newInstance(ActionExecutor.class);
    }

    @Test
    public void testActionExecution() throws ActionExecutionException {

        //WHEN
        actionExecutor.execute(ACTION_NAME);

        //THEN
        assertEquals(ACTION_NAME, params.getActionname());
    }

    @Test(expected=ActionExecutionException.class)
    public void testNonExistingAction() throws ActionExecutionException {

        //WHEN
        actionExecutor.execute(ACTION_NAME + "non_existing");

        //THEN
        // exception thrown
    }

    private SubAppDescriptor createSubAppDescriptor() {
        ConfiguredSubAppDescriptor descriptor = new ConfiguredSubAppDescriptor();
        ActionDefinition testActionDefinition = new AbstractActionExecutorTest.TestActionDefinition();

        Map<String, ActionDefinition> actions =  new HashMap<String, ActionDefinition>();
        actions.put(testActionDefinition.getName(), testActionDefinition);

        descriptor.setActions(actions);

        return descriptor;
    }

    @Override
    public GuiceComponentProvider initComponentProvider() {

        ComponentProviderConfiguration components = new ComponentProviderConfiguration();

        components.addTypeMapping(ActionExecutor.class, SubAppActionExecutor.class);

        components.registerInstance(SubAppContext.class, subAppContext);
        components.registerInstance(TestActionParameters.class, params);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();

        builder.withConfiguration(components);
        builder.exposeGlobally();
        return builder.build();
    }

}
