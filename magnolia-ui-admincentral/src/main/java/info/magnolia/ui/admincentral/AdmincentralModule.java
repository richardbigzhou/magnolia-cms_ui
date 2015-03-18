/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.admincentral;

import info.magnolia.config.source.ConfigurationSourceFactory;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.admincentral.usermenu.definition.UserMenuDefinition;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutDefinition;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Binds the {@link ItemViewDefinitionRegistry} to the configuration sources (JCR and YAML).
 * Initializes app launcher layout.
 */
public class AdmincentralModule implements ModuleLifecycle {

    private UserMenuDefinition userControl;

    private ItemViewDefinitionRegistry itemViewDefinitionRegistry;

    private AppLauncherLayoutManager appLauncherLayoutManager;
    private ConfigurationSourceFactory configurationSourceFactory;
    private AppLauncherLayoutDefinition appLauncherLayout;
    private PulseDefinition pulse;

    @Inject
    public AdmincentralModule(ItemViewDefinitionRegistry itemViewDefinitionRegistry, AppLauncherLayoutManager appLauncherLayoutManager,
            ConfigurationSourceFactory configurationSourceFactory) {
        this.itemViewDefinitionRegistry = itemViewDefinitionRegistry;
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.configurationSourceFactory = configurationSourceFactory;
    }

    @Override
    public void start(ModuleLifecycleContext context) {
        if (context.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
            configurationSourceFactory.jcr().withFilter(new IsViewType()).withModulePath("messageViews").bindTo(itemViewDefinitionRegistry);
            configurationSourceFactory.yaml().bindWithDefaults(itemViewDefinitionRegistry);
        }
        appLauncherLayoutManager.setLayout(getAppLauncherLayout());
    }

    @Override
    public void stop(ModuleLifecycleContext context) {
    }

    public UserMenuDefinition getUserMenu() {
        return userControl;
    }

    public void setUserMenu(UserMenuDefinition userControl) {
        this.userControl = userControl;
    }

    public AppLauncherLayoutDefinition getAppLauncherLayout() {
        return appLauncherLayout;
    }

    public void setAppLauncherLayout(AppLauncherLayoutDefinition appLauncherLayout) {
        this.appLauncherLayout = appLauncherLayout;
    }

    public PulseDefinition getPulse() {
        return pulse;
    }

    public void setPulse(PulseDefinition pulse) {
        this.pulse = pulse;
    }

    /**
     * Evaluates if the considered node can be treated as a {@link info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition}.
     */
    private class IsViewType extends AbstractPredicate<Node> {

        public static final String MESSAGE_VIEW_CONFIG_NODE_NAME = "messageViews";

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                if (node.hasProperty(ConfiguredFormDialogDefinition.EXTEND_PROPERTY_NAME)) {
                    node = new ExtendingNodeWrapper(node);
                }
                return MESSAGE_VIEW_CONFIG_NODE_NAME.equals(node.getParent().getName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
