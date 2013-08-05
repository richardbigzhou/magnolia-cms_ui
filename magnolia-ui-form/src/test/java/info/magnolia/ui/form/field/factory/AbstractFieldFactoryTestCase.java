/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.NoSuchComponentException;
import info.magnolia.objectfactory.ParameterResolver;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.form.field.property.basic.BasicProperty;
import info.magnolia.ui.form.field.property.basic.BasicPropertyHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.DefaultConverterFactory;
import com.vaadin.server.VaadinSession;

/**
 * Abstract test class used for testing implementations of {@link FieldFactory}.
 */
public abstract class AbstractFieldFactoryTestCase<D extends FieldDefinition> {

    protected static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    protected DefaultI18nContentSupport i18nContentSupport;
    protected final String workspaceName = "workspace";
    protected MockSession session;
    protected String propertyName = "propertyName";
    protected String itemName = "item";
    protected Node baseNode;
    protected Item baseItem;
    protected D definition;
    protected SimpleComponentProvider provider;

    @Before
    public void setUp() throws Exception {
        // Init Message & Providers

        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getLocale()).thenReturn(DEFAULT_LOCALE);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        VaadinSession vaadinSession = mock(VaadinSession.class);
        when(vaadinSession.getConverterFactory()).thenReturn(new DefaultConverterFactory());
        when(vaadinSession.getLocale()).thenReturn(DEFAULT_LOCALE);
        when(vaadinSession.hasLock()).thenReturn(true);
        ComponentsTestUtil.setInstance(VaadinSession.class, vaadinSession);

        // Init Session
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);

        // Create ConfiguredField POJO
        createConfiguredFieldDefinition();

        // Init Node and Item.
        Node rootNode = session.getRootNode();
        baseNode = rootNode.addNode(itemName);
        baseItem = new JcrNodeAdapter(baseNode);

        // Init i18n
        i18nContentSupport = new DefaultI18nContentSupport();
        i18nContentSupport.setFallbackLocale(DEFAULT_LOCALE);

    }

    protected void setComponentProviderAndHandler(ConfiguredFieldDefinition definition, Class typeClass, Item baseItem) {
        BasicPropertyHandler basicHandler = new BasicPropertyHandler(baseItem, definition, null, typeClass.getName());
        provider = new SimpleComponentProvider(basicHandler, new BasicProperty(basicHandler, typeClass));
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    /**
     * Create the specific ConfiguredFieldDefinition or sub class.
     */
    protected abstract void createConfiguredFieldDefinition();

    public static class SimpleComponentProvider implements ComponentProvider {
        private PropertyHandler handler = null;
        private Property propertyType = null;

        public SimpleComponentProvider(PropertyHandler handler, Property propertyType) {
            this.handler = handler;
            this.propertyType = propertyType;
        }

        public void setHandler(PropertyHandler handler) {
            this.handler = handler;
        }

        public void setPropertyType(Property propertyType) {
            this.propertyType = propertyType;
        }

        @Override
        public <T> Class<? extends T> getImplementation(Class<T> type) throws ClassNotFoundException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T getSingleton(Class<T> type) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T getComponent(Class<T> type) throws NoSuchComponentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T newInstance(Class<T> type, Object... parameters) {
            if (type.isAssignableFrom(handler.getClass())) {
                return (T) handler;
            } else {
                return (T) propertyType;
            }
        }

        @Override
        public <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameters) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ComponentProvider getParent() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}