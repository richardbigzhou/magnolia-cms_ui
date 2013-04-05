/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.contentapp.config;

import info.magnolia.ui.actionbar.config.ActionbarBuilder;
import info.magnolia.ui.contentapp.detail.ConfiguredDetailSubAppDescriptor;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.builder.ActionBuilder;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;

/**
 * Builds a {@link info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor} used for configuring a {@link info.magnolia.ui.contentapp.detail.DetailSubApp}.
 */
public class DetailSubAppBuilder {
    private ConfiguredDetailSubAppDescriptor descriptor = new ConfiguredDetailSubAppDescriptor();

    public DetailSubAppBuilder(String name) {
        this.descriptor.setName(name);
    }

    public DetailSubAppBuilder subAppClass(Class<? extends SubApp> subAppClass) {
        descriptor.setSubAppClass(subAppClass);
        return this;
    }

    public DetailSubAppBuilder label(String label) {
        descriptor.setLabel(label);
        return this;
    }

    public DetailSubAppBuilder editor(EditorBuilder builder) {
        descriptor.setEditor(builder.exec());
        return this;
    }


    public DetailSubAppBuilder actions(ActionBuilder... builders) {
        for (ActionBuilder builder : builders) {
            descriptor.getActions().put(builder.getName(), builder.exec());
        }
        return this;
    }

    public DetailSubAppBuilder actions(ActionDefinition... definitions) {
        for (ActionDefinition definition : definitions) {
            descriptor.getActions().put(definition.getName(), definition);
        }
        return this;
    }

    public DetailSubAppBuilder actionbar(ActionbarBuilder builder) {
        descriptor.setActionbar(builder.exec());
        return this;
    }

    public DetailSubAppBuilder imageProvider(ImageProviderDefinition imageProvider) {
        descriptor.setImageProvider(imageProvider);
        return this;
    }

    public SubAppDescriptor exec() {
        return descriptor;
    }
}
