/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ObservedManager for dialogs configured in repository.
 */
@Singleton
public class ConfiguredDialogDefinitionManager extends ModuleConfigurationObservingManager {

    public static final String DIALOG_CONFIG_NODE_NAME = "dialogs";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private final DialogDefinitionRegistry dialogDefinitionRegistry;

    @Inject
    public ConfiguredDialogDefinitionManager(ModuleRegistry moduleRegistry, DialogDefinitionRegistry dialogDefinitionRegistry) {
        super(DIALOG_CONFIG_NODE_NAME, moduleRegistry);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<DialogDefinitionProvider> providers = new ArrayList<DialogDefinitionProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        if (isDialog(dialogNode)) {
                            // Handle as dialog only if it has sub nodes indicating that it is actually representing a dialog.
                            // This will filter the fields in dialogs used by the extends mechanism.
                            DialogDefinitionProvider provider = createProvider(dialogNode);
                            if (provider != null) {
                                providers.add(provider);
                            }
                        }
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
        }

        this.registeredIds = dialogDefinitionRegistry.unregisterAndRegister(registeredIds, providers);
    }

    /**
     * Check if this node can be handle as a ConfiguredDialogDefinition.
     */
    private boolean isDialog(Node dialogNode) throws RepositoryException {
        Node node = dialogNode;
        if (node.hasProperty(ConfiguredFormDialogDefinition.EXTEND_PROPERTY_NAME)) {
            node = new ExtendingNodeWrapper(dialogNode);
        }
        return node.hasNode(ConfiguredFormDialogDefinition.FORM_NODE_NAME)
                || node.hasNode(ConfiguredFormDialogDefinition.ACTIONS_NODE_NAME);
    }

    protected DialogDefinitionProvider createProvider(Node dialogNode) throws RepositoryException {

        final String id = createId(dialogNode);

        try {
            return new ConfiguredDialogDefinitionProvider(id, dialogNode);
        } catch (IllegalArgumentException e) {
            // TODO dlipp - suppress stacktrace as long as SCRUM-1749 is not fixed
            log.error("Unable to create provider for dialog [" + id + "]: " + e);
        } catch (Exception e) {
            log.error("Unable to create provider for dialog [" + id + "]", e);
        }
        return null;
    }

    protected String createId(Node configNode) throws RepositoryException {
        final String path = configNode.getPath();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        return moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/" + DIALOG_CONFIG_NODE_NAME + "/");
    }
}
