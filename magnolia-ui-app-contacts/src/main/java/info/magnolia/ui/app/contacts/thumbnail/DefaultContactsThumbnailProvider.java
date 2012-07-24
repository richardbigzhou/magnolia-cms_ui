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
package info.magnolia.ui.app.contacts.thumbnail;

import info.magnolia.cms.util.ContentUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailUtility;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thumbnail provider operating on contacts.
 * Will create and store thumbnail if none is around.
 */
public class DefaultContactsThumbnailProvider implements ThumbnailProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultContactsThumbnailProvider.class);

    final static String PHOTO_NODE_NAME = "photo";
    final static String THUMBNAIL_NODE_NAME = "thumbnail";
    private String format;

    private float quality;

    @Override
    public URL getThumbnail(Node contactNode, int width, int height) {

        URL url = null;
        try {
            if (!contactNode.hasNode(THUMBNAIL_NODE_NAME)) {
                // no thumbnail around create and store it
                InputStream stream = contactNode.getNode(PHOTO_NODE_NAME).getProperty(JcrConstants.JCR_DATA).getStream();

                BufferedImage thumbnail = null;
                try {
                    byte[] array = new byte[stream.available()];
                    stream.read(array);
                    stream.close();

                    final Image contactImage = Toolkit.getDefaultToolkit().createImage(array);
                    thumbnail = ThumbnailUtility.createThumbnail(contactImage, format, width, height, quality);
                    final Node thumbnailNode = contactNode.addNode(THUMBNAIL_NODE_NAME);

                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(thumbnail, format, baos);
                    final InputStream is = new ByteArrayInputStream(baos.toByteArray());

                    thumbnailNode.setProperty(JcrConstants.JCR_DATA, is);
                    contactNode.getSession().save();
                } catch (IOException e) {
                    log.warn("Error creating thumbnail image!", e);
                    return url;
                }
            }

        } catch (RepositoryException e) {
            log.warn("Could read image from contactNode:", e);
            return url;
        }

        try {
            final String link = LinkUtil.createLink(ContentUtil.asContent(contactNode.getNode(THUMBNAIL_NODE_NAME)));
            url =  new URL(link);
        } catch (MalformedURLException e) {
            log.warn("Couldn't create URL", e);
        } catch (RepositoryException e) {
            log.warn("Problems accessing jcr repository", e);
        }

        return url;
    }

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
