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
package info.magnolia.ui.model.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;

import org.junit.Test;

/**
 * Test case for {@link AbstractActionFactory}.
 */
public class AbstractActionFactoryTest {

    public static class TestActionDefinition implements ActionDefinition {
        @Override
        public Class<? extends Action> getImplementationClass() {
            return TestAction.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public String getI18nBasename() {
            return null;
        }

        @Override
        public String getIcon() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public static class TestAction extends ActionBase<ActionDefinition> {

        public TestAction(ActionDefinition definition) {
            super(definition);
        }

        @Override
        public void execute() throws ActionExecutionException {
        }
    }

    @Test
    public void testCreateAction() {
        ActionExecutor executor = new ActionExecutor() {
            @Override
            public void execute(String actionName, Object... args) throws ActionExecutionException {
                
            }
        };
        // GIVEN
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        TestAction mockAction = mock(TestAction.class);
        when(componentProvider.newInstance(same(TestAction.class), any(TestActionDefinition.class))).thenReturn(mockAction);

        /*AbstractActionFactory<ActionDefinition, Action> actionFactory = new AbstractActionFactory<ActionDefinition, Action>(componentProvider) {
        };
        actionFactory.addMapping(TestActionDefinition.class, TestAction.class);

        // WHEN
        Action action = actionFactory.createAction(new TestActionDefinition());

        // THEN
        assertNotNull(action);
        assertSame(mockAction, action);*/
    }
}
