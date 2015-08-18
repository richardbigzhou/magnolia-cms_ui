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
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;
import info.magnolia.ui.form.field.factory.CompositeFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.composite.CompositeTransformer;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractField;

public class CompositeFieldTest extends AbstractFieldFactoryTestCase<CompositeFieldDefinition> {

    private FieldFactoryFactory fieldFactoryFactory;

    private Item relatedItem;

    private CompositeField multiField;

    private CompositeFieldFactory compositeFieldFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.fieldFactoryFactory = mock(FieldFactoryFactory.class);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        this.definition = new CompositeFieldDefinition();
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

        this.definition.setFields(Arrays.asList(childFieldDef1, childFieldDef2));
        this.definition.setRequired(true);

        final CompositeTransformer compositeTransformer = new CompositeTransformer(relatedItem, this.definition, PropertysetItem.class, Arrays.asList("f1", "f2"), mock(I18NAuthoringSupport.class));
        componentProvider.setInstance(CompositeTransformer.class, compositeTransformer);
        componentProvider.setImplementation(CompositeTransformer.class, CompositeTransformer.class.getName());

        this.compositeFieldFactory = new CompositeFieldFactory(this.definition, relatedItem, fieldFactoryFactory, componentProvider, i18NAuthoringSupport);

        // WHEN
        this.multiField = (CompositeField) compositeFieldFactory.createField();
        // Forces the getContent() call internally which initializes Vaadin layout.
        multiField.iterator().next();

        // THEN
        assertTrue(multiField.isEmpty());
        assertFalse(multiField.isValid());
    }
}
