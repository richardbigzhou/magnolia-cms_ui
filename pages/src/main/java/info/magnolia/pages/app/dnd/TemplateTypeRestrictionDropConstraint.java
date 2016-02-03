/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.pages.app.dnd;

import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;

import com.vaadin.data.Item;

/**
 * TemplateTypeRestrictionDropConstraint allows drag-n-drop if and only if the
 * parent node's template type is compatible with child node's template.
 */
public class TemplateTypeRestrictionDropConstraint implements DropConstraint {

    private Logger log  = Logger.getLogger(getClass());

    private TemplateDefinitionAssignment templateAssignment;

    @Inject
    public TemplateTypeRestrictionDropConstraint(TemplateDefinitionAssignment templateAssignment) {
        this.templateAssignment = templateAssignment;
    }

    @Override
    public boolean allowedAsChild(Item sourceItem, Item targetItem) {
        Node sourceNode = applyChanges(sourceItem);
        Node targetNode = applyChanges(targetItem);
        if (sourceNode != null && targetNode != null) {
            try {
                TemplateDefinition sourceTemplateDefinition = templateAssignment.getAssignedTemplateDefinition(sourceNode);
                return templateAssignment.getAvailableTemplates(targetNode).contains(sourceTemplateDefinition);
            } catch (RegistrationException e) {
                log.error("Failed to validate template compatibility for drag-and-drop: " + e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public boolean allowedBefore(Item sourceItem, Item targetItem) {
        return true;
    }

    @Override
    public boolean allowedAfter(Item sourceItem, Item targetItem) {
        return true;
    }

    @Override
    public boolean allowedToMove(Item sourceItem) {
        return applyChanges(sourceItem) != null;
    }

    private Node applyChanges(final Item item) {
        Node node = null;
        if (item instanceof JcrNodeAdapter) {
            try {
                node = ((JcrNodeAdapter) item).applyChanges();
            } catch (RepositoryException e) {
                log.error("Cannot apply changes.", e);
            }
        }
        return node;
    }
}
