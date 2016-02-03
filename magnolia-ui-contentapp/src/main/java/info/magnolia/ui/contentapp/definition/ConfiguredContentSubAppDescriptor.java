/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.contentapp.definition;

import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;

/**
 * Simple implementation for {@link ContentSubAppDescriptor}.
 *
 * @see ContentSubAppDescriptor
 */
public class ConfiguredContentSubAppDescriptor extends ConfiguredSubAppDescriptor implements ContentSubAppDescriptor {

    private ActionbarDefinition actionbar;

    private ImageProviderDefinition imageProvider;

    private ContentConnectorDefinition contentConnectorDefinition;

    public ConfiguredContentSubAppDescriptor() {
        setContentConnector(new ConfiguredJcrContentConnectorDefinition());
    }

    @Override
    public ActionbarDefinition getActionbar() {
        return actionbar;
    }

    public void setActionbar(ActionbarDefinition actionbar) {
        this.actionbar = actionbar;
    }

    @Override
    public ImageProviderDefinition getImageProvider() {
        return imageProvider;
    }

    @Override
    public ContentConnectorDefinition getContentConnector() {
        return contentConnectorDefinition;
    }

    public void setImageProvider(ImageProviderDefinition imageProvider) {
        this.imageProvider = imageProvider;
    }

    public void setContentConnector(ContentConnectorDefinition contentConnectorDefinition) {
        this.contentConnectorDefinition = contentConnectorDefinition;
    }
}
