/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.availability;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check if can add new component to an area.
 */
public class CanAddComponentRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(CanAddComponentRule.class);

    protected static final String PROPERTY_TYPE = "type";
    protected static final String PROPERTY_MAX_COMPONENTS = "maxComponents";

    private TemplateDefinitionRegistry templateRegistry;

    @Inject
    public CanAddComponentRule(TemplateDefinitionRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    @Override
    public boolean isAvailableForItem(Object itemId) {

        if (itemId instanceof JcrItemId && !(itemId instanceof JcrPropertyItemId)) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            Node areaNode = SessionUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
            if (areaNode == null) {
                return false;
            }
            try {
                if (!NodeUtil.isNodeType(areaNode, NodeTypes.Area.NAME)) {
                    return false;
                }
                String template = NodeTypes.Renderable.getTemplate(areaNode.getParent());
                TemplateDefinition parentPageDefinition = templateRegistry.getTemplateDefinition(template);
                AreaDefinition areaDefinition = parentPageDefinition.getAreas().get(areaNode.getName());
                String areaType = areaDefinition.getType() == null ? AreaDefinition.DEFAULT_TYPE : areaDefinition.getType();

                if (AreaDefinition.TYPE_NO_COMPONENT.equals(areaType)) {
                    return false;
                }

                int maxComponentsProperty = areaDefinition.getMaxComponents() == null ? Integer.MAX_VALUE : areaDefinition.getMaxComponents();
                int numberOfComponents = NodeUtil.asList(NodeUtil.getNodes(areaNode, new ComponentsAndContentNodesPredicate())).size();
                if (numberOfComponents >= maxComponentsProperty || numberOfComponents > 0 && AreaDefinition.TYPE_SINGLE.equals(areaType)) {
                    return false;
                }
                return true;

            } catch (RepositoryException e) {
                log.warn("Error evaluating availability for node [{}], returning false: {}", areaNode, e);
            } catch (RegistrationException e) {
                log.warn("Error evaluating availability for node [{}], returning false: {}", areaNode, e);
            }
        }
        return false;
    }

    private static class ComponentsAndContentNodesPredicate extends AbstractPredicate<Node> {

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return NodeUtil.isNodeType(node, NodeTypes.Component.NAME) || NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME);
            } catch (RepositoryException e) {
                log.error("Failed to retrieve node type of '{}':", node, e);
            }
            return false;
        }
    }
}
