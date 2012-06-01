/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.ui.admincentral.dialog.registry.DummyDialogDefinitionManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;

import java.util.List;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AdminCentralApplication extends Application {
    private Window window;

    @Override
    public void init() {
        setTheme("testtheme");

        // Read component configurations from module descriptors
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = Components.getComponent(ModuleRegistry.class).getModuleDefinitions();
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules("admin-central", moduleDefinitions);
        configuration = configuration.clone();
        configuration.addComponent(InstanceConfiguration.valueOf(Application.class, this));

        // Create the component provider
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) Components.getComponentProvider());
        GuiceComponentProvider componentProvider = builder.build();

        DummyDialogDefinitionManager dialogManager = componentProvider.newInstance(DummyDialogDefinitionManager.class);
        dialogManager.load();
        window = new Window("Magnolia shell test");
        window.setContent(new CssLayout(){

            @Override
            protected String getCss(Component c) {
                return super.getCss(c);
            }

        });
        window.getContent().setSizeFull();
        ((CssLayout) window.getContent()).setMargin(false);
        setMainWindow(window);
        final MagnoliaShellPresenter presenter = componentProvider.newInstance(MagnoliaShellPresenter.class);
        presenter.start(window);
    }
}

