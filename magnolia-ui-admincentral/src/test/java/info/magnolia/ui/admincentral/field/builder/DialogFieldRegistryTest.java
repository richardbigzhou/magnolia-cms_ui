/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.objectfactory.ObservedComponentFactory;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.content.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.field.FieldBuilder;
import info.magnolia.ui.model.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.model.field.definition.DateFieldDefinition;
import info.magnolia.ui.model.field.definition.FieldDefinition;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.data.Item;
/**
 * Main test class for {@link DialogFieldRegistry}
 */
public class DialogFieldRegistryTest  extends RepositoryTestCase  {

    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(DefinitionToImplementationMapping.class, DefinitionToImplementationMapping.class);
        String dummyDialog =
            "/modules/ui-admincentral/dialogFieldRegistry.class="+DialogFieldRegistry.class.getName()+"\n" +
            "/modules/ui-admincentral/dialogFieldRegistry/definitionToImplementationMappings/textField.definition="+DefinitionOne.class.getName()+"\n" +
            "/modules/ui-admincentral/dialogFieldRegistry/definitionToImplementationMappings/textField.implementation="+BuilderOne.class.getName()+"\n" ;
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(dummyDialog));
        session.save();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testAppDescriptorOnStart() {
        // GIVEN
        ObservedComponentFactory<DialogFieldRegistry> observer = new ObservedComponentFactory(RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogFieldRegistry", DialogFieldRegistry.class);

        // WHEN
        List<DefinitionToImplementationMapping<FieldDefinition, FieldBuilder>> definitionToImplementationMappings = observer.newInstance().getDefinitionToImplementationMappings();

        // THEN
        assertNotNull(definitionToImplementationMappings);
        assertEquals(1, definitionToImplementationMappings.size());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Ignore
    @Test
    public void testAppDescriptorReloadsOnChange() throws RepositoryException{
        // GIVEN
        ObservedComponentFactory<DialogFieldRegistry> observer = new ObservedComponentFactory(RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogFieldRegistry", DialogFieldRegistry.class);
        Node newDefinition = session.getRootNode().addNode("/modules/ui-admincentral/dialogFieldRegistry/definitionToImplementationMappings/imageField");
        newDefinition.setProperty("definition", DefinitionTwo.class.getName());
        newDefinition.setProperty("implementation", BuilderTwo.class.getName());

        // WHEN
        List<DefinitionToImplementationMapping<FieldDefinition, FieldBuilder>> definitionToImplementationMappings = observer.newInstance().getDefinitionToImplementationMappings();

        // THEN
        assertNotNull(definitionToImplementationMappings);
        assertEquals(2, definitionToImplementationMappings.size());
    }


    public static class DefinitionOne extends CheckboxFieldDefinition{
        public DefinitionOne() {
            super();
        }
    }

    public static class BuilderOne extends CheckBoxFieldBuilder{

        public BuilderOne(CheckboxFieldDefinition definition, Item relatedFieldItem) {
            super(definition, relatedFieldItem);
        }

    }

    public static class DefinitionTwo extends DateFieldDefinition{
        public DefinitionTwo() {
            super();
        }
    }

    public static class BuilderTwo extends DateFieldBuilder{

        public BuilderTwo(DateFieldDefinition definition, Item relatedFieldItem) {
            super(definition, relatedFieldItem);
        }

    }
}
