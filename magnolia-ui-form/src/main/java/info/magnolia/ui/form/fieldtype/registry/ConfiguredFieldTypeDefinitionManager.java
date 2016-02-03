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
package info.magnolia.ui.form.fieldtype.registry;

import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfiguredFieldTypeDefinitionManager.
 */
public class ConfiguredFieldTypeDefinitionManager extends ModuleConfigurationObservingManager {

    public static final String FIELD_CONFIG_NODE_NAME = "fieldTypes";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private final FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry;

    @Inject
    public ConfiguredFieldTypeDefinitionManager(ModuleRegistry moduleRegistry, FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry) {
        super(FIELD_CONFIG_NODE_NAME, moduleRegistry);
        this.fieldTypeDefinitionRegistry = fieldTypeDefinitionRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<FieldTypeDefinitionProvider> providers = new ArrayList<FieldTypeDefinitionProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node fieldTypeNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        if (isFieldType(fieldTypeNode)) {
                            // Handle as fieldType only if it has sub nodes indicating that it is actually representing a fieldType.
                            // This will filter the fields in fieldTypes used by the extends mechanism.
                            FieldTypeDefinitionProvider provider = createProvider(fieldTypeNode);
                            if (provider != null) {
                                providers.add(provider);
                            }
                        } else {
                            log.warn("node " + fieldTypeNode.getName() + " will not be handled as Field.");
                        }
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
        }

        this.registeredIds = fieldTypeDefinitionRegistry.unregisterAndRegister(registeredIds, providers);
    }

    /**
     * Check if this node can be handle as a ConfiguredFieldDefinition.
     */
    private boolean isFieldType(Node fieldTypeNode) throws RepositoryException {
        return true;
    }

    protected FieldTypeDefinitionProvider createProvider(Node fieldTypeNode) throws RepositoryException {

        final String id = fieldTypeNode.getName();

        try {
            return new ConfiguredFieldTypeDefinitionProvider(id, fieldTypeNode);
        } catch (IllegalArgumentException e) {
            log.error("Unable to create provider for fieldType [" + id + "]: " + e);
        } catch (Exception e) {
            log.error("Unable to create provider for fieldType [" + id + "]", e);
        }
        return null;
    }

}
