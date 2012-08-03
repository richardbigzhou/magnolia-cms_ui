/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.app.pages.thumbnail;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.model.thumbnail.AbstractThumbnailProvider;
import info.magnolia.ui.model.thumbnail.ThumbnailUtility;

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

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PagesThumbnailProvider.
 */
public class PagesThumbnailProvider extends AbstractThumbnailProvider {

    private static final Logger log = LoggerFactory.getLogger(PagesThumbnailProvider.class);

    final static String PHOTO_NODE_NAME = "photo";

    @Override
    public String getPath(String nodeIdentifier, String workspace, int width, int height) {

        String path = null;
        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            final Node pageNode = session.getNodeByIdentifier(nodeIdentifier);
            if (!pageNode.hasNode(THUMBNAIL_NODE_NAME)) {
                // no thumbnail around create and store it
                if(!pageNode.hasNode(PHOTO_NODE_NAME)) {
                    return null;
                }
                Node photoNode = pageNode.getNode(PHOTO_NODE_NAME);
                final InputStream stream = photoNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();

                BufferedImage thumbnail = null;
                try {
                    byte[] array = new byte[stream.available()];
                    stream.read(array);
                    stream.close();

                    Image contactImage = Toolkit.getDefaultToolkit().createImage(array);
                    contactImage = new ImageIcon(contactImage).getImage();
                    thumbnail = createThumbnail(contactImage, getFormat(), width, height, getQuality());
                    final Node thumbnailNode = pageNode.addNode(THUMBNAIL_NODE_NAME, MgnlNodeType.NT_RESOURCE);
                    thumbnailNode.setProperty(FileProperties.PROPERTY_FILENAME, photoNode.getProperty(FileProperties.PROPERTY_FILENAME).getString());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_SIZE, photoNode.getProperty(FileProperties.PROPERTY_SIZE).getString());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_EXTENSION, getFormat());

                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(thumbnail, getFormat(), baos);
                    final InputStream is = new ByteArrayInputStream(baos.toByteArray());

                    thumbnailNode.setProperty(JcrConstants.JCR_DATA, is);
                    pageNode.getSession().save();
                    path = LinkUtil.createLink(ContentUtil.asContent(pageNode).getNodeData(THUMBNAIL_NODE_NAME));
                } catch (IOException e) {
                    log.warn("Error creating thumbnail image!", e);
                    return path;
                }
            }
        } catch (RepositoryException e) {
            log.warn("Could read image from contactNode:", e);
            return path;
        } catch (LinkException e) {
            log.warn("Error creating Link", e);
        }
        return path;
    }

    @Override
    protected BufferedImage createThumbnail(Image image, String format, int width, int height, float quality) throws IOException {
        return ThumbnailUtility.createThumbnail(image, format, width, height, quality);
    }

}
