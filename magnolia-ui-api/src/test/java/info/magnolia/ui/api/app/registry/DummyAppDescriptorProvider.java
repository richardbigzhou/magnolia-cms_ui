/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionMetadataBuilder;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.config.source.ConfigurationSourceType;
import info.magnolia.ui.api.app.AppDescriptor;

import java.util.List;

public class DummyAppDescriptorProvider implements DefinitionProvider<AppDescriptor> {

    private final DefinitionMetadata metadata;
    private final AppDescriptor appDescriptor;
    private final boolean valid;

    public DummyAppDescriptorProvider(String appName, String moduleName, String relativeLocation, AppDescriptor appDescriptor) {
        this(appName, moduleName, relativeLocation, appDescriptor, true);
    }

    public DummyAppDescriptorProvider(String appName, String moduleName, String relativeLocation, AppDescriptor appDescriptor, boolean valid) {
        this.metadata = DefinitionMetadataBuilder.usingNameAsId().type(DefinitionTypes.APP).name(appName).module(moduleName).relativeLocation(relativeLocation).build();
        this.appDescriptor = appDescriptor;
        this.valid = valid;
    }

    @Override
    public ConfigurationSourceType getSourceType() {
        return null;
    }

    @Override
    public DefinitionMetadata getMetadata() {
        return metadata;
    }

    @Override
    public AppDescriptor get() {
        return appDescriptor;
    }

    @Override
    public DefinitionRawView getRaw() {
        throw new IllegalStateException("not implemented yet");
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public List<String> getErrorMessages() {
        throw new IllegalStateException("not implemented yet");
    }
}
