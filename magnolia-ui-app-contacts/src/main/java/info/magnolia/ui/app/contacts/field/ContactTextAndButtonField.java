/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.app.contacts.field;

import info.magnolia.ui.admincentral.field.TextAndButtonField;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;


/**
 * Specific Contact TextAndButtonField field that add a Thumbnail before the select action and field
 * see basic implementation {@link TextAndButtonField}.
 */
public class ContactTextAndButtonField extends CustomField<String> {

    private TextAndButtonField textAndButtonField;

    private ContactThumbnailField thumbnail;
    
    public ContactTextAndButtonField(TextAndButtonField textAndButtonField, ImageProvider imageThumbnailProvider, String workspace) {
        this.textAndButtonField = textAndButtonField;
        this.thumbnail = new ContactThumbnailField(imageThumbnailProvider, workspace);
    }

    @Override
    protected Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        // Add Thumbnail Field
        thumbnail.ValueChangeListener(textAndButtonField.getTextField());
        layout.addComponent(thumbnail);
        // Add Select Field
        layout.addComponent(textAndButtonField);
        layout.setComponentAlignment(textAndButtonField, Alignment.MIDDLE_RIGHT);
        layout.setMargin(true);

        return layout;
    }
    
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return textAndButtonField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException, ConversionException {
        textAndButtonField.setValue(newValue);
    }
    @Override
    @SuppressWarnings("rawtypes") 
    public void setPropertyDataSource(Property newDataSource) {
        textAndButtonField.setPropertyDataSource(newDataSource);
    }

    @Override
    @SuppressWarnings("rawtypes") 
    public Property getPropertyDataSource() {
        return textAndButtonField.getPropertyDataSource();
    }
}
