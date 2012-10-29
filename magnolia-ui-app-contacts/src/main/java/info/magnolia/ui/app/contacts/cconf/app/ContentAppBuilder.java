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
package info.magnolia.ui.app.contacts.cconf.app;

import info.magnolia.ui.admincentral.app.content.ConfiguredContentAppDescriptor;
import info.magnolia.ui.app.contacts.cconf.workbench.WorkbenchBuilder;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;

/**
 * Builder used to build a content app descriptor.
 */
public class ContentAppBuilder {

    private ConfiguredContentAppDescriptor descriptor;

    public ContentAppBuilder() {
        descriptor = new ConfiguredContentAppDescriptor();
    }

    public ContentAppBuilder(String name) {
        descriptor = new ConfiguredContentAppDescriptor();
        descriptor.setName(name);
    }

    public ContentAppBuilder label(String label) {
        descriptor.setLabel(label);
        return this;
    }

    public ContentAppBuilder icon(String icon) {
        descriptor.setIcon(icon);
        return this;
    }

    public ContentAppBuilder appClass(Class<? extends info.magnolia.ui.framework.app.App> appClass) {
        descriptor.setAppClass(appClass);
        return this;
    }

    public ContentAppBuilder categoryName(String categoryName) {
        descriptor.setCategoryName(categoryName);
        return this;
    }

    public ContentAppBuilder enabled(boolean enabled) {
        descriptor.setEnabled(enabled);
        return this;
    }

    public WorkbenchBuilder workbench() {
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        descriptor.setWorkbench(workbenchDefinition);
        return new WorkbenchBuilder(workbenchDefinition);
    }

    public SubAppBuilder subApp(String name) {
        ConfiguredSubAppDescriptor subAppDescriptor = new ConfiguredSubAppDescriptor();
        subAppDescriptor.setName(name);
        descriptor.addSubApp(subAppDescriptor);
        return new SubAppBuilder(subAppDescriptor);
    }

    public AppDescriptor exec() {
        return descriptor;
    }
}
