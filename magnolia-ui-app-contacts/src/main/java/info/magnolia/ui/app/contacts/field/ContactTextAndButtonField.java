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
import info.magnolia.ui.model.thumbnail.ImageProvider;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;


/**
 * Specific Contact TextAndButtonField field that add a Thumbnail before the select action and field
 * see basic implementation {@link TextAndButtonField}.
 */
public class ContactTextAndButtonField extends CustomField{

    private TextAndButtonField textAndButtonField;

    public ContactTextAndButtonField(TextAndButtonField textAndButtonField, ImageProvider imageThumbnailProvider, String workspace) {
        //used to set the correct property and values
        this.textAndButtonField = textAndButtonField;
        HorizontalLayout layout = new HorizontalLayout();
        // Add Thumbnail Field
        ContactThumbnailField thumbnail = new ContactThumbnailField(imageThumbnailProvider, workspace);
        thumbnail.ValueChangeListener(textAndButtonField.getTextField());
        layout.addComponent(thumbnail);
        // Add Select Field
        layout.addComponent(textAndButtonField);
        layout.setComponentAlignment(textAndButtonField, Alignment.MIDDLE_RIGHT);
        layout.setMargin(true);

        setCompositionRoot(layout);
    }

    @Override
    public Class< ? > getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return textAndButtonField.getValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        textAndButtonField.setValue(newValue);
    }
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        textAndButtonField.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return textAndButtonField.getPropertyDataSource();
    }
}
