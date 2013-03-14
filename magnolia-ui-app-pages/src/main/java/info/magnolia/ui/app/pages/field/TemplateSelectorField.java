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
package info.magnolia.ui.app.pages.field;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.ui.admincentral.field.builder.SelectFieldBuilder;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;

import com.vaadin.data.Item;

/**
 * Define a Template selector field.
 * The values displayed in the field are initialized based on the
 * related Item (Image of a JCR node) and {@link TemplateDefinitionAssignment}.
 */
public class TemplateSelectorField extends SelectFieldBuilder<TemplateSelectorDefinition> {

    public TemplateSelectorField(TemplateSelectorDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    /**
     * Returns the available templates based on the current node.
     */
    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> res = new ArrayList<SelectFieldOptionDefinition>();
        TemplateDefinitionAssignment templateAssignment = Components.getComponent(TemplateDefinitionAssignment.class);
        Collection<TemplateDefinition> templates = templateAssignment.getAvailableTemplates(asNode(item));

        for (TemplateDefinition templateDefinition : templates) {
            SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
            option.setValue(templateDefinition.getId());
            option.setLabel(getI18nTitle(templateDefinition));
            res.add(option);
        }
        return res;
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }

    /**
     * Get i18n Template title.
     */
    // FIXME: SCRUM-1635 (ehe) review PageEditorPresenter and way Templates are parsed.
    public static synchronized String getI18nTitle(TemplateDefinition templateDefinition) {
        Messages messages = MessagesManager.getMessages(templateDefinition.getI18nBasename());
        return messages.getWithDefault(templateDefinition.getTitle(), templateDefinition.getTitle());
    }

    private Node asNode(final Item item) {
        if (item instanceof JcrNewNodeAdapter) {
            return ((JcrNewNodeAdapter) item).getNode();
        } else {
            return ((JcrNodeAdapter) item).getNode();
        }
    }
}
