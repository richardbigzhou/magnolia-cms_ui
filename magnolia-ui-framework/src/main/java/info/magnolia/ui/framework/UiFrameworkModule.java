/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework;

import info.magnolia.config.source.ConfigurationSourceFactory;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.fieldtype.registry.ConfiguredFieldTypeDefinitionManager;

import java.nio.file.Paths;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Module class for UI framework.
 */
public class UiFrameworkModule implements ModuleLifecycle {

    private final ConfiguredFieldTypeDefinitionManager fieldTypeDefinitionManager;

    private final ConfigurationSourceFactory configSourceFactory;
    private final DialogDefinitionRegistry dialogRegistry;
    private AppDescriptorRegistry appDescriptorRegistry;
    private final String magnoliaHome;

    @Inject
    public UiFrameworkModule(ConfigurationSourceFactory configSourceFactory, MagnoliaConfigurationProperties mcp,
                             DialogDefinitionRegistry dialogRegistry, ConfiguredFieldTypeDefinitionManager configuredFieldTypeDefinitionManager,
                             AppDescriptorRegistry appDescriptorRegistry) {
        this.fieldTypeDefinitionManager = configuredFieldTypeDefinitionManager;

        this.configSourceFactory = configSourceFactory;
        this.dialogRegistry = dialogRegistry;
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.magnoliaHome = mcp.getProperty("magnolia.home");
    }

    @Override
    public void start(ModuleLifecycleContext context) {
        if (context.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {

            fieldTypeDefinitionManager.start();
            configSourceFactory.jcr().withFilter(new IsAppDescriptor()).bindWithDefaults(appDescriptorRegistry);
            configSourceFactory.jcr().withFilter(new IsDialogNode()).bindWithDefaults(dialogRegistry);

            configSourceFactory.yaml().from(Paths.get(magnoliaHome)).bindWithDefaults(dialogRegistry); // TODO mge check if defaults can be implied as well for magnoliaHome
            configSourceFactory.yaml().from(Paths.get(magnoliaHome)).bindWithDefaults(appDescriptorRegistry); // TODO mge check if defaults can be implied as well for magnoliaHome
        }
    }

    @Override
    public void stop(ModuleLifecycleContext context) {
    }


    /**
     * Check if this node can be handled as a ConfiguredDialogDefinition.
     * Prior to 5.4, this was in {@link info.magnolia.ui.dialog.registry.ConfiguredDialogDefinitionManager}.
     */
    private static class IsDialogNode extends AbstractPredicate<Node> {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                if (node.hasProperty(ConfiguredFormDialogDefinition.EXTEND_PROPERTY_NAME)) {
                    node = new ExtendingNodeWrapper(node);
                }
                return node.hasNode(ConfiguredFormDialogDefinition.FORM_NODE_NAME) || node.hasNode(ConfiguredFormDialogDefinition.ACTIONS_NODE_NAME);
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }

    /**
     * Check if this node can be handled as an {@link info.magnolia.ui.api.app.AppDescriptor}.
     * Prior to 5.4, this was in {@link info.magnolia.ui.api.app.registry.ConfiguredAppDescriptorManager}.
     */
    private static class IsAppDescriptor extends AbstractPredicate<Node> {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                if (node.hasProperty(ConfiguredFormDialogDefinition.EXTEND_PROPERTY_NAME)) {
                    node = new ExtendingNodeWrapper(node);
                }
                return "apps".equalsIgnoreCase(node.getParent().getName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }
}
