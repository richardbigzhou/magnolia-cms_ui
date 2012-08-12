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
import info.magnolia.ui.admincentral.field.upload.AbstractUploadFileField.DefaultComponent;
import info.magnolia.ui.admincentral.field.upload.UploadImageField;
import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import org.junit.Test;

import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Field;

/**
 * Main testcase for {@link FileUploadFieldBuilder}.
 */
public class FileUploadFieldBuilderTest extends AbstractBuilderTest<FileUploadFieldDefinition> {

    private FileUploadFieldBuilder fileUploadBuilder;

    @Test
    public void simpleFileUploadFieldBuilderTest() throws Exception{
        // GIVEN
        fileUploadBuilder = new FileUploadFieldBuilder(definition, baseItem);

        // WHEN
        Field field = fileUploadBuilder.getField();

        // THEN
        assertEquals(true, field instanceof UploadImageField);
        assertEquals(0, ((AbstractJcrNodeAdapter)baseItem).getChilds().size());
    }

    @Test
    public void buildDefaultUploadLayoutTest() throws Exception{
        // GIVEN
        fileUploadBuilder = new FileUploadFieldBuilder(definition, baseItem);
        UploadImageField field = (UploadImageField)fileUploadBuilder.getField();

        // WHEN
        field.buildDefaultUploadLayout();

        // THEN
        assertEquals(true, field.getDefaultComponent(DefaultComponent.UPLOAD).isVisible());
        assertEquals(true, field.getRootLayout() instanceof DragAndDropWrapper);
    }


    @Override
    protected void createConfiguredFieldDefinition() {
        FileUploadFieldDefinition fieldDefinition = new FileUploadFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
