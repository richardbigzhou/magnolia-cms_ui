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
package info.magnolia.ui.admincentral.field;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import org.vaadin.easyuploads.UploadField;

import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;

/**
 * Creates and configures a Vaadin UploadField.
 */
public class FileUploadField extends AbstractDialogField<FileUploadFieldDefinition> {

    public FileUploadField(FileUploadFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field buildField() {
        UploadField uploadField = new UploadField();
        uploadField.setStorageMode(UploadField.StorageMode.MEMORY);
        uploadField.setFieldType(UploadField.FieldType.UTF8_STRING);
        return uploadField;
    }

    @Override
    public void setPropertyDataSource(final Property property) {
        super.setPropertyDataSource(property);

        // For some reason we need to add this to get the value in the field into the Property. The field does not
        // do this on its own.
        field.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                property.setValue(field.getValue());
            }
        });
    }
}
