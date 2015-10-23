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
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.link.LinkUtil;
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
 * This implementation of {@link ImageProvider} provides portrait or thumbnail images for JCR-based content.
 * <p>
 * It expects a given Node to have a binary sub-node named according to {@link ImageProviderDefinition#getOriginalImageNodeName()},
 * and will only resolve images when the binary is of mime-type image/*.
 * It also relies on imaging module to generate and store the preview images.
 *
 * @see ImageProviderDefinition
 */
public class DefaultImageProvider extends AbstractImageProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultImageProvider.class);

    private final ImageProviderDefinition definition;

    @Inject
    public DefaultImageProvider(ImageProviderDefinition definition, ContentConnector contentConnector) {
        super(contentConnector);
        this.definition = definition;
    }

    @Override
    protected String resolveImagePath(Item item, String generator) {
        String imagePath = null;

        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrAdapter = (JcrNodeAdapter) item;
            Node node = jcrAdapter.getJcrItem();
            String workspace = jcrAdapter.getWorkspace();

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
        } else {
            log.debug("DefaultImageProvider works with info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter only.");
        }

        return imagePath;
    }

    @Override
    protected Object resolveImageResource(Item item, String generator) {
        Object resource = null;

        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrAdapter = (JcrNodeAdapter) item;
            Node node = jcrAdapter.getJcrItem();
            String workspace = jcrAdapter.getWorkspace();

            try {
                final Node imageNode = node.getNode(definition.getOriginalImageNodeName());
                String mimeType = imageNode.getProperty(FileProperties.PROPERTY_CONTENTTYPE).getString();

                if (isImage(mimeType)) {
                    if (MediaType.SVG_UTF_8.is(MediaType.parse(mimeType))) {
                        ImageStreamSource iss = new ImageStreamSource(imageNode.getIdentifier(), workspace);
                        // By default a StreamResource is cached for one year - filename contains the last modified date so that image is cached by the browser until changed.
                        String filename = imageNode.getIdentifier() + LastModified.getLastModified(imageNode).getTimeInMillis();
                        StreamResource streamResource = new StreamResource(iss, filename);
                        streamResource.setMIMEType(mimeType);
                        resource = streamResource;
                    } else {
                        String path = resolveImagePath(item, generator);
                        if (StringUtils.isNotBlank(path)) {
                            resource = new ExternalResource(path, MediaType.PNG.toString());
                        }
                    }
                } else {
                    resource = resolveIconClassName(mimeType);
                }
            } catch (RepositoryException e) {
                log.debug("Could not get name or identifier from imageNode: {}", e.getMessage());
            }
        } else {
            log.debug("DefaultImageProvider works with info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter only.");
        }

        return resource;
    }

    private static class ImageStreamSource implements StreamSource {
        private final String id;
        private final String workspace;

        public ImageStreamSource(String id, String workspace) {
            this.id = id;
            this.workspace = workspace;
        }

        @Override
        public InputStream getStream() {
            try {
                Node node = NodeUtil.getNodeByIdentifier(workspace, id);
                if (node != null && node.hasProperty(JcrConstants.JCR_DATA)) {
                    return node.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
