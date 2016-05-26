/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.extension.MaxLengthIndicator;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Creates and initializes an edit field based on a field definition.
 */
public class TextFieldFactory extends AbstractFieldFactory<TextFieldDefinition, String> {

    private AbstractTextField field;

    @Inject
    public TextFieldFactory(TextFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #TextFieldFactory(TextFieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public TextFieldFactory(TextFieldDefinition definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<String> createFieldComponent() {
        // Create a TextArea if the rows > 1
        if (definition.getRows() > 1) {
            TextArea textArea = new TextArea();
            textArea.setRows(definition.getRows());
            field = textArea;
        } else {
            field = new TextField();
        }
        field.setNullRepresentation("");
        field.setNullSettingAllowed(true);
        if (definition.getMaxLength() != -1) {
            field.setMaxLength(definition.getMaxLength());
            MaxLengthIndicator.extend(field);
        }
        return field;
    }


}
