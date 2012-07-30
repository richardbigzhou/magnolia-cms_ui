/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.ui.model.thumbnail;


import javax.jcr.Node;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Superclass for all thumbnail providers.
 */
public abstract class AbstractThumbnailProvider implements ThumbnailProvider {
    public static final String THUMBNAIL_NODE_NAME = "thumbnail";
    public static final String DEFAULT_THUMBNAIL_FORMAT = "jpg";
    public static final float DEFAULT_THUMBNAIL_QUALITY = 0.75f;

    /**
     * image format for the thumbnails - jpg, png, ...
     */
    private String format = DEFAULT_THUMBNAIL_FORMAT;

    /**
     * Quality of the thumbnails.
     */
    private float quality = DEFAULT_THUMBNAIL_QUALITY;

    @Override
    public abstract String getPath(Node contactNode, int width, int height);

    protected abstract BufferedImage createThumbnail(final Image contactImage, final String format, final int width, final int height, final float quality) throws IOException;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }
}
