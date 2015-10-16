/**
 * This file Copyright (c) 2015 Magnolia International
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

import static org.mockito.Mockito.mock;

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.util.List;

import com.vaadin.data.util.PropertysetItem;

public abstract class AbstractFieldTestCase<D extends FieldDefinition> extends AbstractFieldFactoryTestCase<D> {

    protected AbstractFieldFactory<D, PropertysetItem> factory;
    protected FieldFactoryFactory subfieldFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        componentProvider.registerInstance(ComponentProvider.class, componentProvider);
        componentProvider.registerInstance(AppController.class, mock(AppController.class));

        FieldTypeDefinitionRegistry fieldDefinitionRegistry = createFieldTypeRegistry(getFieldTypeDefinitions());
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistry, null);
    }

    protected abstract List<DefinitionProvider<FieldTypeDefinition>> getFieldTypeDefinitions();

    private FieldTypeDefinitionRegistry createFieldTypeRegistry(List<DefinitionProvider<FieldTypeDefinition>> definitions) {
        FieldTypeDefinitionRegistry registry = new FieldTypeDefinitionRegistry();

        for (DefinitionProvider<FieldTypeDefinition> def : definitions) {
            registry.register(def);
        }

        return registry;
    }

}