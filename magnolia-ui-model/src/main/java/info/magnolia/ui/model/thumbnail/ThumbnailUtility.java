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

import info.magnolia.context.MgnlContext;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Utility for creating Thumbnails.
 */
public class ThumbnailUtility {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailUtility.class);

    private ThumbnailUtility() {
    }

    /**
     * Create a Thumbnail image from the provided original. Thumbnail will have the indicated width, height and quality.
     */
    public static final BufferedImage createThumbnail(final Image original, final String format, final int width, final int height, final float quality) throws IOException {
        int thumbWidth = width;
        int thumbHeight = height;

        final double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        final double imageRatio = (double) original.getWidth(null) / (double) original.getHeight(null);

        // This calculation is used to convert the image size according to the pixels mentioned above
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        final BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);

        final Graphics2D graphics = thumbnail.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(original, 0, 0, thumbWidth, thumbHeight, null);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        final JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbnail);

        param.setQuality(quality, false);
        encoder.setJPEGEncodeParam(param);
        encoder.encode(thumbnail);

        return thumbnail;
    }

    /**
     * Checks whether this node contains an image which needs to be generated for the first time or regenerated.
     * @returns <code>true</code> if the thumbnail has to be created because not yet existing or recreated due to its original image having been updated, else <code>false</code>.
     * @throws RepositoryException
     */
    public static final boolean isThumbnailToBeGenerated(final String nodeIdentifier, final String workspace, final String originalNodeName, final String thumbnailNodeName) throws RepositoryException {

        final Session session = MgnlContext.getJCRSession(workspace);
        final Node node = session.getNodeByIdentifier(nodeIdentifier);

        if (!node.hasNode(originalNodeName)) {
            log.warn("No [{}] node found for contact node [{}]. Cannot create thumbnail.", originalNodeName, node.getPath());
            return false;
        }
        if (!node.hasNode(thumbnailNodeName)) {
            return true;
        }
        final Node originalImageNode = node.getNode(originalNodeName);
        final Node thumbnailNode = node.getNode(thumbnailNodeName);
        final Calendar originalImageDate = originalImageNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
        final Calendar thumbnailNodeDate = thumbnailNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
        if (originalImageDate.compareTo(thumbnailNodeDate) > 0) {
            log.debug("Original image date is {} - thumbnail date is {}. Recreating thumbnail for node [{}]...", new Object[]{ originalImageDate.getTime(), thumbnailNodeDate.getTime(), node.getPath()});
            // photo node must have been updated as its last mod date is after thumbnail last mod date
            return true;
        }
        return false;
    }

}
