/**
 * This file Copyright (c) 2003-2015 Magnolia International
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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.InputStream;
import java.util.Calendar;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.vaadin.data.Item;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

/**
 * This implementation of image provider, is able to provide portrait or thumbnail images only for objects of mime
 * type image/*. It will not store preview of image under the image node itself, instead it relies on imaging module
 * to generate and store the preview.
 */
public class DefaultImageProvider implements ImageProvider {

    private ImageProviderDefinition definition;

    private ContentConnector contentConnector;

    @Inject
    public DefaultImageProvider(ImageProviderDefinition definition, ContentConnector contentConnector) {
        this.definition = definition;
        this.contentConnector = contentConnector;
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultImageProvider.class);

    public final String ICON_CLASS_DEFAULT = "file";

    @Override
    public String getPortraitPath(Object itemId) {
        Item item = contentConnector.getItem(itemId);
        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrAdapter = (JcrNodeAdapter) item;
            return getGeneratorImagePath(jcrAdapter.getWorkspace(), jcrAdapter.getJcrItem(), PORTRAIT_GENERATOR);
        }
        log.debug("DefaultImageProvider works with info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter only.");
        return null;
    }

    @Override
    public String getThumbnailPath(Object itemId) {
        Item item = contentConnector.getItem(itemId);
        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrAdapter = (JcrNodeAdapter) item;
            Node node = jcrAdapter.getJcrItem();
            return getGeneratorImagePath(jcrAdapter.getWorkspace(), node, THUMBNAIL_GENERATOR);
        }
        log.debug("DefaultImageProvider works with info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter only.");
        return null;
    }

    protected String getGeneratorImagePath(String workspace, Node node, String generator) {
        String imagePath = null;

        if (node != null) {
            try {
                Node imageNode = node.getNode(definition.getOriginalImageNodeName());

                String imageName;
                if (imageNode.hasProperty(FileProperties.PROPERTY_FILENAME)) {
                    imageName = imageNode.getProperty(FileProperties.PROPERTY_FILENAME).getString();
                } else {
                    imageName = node.getName();
                }
                imagePath = MgnlContext.getContextPath() + "/" + definition.getImagingServletPath() + "/" + generator + "/" + workspace + "/" + imageNode.getIdentifier() + "/" + imageName + "." + definition.getImageExtension();

                // Add cache fingerprint so that browser caches asset only until asset is modified.
                Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
                imagePath = LinkUtil.addFingerprintToLink(imagePath, lastModified);

            } catch (RepositoryException e) {
                log.warn("Could not get name or identifier from imageNode: {}", e.getMessage());
            }
        }

        return imagePath;
    }

    @Override
    public Object getThumbnailResource(Object itemId, String generator) {
        Item item = contentConnector.getItem(itemId);
        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrAdapter = (JcrNodeAdapter) item;
            Node node = jcrAdapter.getJcrItem();

            Object resource = null;
            if (node != null) {
                resource = getThumbnailResource(node, jcrAdapter.getWorkspace(), generator);
            }
            return resource;
        }
        return null;
    }

    private Object getThumbnailResource(Node node, String workspace, String generator) {
        Object resource = null;
        try {
            final Node imageNode = node.getNode(definition.getOriginalImageNodeName());
            String mimeType = imageNode.getProperty(FileProperties.PROPERTY_CONTENTTYPE).getString();

            if (isImage(mimeType)) {
                if(MediaType.SVG_UTF_8.is(MediaType.parse(mimeType))) {
                    ImageStreamSource iss = new ImageStreamSource(imageNode.getIdentifier(), workspace);
                    // By default a StreamResource is cached for one year - filename contains the last modified date so that image is cached by the browser until changed.
                    String filename = imageNode.getIdentifier() + LastModified.getLastModified(imageNode).getTimeInMillis();
                    StreamResource streamResource = new StreamResource(iss, filename);
                    streamResource.setMIMEType(mimeType);
                    resource = streamResource;
                } else {
                    String path = getGeneratorImagePath(workspace, node, generator);
                    if (StringUtils.isNotBlank(path)) {
                        resource = new ExternalResource(path, MediaType.PNG.toString());
                    }
                }
            } else {
                resource = createIconFontResource(mimeType);
            }
        } catch (RepositoryException e) {
            log.debug("Could not get name or identifier from imageNode: {}", e.getMessage());
        }
        return resource;
    }

    /**
     * Create a Icon Resource.
     */
    private String createIconFontResource(String mimeType) {
        return resolveIconClassName(mimeType);
    }

    /**
     * Simple MimeType to Icon Class Mapping.
     */
    @Override
    public String resolveIconClassName(String mimeType) {

        String fileType = resolveFileTypeFromMimeType(mimeType);

        if (!"".equals(fileType)) {
            return "file-" + fileType;
        }

        return ICON_CLASS_DEFAULT;
    }

    /**
     * Simple MimeType to FileType Mapping.
     */
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

    private static class ImageStreamSource implements StreamSource {
        private String id;
        private String workspace;

        public ImageStreamSource(String id, String workspace) {
            this.id = id;
            this.workspace = workspace;
        }

        @Override
        public InputStream getStream() {
            try {
                Node node = NodeUtil.getNodeByIdentifier(workspace, id);
                if(node != null && node.hasProperty(JcrConstants.JCR_DATA)) {
                    return node.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
