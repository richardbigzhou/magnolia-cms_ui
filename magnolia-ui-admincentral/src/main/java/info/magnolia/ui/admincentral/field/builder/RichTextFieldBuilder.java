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

import java.util.ArrayList;
import java.util.List;

import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.integration.widget.MagnoliaRichTextField;
import info.magnolia.ui.vaadin.integration.widget.MagnoliaRichTextFieldConfig;
import info.magnolia.ui.vaadin.integration.widget.MagnoliaRichTextFieldConfig.ToolbarGroup;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an edit field based on a field definition.
 */
public class RichTextFieldBuilder extends
        AbstractFieldBuilder<RichTextFieldDefinition> {

    public RichTextFieldBuilder(RichTextFieldDefinition definition,
            Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field buildField() {
        // RichTextFieldDefinition editDefinition = definition;
        MagnoliaRichTextFieldConfig config = new MagnoliaRichTextFieldConfig();

        List<ToolbarGroup> toolbars = new ArrayList<ToolbarGroup>();
        toolbars.add(new ToolbarGroup("basictyles", new String[] { "Bold",
                "Italic", "Underline", "SpecialChar" }));
        toolbars.add(new ToolbarGroup("paragraph", new String[] {
                "NumberedList", "BulletedList" }));
        toolbars.add(new ToolbarGroup("insert",
                new String[] { "Link", "Unlink" }));
        toolbars.add(new ToolbarGroup("clipboard", new String[] { "Cut",
                "Copy", "Paste", "PasteText", "PasteFromWord" }));
        toolbars.add(new ToolbarGroup("objects", new String[] { "Image",
                "Table" }));
        toolbars.add(new ToolbarGroup("special", new String[] { "Undo", "Redo",
                "Demo" }));
        config.addToolbarLine(toolbars);
        config.addListenedEvent("demoevent");

        final MagnoliaRichTextField richtexteditor = new MagnoliaRichTextField(config);
        richtexteditor.addListener(new MagnoliaRichTextField.PluginListener() {
            
            @Override
            public void onPluginEvent(String eventName, String value) {
                richtexteditor.firePluginEvent("customevent", value);
            }
        });
        
        return richtexteditor;
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }
}
