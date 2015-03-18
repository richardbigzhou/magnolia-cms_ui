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
package info.magnolia.ui.mediaeditor;

import info.magnolia.config.source.ConfigurationSourceFactory;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.mediaeditor.registry.MediaEditorRegistry;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Binds {@link MediaEditorRegistry} to JCR and Yaml config sources.
 */
public class MediaEditorModule implements ModuleLifecycle {

    private ConfigurationSourceFactory configSourceFactory;

    private MediaEditorRegistry registry;

    @Inject
    public MediaEditorModule(ConfigurationSourceFactory configSourceFactory, MediaEditorRegistry registry) {
        this.configSourceFactory = configSourceFactory;
        this.registry = registry;
    }

    @Override
    public void start(ModuleLifecycleContext context) {
        if (context.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {

            configSourceFactory.jcr().withFilter(new IsMediaEditor()).withModulePath("mediaEditors").bindTo(registry);
            configSourceFactory.yaml().bindWithDefaults(registry);
        }
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    /**
     * Check if this node can be handled as an {@link info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition}.
     * Prior to 5.4, this was in {@link info.magnolia.ui.mediaeditor.registry.ConfiguredMediaEditorDefinitionManager}.
     */
    private static class IsMediaEditor extends AbstractPredicate<Node> {

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                if (node.hasProperty(ConfiguredFormDialogDefinition.EXTEND_PROPERTY_NAME)) {
                    node = new ExtendingNodeWrapper(node);
                }
                return "mediaEditors".equalsIgnoreCase(node.getParent().getName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
