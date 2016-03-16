/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.form.field;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.ui.AbstractTextField;

/**
 * An abstract JCR-to-Vaadin integration test for Magnolia fields, field factories and transformers.
 */
public class FieldIntegrationTestCase {
    protected Session session;
    protected ComponentProvider componentProvider;
    protected I18NAuthoringSupport i18nAuthoringSupport;
    protected MockFieldFactoryFactory fieldFactoryFactory;
    protected UiContext uiContext;

    @Before
    public void setUp() throws Exception {
        session = new MockSession("website");
        MockContext ctx = new MockContext();
        ctx.addSession("website", session);
        MgnlContext.setInstance(ctx);

        componentProvider = Components.getComponentProvider();
        i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        ComponentsTestUtil.setInstance(I18NAuthoringSupport.class, i18nAuthoringSupport);
        uiContext = mock(UiContext.class);
        ComponentsTestUtil.setInstance(UiContext.class, uiContext);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        mockMessagesManager();

        fieldFactoryFactory = new MockFieldFactoryFactory(componentProvider);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    /**
     * Simulates receiving a text change from the client, via #changeVariables instead of #setValue (performs additional null-conversion)
     */
    protected static void enterText(AbstractTextField field, String text) {
        field.changeVariables(null, ImmutableMap.<String, Object>of("text", text));
    }

    /**
     * Registers a fieldType mapping for the fake FieldFactoryFactory. This is only needed for complex fields, i.e. where the fieldFactoryFactory is used to create sub-fields.
     */
    protected void registerFieldType(Class<? extends FieldDefinition> definitionClass, Class<? extends FieldFactory> factoryClass) {
        fieldFactoryFactory.registerFieldType(definitionClass, factoryClass);
    }

    private static void mockMessagesManager() {
        MockMessagesManager messagesManager = mock(MockMessagesManager.class);
        Messages messages = mock(Messages.class);
        doAnswer(new ReturnsArgumentAt(0)).when(messages).get(anyString());
        doReturn(messages).when(messagesManager).getMessagesInternal(anyString(), any(Locale.class));
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
    }

    /**
     * A fake FieldFactoryFactory replacing the field-type registry with fixed definition/factory mappings.
     */
    protected static class MockFieldFactoryFactory extends FieldFactoryFactory {

        private final Map<Class<? extends FieldDefinition>, Class<? extends FieldFactory>> fieldTypes = Maps.newHashMap();
        private final ComponentProvider componentProvider;

        public MockFieldFactoryFactory(ComponentProvider componentProvider) {
            super(null, null, null);
            this.componentProvider = componentProvider;
        }

        public void registerFieldType(Class<? extends FieldDefinition> definitionClass, Class<? extends FieldFactory> factoryClass) {
            fieldTypes.put(definitionClass, factoryClass);
        }

        @Override
        public FieldFactory createFieldFactory(FieldDefinition definition, Object... parameters) {
            if (fieldTypes.containsKey(definition.getClass())) {
                Class<? extends FieldFactory> factoryClass = fieldTypes.get(definition.getClass());
                ArrayList<Object> combinedParameters = Lists.newArrayList(parameters);
                combinedParameters.add(definition);
                combinedParameters.add(componentProvider);
                return componentProvider.newInstance(factoryClass, combinedParameters.toArray());
            }
            return null;
        }
    }

    /**
     * Just exposing #getMessagesInternal() for mocking.
     */
    private abstract static class MockMessagesManager extends MessagesManager {
        @Override
        public abstract Messages getMessagesInternal(String basename, Locale locale);
    }
}
