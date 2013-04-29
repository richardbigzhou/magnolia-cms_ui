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
package info.magnolia.ui.api.action;

import static org.junit.Assert.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Test case for AbstractActionExecutor.
 */
public class AbstractActionExecutorTest {

    @Test
    public void testCreateActionThrowsExceptionWhenActionDefinitionMissing() {

        AbstractActionExecutor abstractActionExecutor = new AbstractActionExecutor(null) {
            @Override
            public ActionDefinition getActionDefinition(String actionName) {
                return null;
            }
        };

        //WHEN
        try {
            abstractActionExecutor.createAction("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            //THEN
            assertEquals("No definition exists for action: foobar", e.getMessage());
        }
    }

    @Test
    public void testCreateActionThrowsExceptionWhenActionDefinitionLacksActionClass() {

        AbstractActionExecutor abstractActionExecutor = new AbstractActionExecutor(null) {
            @Override
            public ActionDefinition getActionDefinition(String actionName) {
                return new CommandActionDefinition();
            }
        };

        //WHEN
        try {
            abstractActionExecutor.createAction("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            //THEN
            assertEquals("No action class set for action: foobar", e.getMessage());
        }
    }

    @Test
    public void testThrowsExceptionWhenActionThrowsRuntimeException() {

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(new ComponentProviderConfiguration());
        GuiceComponentProvider componentProvider = builder.build();
        SimpleActionExecutor actionExecutor = new SimpleActionExecutor(componentProvider);

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionDefinition.setImplementationClass(ActionThatThrowsRuntimeException.class);
        actionExecutor.add(actionDefinition);

        //WHEN
        try {
            actionExecutor.execute("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            //THEN
            assertEquals("Action execution failed for action: foobar", e.getMessage());
            assertEquals("ActionThatThrowsRuntimeException", e.getCause().getMessage());
        }
    }

    @Test
    public void testThrowsExceptionWhenActionThrowsActionExecutionException() {

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(new ComponentProviderConfiguration());
        GuiceComponentProvider componentProvider = builder.build();
        SimpleActionExecutor actionExecutor = new SimpleActionExecutor(componentProvider);

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionDefinition.setImplementationClass(ActionThatThrowsActionExecutionException.class);
        actionExecutor.add(actionDefinition);

        //WHEN
        try {
            actionExecutor.execute("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            //THEN
            assertEquals("ActionThatThrowsActionExecutionException", e.getMessage());
        }
    }

    public static class ActionThatThrowsRuntimeException implements Action {

        @Override
        public void execute() throws ActionExecutionException {
            throw new NullPointerException("ActionThatThrowsRuntimeException");
        }
    }

    public static class ActionThatThrowsActionExecutionException implements Action {

        @Override
        public void execute() throws ActionExecutionException {
            throw new ActionExecutionException("ActionThatThrowsActionExecutionException");
        }
    }

    private static class SimpleActionExecutor extends AbstractActionExecutor {

        private List<ActionDefinition> definitions = new ArrayList<ActionDefinition>();

        @Inject
        public SimpleActionExecutor(ComponentProvider componentProvider) {
            super(componentProvider);
        }

        public boolean add(ActionDefinition actionDefinition) {
            return definitions.add(actionDefinition);
        }

        @Override
        public ActionDefinition getActionDefinition(String actionName) {
            for (ActionDefinition definition : definitions) {
                if (definition.getName() != null && definition.getName().equals(actionName))
                    return definition;
            }
            return null;
        }
    }
}

