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


import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkUtil;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for all thumbnail providers.
 */
public abstract class AbstractThumbnailProvider implements ThumbnailProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractThumbnailProvider.class);

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

    private String originalImageNodeName = ORIGINAL_IMAGE_NODE_NAME;

    private String thumbnailNodeName = THUMBNAIL_NODE_NAME;
    /**
     * This method is capable of navigating and using a structure like the following:
     * <pre>
     *   /myNode [sometype]
     *   /myNode/originalImage [mgnl:resource]
     *   /myNode/thumbnail [mgnl:resource]
     * </pre>
     * where <code>originalImage</code> and <code>thumbnail</code> are subnodes whose default names are defined
     * by {@link #ORIGINAL_IMAGE_NODE_NAME} and {@link #THUMBNAIL_NODE_NAME}.<p>
     * If you want to store your binaries under different names you can override them via the provided setters.
     * If your node structure is different from the default one, you need to override this method.<p>
     * This method will also handle whether a thumbnail needs to be created for the very first time, regenerated in case the original image has been updated or can
     * just be retrieved from the JCR repository by using {@link ThumbnailUtility#isThumbnailToBeGenerated(String, String, String, String)}.
     * @return <code>null</code> if no link could be generated.
     * @see info.magnolia.ui.model.thumbnail.ThumbnailProvider#getPath(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public String getPath(final String nodeIdentifier, final String workspace, int width, int height) {
        String path = null;
        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            final Node imageNode = session.getNodeByIdentifier(nodeIdentifier);
            if (ThumbnailUtility.isThumbnailToBeGenerated(nodeIdentifier, workspace, getOriginalImageNodeName(), getThumbnailNodeName())) {
                extractImageAndCreateThumbnail(imageNode, width, height);
            }
            path = LinkUtil.createLink(ContentUtil.asContent(imageNode).getNodeData(getThumbnailNodeName()));
        } catch (RepositoryException e) {
            log.error("A repository exception occurred.", e);
            return null;
        } catch (LinkException e) {
            log.error("Error while creating link to thumbnail image.", e);
            return null;
        }
        return path;
    }

    protected abstract BufferedImage createThumbnail(final Image image, final String format, final int width, final int height, final float quality) throws IOException;

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

    @Override
    public String getOriginalImageNodeName() {
        return originalImageNodeName;
    }

    public void setOriginalImageNodeName(String originalImageNodeName) {
        if(StringUtils.isBlank(originalImageNodeName)) {
            log.warn("originalImageNodeName cannot be null or empty. Will leave default value");
        } else {
            this.originalImageNodeName = originalImageNodeName;
        }
    }

    @Override
    public String getThumbnailNodeName() {
        return thumbnailNodeName;
    }

    public void setThumbnailNodeName(String thumbnailNodeName) {
        if(StringUtils.isBlank(thumbnailNodeName)) {
            log.warn("thumbailNodeName cannot be null or empty. Will leave default value");
        } else {
            this.thumbnailNodeName = thumbnailNodeName;
        }
    }

    private void extractImageAndCreateThumbnail(final Node imageNode, int width, int height) throws RepositoryException {
        log.debug("Generating thumbnail for node at [{}}... ", imageNode.getPath());
        long start = System.currentTimeMillis();

        final Node originalImageNode = imageNode.getNode(getOriginalImageNodeName());
        final InputStream originalInputStream = originalImageNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
        ByteArrayOutputStream thumbnailOutputStream = null;
        InputStream thumbnailInputStream = null;

        BufferedImage thumbnail = null;
        try {
            byte[] array = new byte[originalInputStream.available()];
            originalInputStream.read(array);
            originalInputStream.close();

            Image thumbnailImage = Toolkit.getDefaultToolkit().createImage(array);
            thumbnailImage = new ImageIcon(thumbnailImage).getImage();
            thumbnail = createThumbnail(thumbnailImage, getFormat(), width, height, getQuality());

            if (imageNode.hasNode(getThumbnailNodeName())) {
                imageNode.getNode(getThumbnailNodeName()).remove();
            }
            final Node thumbnailNode = imageNode.addNode(getThumbnailNodeName(), MgnlNodeType.NT_RESOURCE);
            thumbnailNode.setProperty(FileProperties.PROPERTY_FILENAME, originalImageNode.getProperty(FileProperties.PROPERTY_FILENAME).getString());
            thumbnailNode.setProperty(FileProperties.PROPERTY_EXTENSION, getFormat());
            thumbnailNode.setProperty(FileProperties.PROPERTY_MIMETYPE, originalImageNode.getProperty(JcrConstants.JCR_MIMETYPE).getString());
            thumbnailNode.setProperty(FileProperties.PROPERTY_HEIGHT, thumbnail.getHeight());
            thumbnailNode.setProperty(FileProperties.PROPERTY_WIDTH, thumbnail.getWidth());

            thumbnailOutputStream = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, getFormat(), thumbnailOutputStream);
            final int size = thumbnailOutputStream.size();
            log.debug("thumbnail size is {} ", size);

            thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
            thumbnailNode.setProperty(JcrConstants.JCR_DATA, thumbnailInputStream);
            thumbnailNode.setProperty(FileProperties.PROPERTY_SIZE, size);

            imageNode.getSession().save();

            log.debug("thumbnail generated in {} ms", System.currentTimeMillis() - start);
        } catch (IOException e) {
            log.error("Error while creating thumbnail image.", e);
        } finally {
            IOUtils.closeQuietly(thumbnailOutputStream);
            IOUtils.closeQuietly(thumbnailInputStream);
            IOUtils.closeQuietly(originalInputStream);
        }
    }
}
