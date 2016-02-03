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

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.app.AppDescriptor;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfiguredAppDescriptorProvider that instantiates an AppDescriptor from a configuration node.
 * Overrides equals in order to define the uniqueness of a ConfiguredAppDescriptorProvider.
 * In our case, ConfiguredAppDescriptorProvider is equal if:
 * AppDescriptor.getName(), isEnabled(), getIcon(), getAppClass() and getLabel() are equal.
 *
 * @deprecated since 5.4 use DefinitionProvider<AppDescriptor> and configuration sources.
 */
@Deprecated
public class ConfiguredAppDescriptorProvider implements AppDescriptorProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final ConfiguredAppDescriptor appDescriptor;

    public ConfiguredAppDescriptorProvider(Node configNode) throws Node2BeanException, RepositoryException {
        super();
        this.appDescriptor = (ConfiguredAppDescriptor) Components.getComponent(Node2BeanProcessor.class).toBean(configNode, ConfiguredAppDescriptor.class);
    }

    @Override
    public String getName() {
        return appDescriptor.getName();
    }

    @Override
    public AppDescriptor getAppDescriptor() throws RegistrationException {
        return appDescriptor;
    }

    @Override
    public String toString() {
        return "ConfiguredAppDescriptorProvider [id=" + appDescriptor.getName() + ", appDescriptor=" + appDescriptor + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AppDescriptorProvider) {
            AppDescriptorProvider other = (AppDescriptorProvider) o;
            String thisCompareToString = "";
            String otherCompareToString = "";
            try {
                thisCompareToString = getAppDescriptorProviderUniqueIdentifier(this.getAppDescriptor());
                otherCompareToString = getAppDescriptorProviderUniqueIdentifier(other.getAppDescriptor());
            } catch (RegistrationException e) {
                log.error("", e);
            }
            return thisCompareToString.equals(otherCompareToString);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return getAppDescriptorProviderUniqueIdentifier(this.getAppDescriptor()).hashCode();
        } catch (RegistrationException e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * Used to define if an app was changed in config, and also if the changes made in config needs a reload.
     */
    private String getAppDescriptorProviderUniqueIdentifier(AppDescriptor app) {
        return app.getName() + app.isEnabled() + app.getIcon() + app.getAppClass() + app.getLabel();
    }
}
