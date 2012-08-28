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
package info.magnolia.ui.app.contacts.field.builder;

import info.magnolia.ui.admincentral.field.TextAndButtonField;
import info.magnolia.ui.admincentral.field.builder.LinkFieldBuilder;
import info.magnolia.ui.admincentral.image.ImageThumbnailProvider;
import info.magnolia.ui.app.contacts.field.ContactTextAndButtonField;
import info.magnolia.ui.app.contacts.field.definition.ContactLinkFieldDefinition;
import info.magnolia.ui.framework.app.AppController;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a ContactLinkField field based on a field definition.
 */
public class ContactLinkFieldBuilder extends LinkFieldBuilder<ContactLinkFieldDefinition>{

    private ImageThumbnailProvider imageThumbnailProvider;

    @Inject
    public ContactLinkFieldBuilder(ImageThumbnailProvider imageThumbnailProvider, ContactLinkFieldDefinition definition, Item relatedFieldItem, AppController appController) {
        super(definition, relatedFieldItem, appController);
        this.imageThumbnailProvider = imageThumbnailProvider;
    }

    @Override
    protected Field buildField() {
        TextAndButtonField textAndButton = (TextAndButtonField)super.buildField();
        ContactTextAndButtonField field = new ContactTextAndButtonField(textAndButton,imageThumbnailProvider,definition.getWorkspace(),150,150);

        return field;
    }
}
