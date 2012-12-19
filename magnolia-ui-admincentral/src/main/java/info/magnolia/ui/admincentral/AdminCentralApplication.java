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

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;


/**
 * The Application's "main" class.
 */
public class AdminCentralApplication extends Application implements HttpServletRequestListener {

    private static final Logger log = LoggerFactory.getLogger(AdminCentralApplication.class);

    private static final Boolean isDeviceOverrideTablet = true;

    private Window window;
    
    private String contextPath = "";

    public boolean getIsDeviceOverrideTablet() {
        return isDeviceOverrideTablet;
    }

    @Override
    public void init() {
        log.debug("Init AdminCentralApplication...");
        setTheme("admincentraltheme");

        log.debug("Read component configurations from module descriptors...");
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = Components.getComponent(ModuleRegistry.class).getModuleDefinitions();
        ComponentProviderConfiguration admincentralConfig = configurationBuilder.getComponentsFromModules("admincentral", moduleDefinitions);

        ComponentProviderConfiguration configuration = admincentralConfig.clone();
        configuration.addComponent(InstanceConfiguration.valueOf(Application.class, this));

        log.debug("Creating the component provider...");
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) Components.getComponentProvider());
        GuiceComponentProvider componentProvider = builder.build();
        final MagnoliaShellPresenter presenter = componentProvider.newInstance(MagnoliaShellPresenter.class);

        window = new Window("Magnolia 5.0");
        window.setContent(((MagnoliaShellView) presenter.start()).asVaadinComponent());

        setMainWindow(window);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (request.getContextPath() != null) {
            this.contextPath = request.getContextPath();
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {

    }
    
    public String getAdminCentralPath() {
        return this.contextPath;
    }
}
