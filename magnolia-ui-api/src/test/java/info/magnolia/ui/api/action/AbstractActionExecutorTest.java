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
package info.magnolia.ui.api.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Item;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for AbstractActionExecutor.
 */
public class AbstractActionExecutorTest extends MgnlTestCase {

    private static final Item[] ROOT_ITEM = new Item[] { null };

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        ArrayList<String> roles = new ArrayList<String>();
        roles.add("testRole");

        MgnlUser user = new MgnlUser("testUser", null, new ArrayList<String>(), roles, new HashMap<String, String>()) {

            // Overridden to avoid querying the group manager in test
            @Override
            public Collection<String> getAllRoles() {
                return super.getRoles();
            }
        };

        MockWebContext context = (MockWebContext) MgnlContext.getInstance();
        context.setUser(user);
    }

    @Test
    public void testCreatesAndExecutesAction() throws ActionExecutionException {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionDefinition.setImplementationClass(ActionThatCompletesNormally.class);
        actionExecutor.add(actionDefinition);

        // WHEN
        actionExecutor.execute("foobar");

        // THEN
        assertTrue(ActionThatCompletesNormally.executed);
    }

    @Test
    public void testCreateActionThrowsExceptionWhenActionDefinitionMissing() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        // WHEN
        try {
            actionExecutor.createAction("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            // THEN
            assertEquals("No definition exists for action: foobar", e.getMessage());
        }
    }

    @Test
    public void testCreateActionThrowsExceptionWhenActionDefinitionLacksActionClass() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionExecutor.add(actionDefinition);

        // WHEN
        try {
            actionExecutor.createAction("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            // THEN
            assertEquals("No action class set for action: foobar", e.getMessage());
        }
    }

    @Test
    public void testThrowsExceptionWhenActionThrowsRuntimeException() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionDefinition.setImplementationClass(ActionThatThrowsRuntimeException.class);
        actionExecutor.add(actionDefinition);

        // WHEN
        try {
            actionExecutor.execute("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            // THEN
            assertEquals("Action execution failed for action: foobar", e.getMessage());
            assertEquals("ActionThatThrowsRuntimeException", e.getCause().getMessage());
        }
    }

    @Test
    public void testThrowsExceptionWhenActionThrowsActionExecutionException() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionDefinition.setImplementationClass(ActionThatThrowsActionExecutionException.class);
        actionExecutor.add(actionDefinition);

        // WHEN
        try {
            actionExecutor.execute("foobar");
            fail("Expected ActionExecutionException");
        } catch (ActionExecutionException e) {
            // THEN
            assertEquals("ActionThatThrowsActionExecutionException", e.getMessage());
        }
    }

    public static class ActionThatCompletesNormally implements Action {

        public static boolean executed = false;

        @Override
        public void execute() throws ActionExecutionException {
            executed = true;
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

    @Test
    public void missingActionIsNotAvailable() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        // THEN
        assertFalse(actionExecutor.isAvailable("foobar", ROOT_ITEM));
        assertFalse(actionExecutor.isAvailable("foobar", new MockNode()));
        assertFalse(actionExecutor.isAvailable("foobar", new MockProperty("propertyName", "propertyValue", new MockNode())));
    }

    @Test
    public void actionIsAvailableIfRootEnabledAndGivenNull() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        ConfiguredAvailabilityDefinition restrictions = (ConfiguredAvailabilityDefinition) actionDefinition.getAvailability();
        restrictions.setAccess(new ConfiguredAccessDefinition());
        restrictions.setRoot(true);
        actionExecutor.add(actionDefinition);

        // THEN
        assertTrue(actionExecutor.isAvailable("foobar", ROOT_ITEM));
        assertTrue(actionExecutor.isAvailable("foobar", new MockNode()));
        assertFalse(actionExecutor.isAvailable("foobar", new MockProperty("propertyName", "propertyValue", new MockNode())));
    }

    @Test
    public void actionIsAvailableIfPropertiesEnabledAndGivenProperty() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        ConfiguredAvailabilityDefinition restrictions = (ConfiguredAvailabilityDefinition) actionDefinition.getAvailability();
        restrictions.setProperties(true);
        restrictions.setAccess(new ConfiguredAccessDefinition());
        actionExecutor.add(actionDefinition);

        // THEN
        assertFalse(actionExecutor.isAvailable("foobar", ROOT_ITEM));
        assertTrue(actionExecutor.isAvailable("foobar", new MockProperty("propertyName", "propertyValue", new MockNode())));
        assertTrue(actionExecutor.isAvailable("foobar", new MockNode()));
    }

    @Test
    public void actionIsAvailableWhenNoNodeTypesAreConfigured() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        ((ConfiguredAvailabilityDefinition) actionDefinition.getAvailability()).setAccess(new ConfiguredAccessDefinition());
        actionExecutor.add(actionDefinition);

        // THEN
        assertFalse(actionExecutor.isAvailable("foobar", ROOT_ITEM));
        assertFalse(actionExecutor.isAvailable("foobar", new MockProperty("propertyName", "propertyValue", new MockNode())));
        assertTrue(actionExecutor.isAvailable("foobar", new MockNode()));
    }

    @Test
    public void actionIsAvailableOnlyForTheConfiguredNodeTypes() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        ConfiguredAvailabilityDefinition restrictions = (ConfiguredAvailabilityDefinition) actionDefinition.getAvailability();
        restrictions.getNodeTypes().add(NodeTypes.Content.NAME);
        restrictions.setAccess(new ConfiguredAccessDefinition());
        actionExecutor.add(actionDefinition);

        // THEN
        assertFalse(actionExecutor.isAvailable("foobar", ROOT_ITEM));
        assertFalse(actionExecutor.isAvailable("foobar", new MockProperty("propertyName", "propertyValue", new MockNode())));
        assertTrue(actionExecutor.isAvailable("foobar", new MockNode("", NodeTypes.Content.NAME)));
        assertFalse(actionExecutor.isAvailable("foobar", new MockNode("", NodeTypes.ContentNode.NAME)));
    }

    @Test
    public void actionIsAvailableOnlyForTheConfiguredRoles() {

        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("requiresTestRole");
        ConfiguredAvailabilityDefinition availability = (ConfiguredAvailabilityDefinition) actionDefinition.getAvailability();
        availability.setRoot(true);
        ConfiguredAccessDefinition access = new ConfiguredAccessDefinition();
        access.addRole("testRole");
        availability.setAccess(access);
        actionExecutor.add(actionDefinition);

        ConfiguredActionDefinition actionDefinition2 = new ConfiguredActionDefinition();
        actionDefinition2.setName("requiresTestRole2");
        availability = (ConfiguredAvailabilityDefinition) actionDefinition2.getAvailability();
        availability.setRoot(true);
        access = new ConfiguredAccessDefinition();
        access.addRole("testRole2");
        availability.setAccess(access);
        actionExecutor.add(actionDefinition2);

        // THEN
        assertTrue(actionExecutor.isAvailable("requiresTestRole", ROOT_ITEM));
        assertFalse(actionExecutor.isAvailable("requiresTestRole2", ROOT_ITEM));
    }

    @Test
    public void actionIsNotAvailableForMultipleItemsByDefault() {
        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        // WHEN
        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        actionExecutor.add(actionDefinition);

        // THEN
        assertFalse(actionExecutor.isAvailable("foobar", new MockNode("a", NodeTypes.Content.NAME), new MockNode("b", NodeTypes.Content.NAME)));
    }

    @Test
    public void actionIsAvailableForMultipleItemsWhenExplicitlySet() {
        // GIVEN
        SimpleActionExecutor actionExecutor = createSimpleActionExecutor();

        // WHEN
        ConfiguredActionDefinition actionDefinition = new ConfiguredActionDefinition();
        actionDefinition.setName("foobar");
        ConfiguredAvailabilityDefinition availability = (ConfiguredAvailabilityDefinition) actionDefinition.getAvailability();
        availability.setMultiple(true);
        actionExecutor.add(actionDefinition);

        // THEN
        assertTrue(actionExecutor.isAvailable("foobar", new MockNode("a", NodeTypes.Content.NAME), new MockNode("b", NodeTypes.Content.NAME)));
    }

    private SimpleActionExecutor createSimpleActionExecutor() {
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        ComponentProviderConfiguration componentProviderConfig = new ComponentProviderConfiguration();
        componentProviderConfig.registerInstance(I18nizer.class, mock(I18nizer.class));
        builder.withConfiguration(componentProviderConfig);
        GuiceComponentProvider componentProvider = builder.build();
        return new SimpleActionExecutor(componentProvider);
    }
}
