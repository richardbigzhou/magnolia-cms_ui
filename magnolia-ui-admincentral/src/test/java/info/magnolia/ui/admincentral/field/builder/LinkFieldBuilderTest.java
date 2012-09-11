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
import info.magnolia.ui.admincentral.field.TextAndButtonField;
import info.magnolia.ui.model.field.definition.LinkFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link LinkFieldBuilder}.
 */
public class LinkFieldBuilderTest extends AbstractBuilderTest<LinkFieldDefinition> {

    private LinkFieldBuilder linkFieldBuilder;

    @Test
    public void simpleLinkFieldTest() throws Exception{
        // GIVEN
        linkFieldBuilder = new LinkFieldBuilder(definition, baseItem, null);
        linkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = linkFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndButtonField);
    }

    @Test
    public void simpleLinkFieldUuidTest() throws Exception{
        // GIVEN
        definition.setUuid(true);
        definition.setName(propertyName);
        definition.setWorkspace(workspaceName);
        baseNode.setProperty(propertyName, baseNode.getIdentifier());
        baseItem = new JcrNodeAdapter(baseNode);
        linkFieldBuilder = new LinkFieldBuilder(definition, baseItem, null);
        linkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = linkFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndButtonField);
        // Propert way set to the UUID baseNode.getIdentifier() and we display the path
        assertEquals(baseNode.getPath(), field.getValue());
    }

    @Test
    public void linkField_SetButtonCaptionTest() throws Exception{
        // GIVEN
        linkFieldBuilder = new LinkFieldBuilder(definition, baseItem, null);
        linkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        definition.setButtonLabel("Button Select");
        // WHEN
        Field field = linkFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndButtonField);
        assertEquals("Button Select", ((TextAndButtonField)field).getSelectButton().getCaption());
    }

    @Test
    public void linkField_SetFieldPropagation() throws Exception{
        // GIVEN
        definition.setName(propertyName);
        baseNode.setProperty(propertyName, "notChanged");
        baseItem = new JcrNodeAdapter(baseNode);
        linkFieldBuilder = new LinkFieldBuilder(definition, baseItem, null);
        linkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        Field field = linkFieldBuilder.getField();
        assertEquals("notChanged", (String)((TextAndButtonField)field).getTextField().getValue());
        // WHEN
        ((TextAndButtonField)field).getTextField().setValue("Changed");

        // THEN
        assertEquals("Changed", (String)baseItem.getItemProperty(propertyName).getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldDefinition fieldDefinition = new LinkFieldDefinition();
        fieldDefinition.setName(propertyName);
        fieldDefinition.setDialogName("dialogName");
        this.definition = fieldDefinition;
    }

}
