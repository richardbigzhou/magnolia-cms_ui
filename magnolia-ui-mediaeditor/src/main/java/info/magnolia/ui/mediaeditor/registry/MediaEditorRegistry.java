/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.mediaeditor.registry;

import info.magnolia.config.registry.AbstractRegistry;
import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition;

import javax.inject.Singleton;

/**
 * {@link info.magnolia.config.registry.Registry} implementation for {@link MediaEditorDefinition} types.
 */
@Singleton
public class MediaEditorRegistry extends AbstractRegistry<MediaEditorDefinition> {

    /**
     * @deprecated since 5.4 - use the {@link #getProvider(String)} method instead and fetch definition from the result.
     */
    @Deprecated
    public MediaEditorDefinition get(String mediaEditorId) throws RegistrationException {
        return getProvider(mediaEditorId).get();
    }

    @Override
    protected String asReferenceString(DefinitionProvider<MediaEditorDefinition> provider) {
        final DefinitionMetadata md = provider.getMetadata();
        return md.getModule() + ":" + md.getRelativeLocation();
    }

    @Override
    public DefinitionType type() {
        return MediaEditorDefinitionType.TYPE;
    }
}
