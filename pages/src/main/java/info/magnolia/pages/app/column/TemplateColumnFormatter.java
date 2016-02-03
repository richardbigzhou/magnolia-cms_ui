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
package info.magnolia.pages.app.column;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Table;

/**
 * Column formatter used to display the title of a page's template.
 *
 * @see TemplateColumnDefinition
 */
public class TemplateColumnFormatter extends AbstractColumnFormatter<TemplateColumnDefinition> {

    private static final Logger log = LoggerFactory.getLogger(TemplateColumnFormatter.class);

    private TemplateDefinitionRegistry templateRegistry;
    private TemplateDefinitionAssignment templateDefinitionAssignment;

    public TemplateColumnFormatter(TemplateColumnDefinition definition, TemplateDefinitionRegistry templateRegistry, TemplateDefinitionAssignment templateDefinitionAssignment) {
        super(definition);
        this.templateRegistry = templateRegistry;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final Item jcrItem = getJcrItem(source, itemId);

        if (jcrItem == null || !jcrItem.isNode()) {
            return "";
        }

        Node node = (Node) jcrItem;

        // Get template id
        String templateId;
        try {
            templateId = templateDefinitionAssignment.getAssignedTemplate(node);
        } catch (RepositoryException e) {
            return "Unknown";
        }

        // Get template definition
        TemplateDefinition template = null;
        try {
            template = templateRegistry.getTemplateDefinition(templateId);
        } catch (RegistrationException e) {
            log.warn("Template with id {} not found.", templateId, e);
        }

        return template != null ? getI18nTitle(template) : "Missing: " + StringUtils.defaultString(templateId);
    }

    private String getI18nTitle(TemplateDefinition template) {
        Messages messages = MessagesManager.getMessages(template.getI18nBasename());
        String title = template.getTitle();
        return messages.getWithDefault(title, title);
    }
}
