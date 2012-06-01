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
package info.magnolia.ui.admincentral.app.registry;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.app.AppDescriptor;

/**
 * ConfiguredAppDescriptorProvider that instantiates an AppDescriptor from a configuration node.
 *
 * @version $Id$
 */
public class ConfiguredAppDescriptorProvider implements AppDescriptorProvider {

    private AppDescriptor appDescriptor;

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

    public String toString() {
        return "ConfiguredAppDescriptorProvider [id=" + appDescriptor.getName() + ", appDescriptor=" + appDescriptor + "]";
    }

    public void validate() {
        if (StringUtils.isEmpty(appDescriptor.getCategoryName())) {
            appDescriptor.setCategoryName(DEFAULT_CATEGORY_NAME);
        }
    }
}
