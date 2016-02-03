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
package info.magnolia.ui.api.app.registry;

import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.registry.RegistrationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.NodeTypePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObservedManager for {@link info.magnolia.ui.api.app.AppDescriptor} configured in repository.
 */
@Singleton
public class ConfiguredAppDescriptorManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public ConfiguredAppDescriptorManager(ModuleRegistry moduleRegistry, AppDescriptorRegistry appDescriptorRegistry) {
        super("apps", moduleRegistry);
        this.appDescriptorRegistry = appDescriptorRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<AppDescriptorProvider> providers = new ArrayList<AppDescriptorProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node nodeToVisit) throws RepositoryException {
                    for (Node configNode : NodeUtil.getNodes(nodeToVisit, NodeTypes.ContentNode.NAME)) {
                        AppDescriptorProvider provider = createProvider(configNode);
                        if (provider != null) {
                            providers.add(provider);
                        }
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME, false));
        }
        try {
            this.registeredIds = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);
        } catch (RegistrationException re) {
            throw new RepositoryException(re);
        }
    }

    protected AppDescriptorProvider createProvider(Node appDefinitionNode) throws RepositoryException {

        try {
            return new ConfiguredAppDescriptorProvider(appDefinitionNode);
        } catch (Exception e) {
            log.error("Unable to create provider for appDescriptor [" + appDefinitionNode.getPath() + "]", e);
            return null;
        }
    }

}
