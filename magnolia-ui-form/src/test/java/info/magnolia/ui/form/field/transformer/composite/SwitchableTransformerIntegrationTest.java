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
package info.magnolia.ui.form.field.transformer.composite;

import static com.google.common.collect.Lists.newArrayList;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class SwitchableTransformerIntegrationTest {

    private Session session;
    private ComponentProvider componentProvider;
    private I18NAuthoringSupport i18nAuthoringSupport;
    private UiContext uiContext;
    private FieldFactoryFactory fieldFactoryFactory;

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

    @Test
    public void writeDiscardsUnselectedOptions() throws Exception {
        // GIVEN
        SwitchableFieldDefinition definition = createSwitchableFieldDefinition("switchable");

        Node node = session.getRootNode().addNode("node");
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        SwitchableFieldFactory<SwitchableFieldDefinition> factory = new SwitchableFieldFactory<>(definition, nodeAdapter, fieldFactoryFactory, componentProvider, i18nAuthoringSupport);
        SwitchableField field = (SwitchableField) factory.createField();
        Iterator<Component> componentIterator = field.iterator();
        // CustomField#getContent is lazy, we trigger it via ComponentIterator#next()
        VerticalLayout layout = (VerticalLayout) componentIterator.next();

        OptionGroup options = (OptionGroup) layout.getComponent(0);
        TextField t1 = (TextField) layout.getComponent(1);
        TextField t2 = (TextField) layout.getComponent(2);

        // WHEN
        options.setValue("t1");
        t1.setValue("v1");
        options.setValue("t2");
        t2.setValue("v2");
        nodeAdapter.applyChanges();

        // THEN
        assertThat(node, hasProperty("switchable", "t2"));
        assertThat(node, hasProperty("switchablet2", "v2"));
        assertThat(node, not(hasProperty("switchablet1")));
    }

    private SwitchableFieldDefinition createSwitchableFieldDefinition(String name) {
        SwitchableFieldDefinition switchable = new SwitchableFieldDefinition();
        switchable.setName(name);

        TextFieldDefinition t1 = new TextFieldDefinition();
        t1.setName("t1");
        TextFieldDefinition t2 = new TextFieldDefinition();
        t2.setName("t2");
        switchable.setFields(Lists.<ConfiguredFieldDefinition>newArrayList(t1, t2));

        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setName("t1");
        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setName("t2");
        switchable.setOptions(newArrayList(option1, option2));
        return switchable;
    }

    private static class MockFieldFactoryFactory extends FieldFactoryFactory {

        private final Map<Class<? extends FieldDefinition>, Class<? extends FieldFactory>> fieldTypes = Maps.newHashMap();
        private final ComponentProvider componentProvider;

        public MockFieldFactoryFactory(ComponentProvider componentProvider) {
            super(null, null, null);
            this.componentProvider = componentProvider;
            registerFieldType(TextFieldDefinition.class, TextFieldFactory.class);
            registerFieldType(OptionGroupFieldDefinition.class, OptionGroupFieldFactory.class);
            registerFieldType(SelectFieldDefinition.class, SelectFieldFactory.class);
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

    private static void mockMessagesManager() {
        MockMessagesManager messagesManager = mock(MockMessagesManager.class);
        Messages messages = mock(Messages.class);
        doAnswer(new ReturnsArgumentAt(0)).when(messages).get(anyString());
        doReturn(messages).when(messagesManager).getMessagesInternal(anyString(), any(Locale.class));
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
    }

    /**
     * Just exposing #getMessagesInternal() for mocking.
     */
    private abstract static class MockMessagesManager extends MessagesManager {

        @Override
        public abstract Messages getMessagesInternal(String basename, Locale locale);
    }
}
