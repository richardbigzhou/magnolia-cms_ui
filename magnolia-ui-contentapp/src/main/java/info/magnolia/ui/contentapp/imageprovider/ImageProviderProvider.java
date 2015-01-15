/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.contentapp.imageprovider;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;

/**
 * Provides the {@link ImageProvider} as configured under current sub-app descriptor.
 */
@Singleton
public class ImageProviderProvider implements Provider<ImageProvider> {

    private ComponentProvider componentProvider;

    private SubAppContext subAppContext;

    private ImageProvider imageProvider;

    @Inject
    public ImageProviderProvider(ComponentProvider componentProvider, SubAppContext subAppContext) {
        this.componentProvider = componentProvider;
        this.subAppContext = subAppContext;
    }

    @Override
    public ImageProvider get() {
        if (imageProvider == null) {
            ImageProviderDefinition definition = resolveImageProviderDefinition();
            if (definition != null) {
                imageProvider = componentProvider.newInstance(definition.getImageProviderClass(), definition);
            }
        }
        if (imageProvider == null) {
            imageProvider = new NullImageProvider();
        }
        return imageProvider;
    }

    protected ImageProviderDefinition resolveImageProviderDefinition() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();
        if (subAppDescriptor instanceof ContentSubAppDescriptor) {
            return ((ContentSubAppDescriptor) subAppDescriptor).getImageProvider();
        }
        return null;
    }

    /**
     * A void implementation of ImageProvider for those cases where imageProvider is not configured and cannot be injected otherwise.
     */
    private static class NullImageProvider implements ImageProvider {

        @Override
        public String getPortraitPath(Object itemId) {
            return null;
        }

        @Override
        public String getThumbnailPath(Object itemId) {
            return null;
        }

        @Override
        public String resolveIconClassName(String mimeType) {
            return null;
        }

        @Override
        public Object getThumbnailResource(Object itemId, String generator) {
            return null;
        }
    }

}
