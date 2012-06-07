/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.framework.app.registry;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.framework.app.AppDescriptor;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfiguredAppDescriptorProvider that instantiates an AppDescriptor from a configuration node.
 *
 * @version $Id$
 */
public class ConfiguredAppDescriptorProvider implements AppDescriptorProvider{

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private AppDescriptor appDescriptor;

    @SuppressWarnings("deprecation")
    public ConfiguredAppDescriptorProvider(Node configNode) throws Content2BeanException {
        super();
        Content content = ContentUtil.asContent(configNode);
        this.appDescriptor = (AppDescriptor) Content2BeanUtil.toBean(content, true, AppDescriptor.class);

        // Minimal check
        validate();
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

    public void validate() {
        if (StringUtils.isEmpty(appDescriptor.getCategoryName())) {
            appDescriptor.setCategoryName(DEFAULT_CATEGORY_NAME);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof AppDescriptorProvider) {
            AppDescriptorProvider other = (AppDescriptorProvider) o;
            String thisCompareToString = "";
            String otherCompareToString = "";
            try {
                thisCompareToString = getAppDescriptorProviderUniqueIdentifier(this.getAppDescriptor());
                otherCompareToString = getAppDescriptorProviderUniqueIdentifier(other.getAppDescriptor());
            }
            catch (RegistrationException e) {
                log.error("",e);
            }
            return thisCompareToString.equals(otherCompareToString);
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return getAppDescriptorProviderUniqueIdentifier(this.getAppDescriptor()).hashCode();
        }
        catch (RegistrationException e) {
            log.error("",e);
            return 0;
        }
    }


    /**
     * Used to define if an App was Change in config, and also if the changes made in config
     * needs a reload.
     */
    private String getAppDescriptorProviderUniqueIdentifier(AppDescriptor app) {
        return app.getName()+app.getCategoryName() + app.isEnabled()+app.getIcon()+app.getAppClass();
    }
}
