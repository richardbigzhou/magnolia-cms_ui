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
package info.magnolia.ui.imageprovider;

import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;

/**
 * This abstract implementation of {@link ImageProvider} covers resolution of icon class names from the Magnolia icon-font,
 * as well as "translating" arbitrary itemIds to proper Vaadin {@link Item Items}, through the {@link ContentConnector}.
 *
 * @see DefaultImageProvider
 */
public abstract class AbstractImageProvider implements ImageProvider {

    private final ContentConnector contentConnector;

    protected AbstractImageProvider(ContentConnector contentConnector) {
        this.contentConnector = contentConnector;
    }

    /**
     * Resolves the image {@link com.vaadin.server.Resource Resource} or icon class name, as expected by {@link #getThumbnailResource(Object, String)}.
     *
     * @see ImageProvider#getThumbnailResource(Object, String)
     */
    protected abstract Object resolveImageResource(Item item, String generator);

    /**
     * Resolves the link to a preview image for the given content and {@link info.magnolia.imaging.ImageGenerator ImageGenerator}.
     *
     * @see ImageProvider#getPortraitPath(Object)
     * @see ImageProvider#getThumbnailPath(Object)
     */
    protected abstract String resolveImagePath(Item item, String generator);

    @Override
    public String getPortraitPath(Object itemId) {
        Item item = contentConnector.getItem(itemId);
        return resolveImagePath(item, PORTRAIT_GENERATOR);
    }

    @Override
    public String getThumbnailPath(Object itemId) {
        Item item = contentConnector.getItem(itemId);
        return resolveImagePath(item, THUMBNAIL_GENERATOR);
    }

    @Override
    public Object getThumbnailResource(Object itemId, String generator) {
        Item item = contentConnector.getItem(itemId);
        return resolveImageResource(item, generator);
    }

    @Override
    public String resolveIconClassName(String mimeType) {
        String fileType = resolveFileTypeFromMimeType(mimeType);

        if (!"".equals(fileType)) {
            return "file-" + fileType;
        }

        return "file";
    }

    private String resolveFileTypeFromMimeType(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return StringUtils.EMPTY;
        }
        if (mimeType.contains("application/pdf")) {
            return "pdf";
        }
        if (mimeType.matches("application.*(msword)")) {
            return "word";
        }
        if (mimeType.matches("application.*(excel|xls)")) {
            return "excel";
        }
        if (mimeType.matches("application.*(powerpoint)")) {
            return "powerpoint";
        }
        if (mimeType.contains("text/")) {
            return "text";
        }
        if (mimeType.contains("image/")) {
            return "image";
        }
        if (mimeType.contains("video/")) {
            return "video";
        }
        if (mimeType.contains("audio/")) {
            return "audio";
        }
        if (mimeType.matches(".*(zip|compress)")) {
            return StringUtils.EMPTY;
        }

        return StringUtils.EMPTY;
    }

    protected boolean isImage(String mimeType) {
        return mimeType != null && mimeType.matches("image.*");
    }
}
