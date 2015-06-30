/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.EmptyMessages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.factory.CompositeFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.composite.CompositeTransformer;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractField;

public class CompositeFieldTest extends MgnlTestCase {

    private FieldFactoryFactory fieldFactoryFactory;

    private I18NAuthoringSupport i18NAuthoringSupport;

    private ComponentProvider componentProvider;

    private Item relatedItem;

    private CompositeField multiField;

    private CompositeFieldFactory compositeFieldFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.fieldFactoryFactory = mock(FieldFactoryFactory.class);
        this.i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        this.componentProvider = mock(ComponentProvider.class);
        MockUtil.getMockContext().setLocale(Locale.ENGLISH);
        MessagesManager messagesManager = mock(MessagesManager.class);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);

        when(messagesManager.getMessages(anyString(), any(Locale.class))).thenReturn(new EmptyMessages());
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
    }

    @Test
    public void testIsRequired() throws Exception {
        // GIVEN
        this.relatedItem = new PropertysetItem();

        // Child field definitions
        final ConfiguredFieldDefinition childFieldDef1 = new ConfiguredFieldDefinition();
        childFieldDef1.setName("f1");

        final ConfiguredFieldDefinition childFieldDef2 = new ConfiguredFieldDefinition();
        childFieldDef2.setName("f2");


        // Child field factories
        final FieldFactory childFieldFactory1 = mock(FieldFactory.class);
        doReturn(childFieldFactory1).when(fieldFactoryFactory).createFieldFactory(eq(childFieldDef1), anyVararg());

        final FieldFactory childFieldFactory2 = mock(FieldFactory.class);
        doReturn(childFieldFactory2).when(fieldFactoryFactory).createFieldFactory(eq(childFieldDef2), anyVararg());

        // Vaadin field mocks
        final AbstractField f1 = mock(AbstractField.class);
        final AbstractField f2 = mock(AbstractField.class);
        // One field has the value set while the other - doesn't
        doReturn(null).when(f2).getValue();
        doReturn(new Object()).when(f1).getValue();

        doReturn(f1).when(childFieldFactory1).createField();
        doReturn(f2).when(childFieldFactory2).createField();

        // Composite field/factory/transformer
        final CompositeFieldDefinition fieldDefinition = new CompositeFieldDefinition();
        fieldDefinition.setFields(Arrays.asList(childFieldDef1, childFieldDef2));
        fieldDefinition.setRequired(true);
        fieldDefinition.setName("compositeFieldDefinition");

        final CompositeTransformer compositeTransformer = new CompositeTransformer(relatedItem, fieldDefinition, PropertysetItem.class, Arrays.asList("f1", "f2"));
        doReturn(compositeTransformer).when(componentProvider).newInstance(eq(CompositeTransformer.class), anyVararg());

        this.compositeFieldFactory = new CompositeFieldFactory(fieldDefinition, relatedItem, fieldFactoryFactory, componentProvider, i18NAuthoringSupport);

        // WHEN
        //given(relatedItem.getItemProperty(""));
        this.multiField = (CompositeField) compositeFieldFactory.createField();
        // Forces the getContent() call internally which initializes Vaadin layout.
        multiField.iterator().next();

        // THEN
        assertTrue(multiField.isEmpty());
        assertFalse(multiField.isValid());
    }
}
