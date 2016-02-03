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
package info.magnolia.ui.admincentral.shellapp.pulse.message.registry;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObservedManager for message views configured in repository.
 */
public class ConfiguredMessageViewDefinitionManager extends ModuleConfigurationObservingManager {

    public static final String MESSAGE_VIEW_CONFIG_NODE_NAME = "messageViews";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private final MessageViewDefinitionRegistry messageViewDefinitionRegistry;

    @Inject
    public ConfiguredMessageViewDefinitionManager(ModuleRegistry moduleRegistry, MessageViewDefinitionRegistry messageViewDefinitionRegistry) {
        super(MESSAGE_VIEW_CONFIG_NODE_NAME, moduleRegistry);
        this.messageViewDefinitionRegistry = messageViewDefinitionRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<MessageViewDefinitionProvider> providers = new ArrayList<MessageViewDefinitionProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node messageViewNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        // Handle as messageView only if it has sub nodes indicating that it is actually representing a messageView.
                        // This will filter the fields in messageViews used by the extends mechanism.
                        MessageViewDefinitionProvider provider = createProvider(messageViewNode);
                        if (provider != null) {
                            providers.add(provider);
                        }
                    }

                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
        }

        this.registeredIds = messageViewDefinitionRegistry.unregisterAndRegister(registeredIds, providers);
    }

    protected MessageViewDefinitionProvider createProvider(Node messageViewNode) throws RepositoryException {

        final String id = createId(messageViewNode);

        try {
            return new ConfiguredMessageViewDefinitionProvider(id, messageViewNode);
        } catch (IllegalArgumentException e) {
            log.error("Unable to create provider for messageView [" + id + "]: " + e);
        } catch (Exception e) {
            log.error("Unable to create provider for messageView [" + id + "]", e);
        }
        return null;
    }

    protected String createId(Node configNode) throws RepositoryException {
        final String path = configNode.getPath();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        return moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/" + MESSAGE_VIEW_CONFIG_NODE_NAME + "/");
    }
}
