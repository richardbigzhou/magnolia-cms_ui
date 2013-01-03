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
package info.magnolia.ui.admincentral.image;

import java.util.Calendar;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.LinkUtilAsset;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.terminal.IconFontResource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;

/**
 * Superclass for all thumbnail providers.
 */
public class DefaultImageProvider implements ImageProvider {

    private ImageProviderDefinition definition;

    public DefaultImageProvider(ImageProviderDefinition definition) {
        this.definition = definition;
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultImageProvider.class);

    private final String ICON_CLASS_DEFAULT = "file";

    @Override
    public String getPortraitPath(final String workspace, final String path) {
        Node node = SessionUtil.getNode(workspace, path);

        return getGeneratorImagePath(workspace, node, PORTRAIT_GENERATOR);
    }

    @Override
    public String getThumbnailPath(final String workspace, final String path) {
        Node node = SessionUtil.getNode(workspace, path);

        return getGeneratorImagePath(workspace, node, THUMBNAIL_GENERATOR);
    }

    @Override
    public String getPortraitPathByIdentifier(String workspace, String identifier) {
        Node node = SessionUtil.getNodeByIdentifier(workspace, identifier);

        return getGeneratorImagePath(workspace, node, PORTRAIT_GENERATOR);
    }

    @Override
    public String getThumbnailPathByIdentifier(String workspace, String identifier) {
        Node node = SessionUtil.getNodeByIdentifier(workspace, identifier);

        return getGeneratorImagePath(workspace, node, THUMBNAIL_GENERATOR);
    }

    private String getGeneratorImagePath(String workspace, Node node, String generator) {
        String imagePath = null;

        if (node != null) {
            try {
                Node imageNode = node.getNode(definition.getOriginalImageNodeName());

                String imageName;
                if (imageNode.hasProperty("fileName")) {
                    imageName = imageNode.getProperty("fileName").getString();
                } else {
                    imageName = node.getName();
                }

                imagePath = MgnlContext.getContextPath() + "/" + definition.getImagingServletPath() + "/" + generator + "/" + workspace + "/"
                        + imageNode.getIdentifier() + "/" + imageName + "." + definition.getImageExtension();

                // Add cache fingerprint so that browser caches asset only until asset is modified.
                Calendar lastModified = NodeTypes.LastModified.getLastModified(node);
                imagePath = LinkUtilAsset.addAssetCacheFingerprintToLink(imagePath, lastModified);

            } catch (RepositoryException e) {
                log.warn("Could not get name or identifier from imageNode: {}", e.getMessage());
            }
        }

        return imagePath;
    }

    @Override
    public Resource getThumbnailResourceByPath(String workspace, String path, String generator) {
        Resource resource = null;
        Node node = SessionUtil.getNode(workspace, path);
        if (node != null) {
            resource = getThumbnailResource(node, workspace, generator);
        }
        return resource;
    }

    @Override
    public Resource getThumbnailResourceById(String workspace, String identifier, String generator) {
        Resource resource = null;
        Node node = SessionUtil.getNodeByIdentifier(workspace, identifier);
        if (node != null) {
            resource = getThumbnailResource(node, workspace, generator);
        }
        return resource;
    }

    private Resource getThumbnailResource(Node node, String workspace, String generator) {
        Resource resource = null;

        try {
            Node imageNode = node.getNode(definition.getOriginalImageNodeName());
            String mimeType = imageNode.getProperty(FileProperties.PROPERTY_CONTENTTYPE).getString();
            if (isImage(mimeType)) {
                String path = getGeneratorImagePath(workspace, node, generator);
                if (StringUtils.isNotBlank(path)) {
                    resource = new ExternalResource(path, "image/png");
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
    private IconFontResource createIconFontResource(String mimeType) {
        IconFontResource img = new IconFontResource(resolveIconClassName(mimeType));
        return img;
    }

    /**
     * Simple MimeType to Icon Class Mapping.
     */
    private String resolveIconClassName(String mimeType) {
        if (mimeType.contains("application/pdf")) {
            return "file-pdf";
        }
        if (mimeType.matches("application.*(msword)")) {
            return "file-word";
        }
        if (mimeType.matches("application.*(excel|xls)")) {
            return "file-excel";
        }
        if (mimeType.matches("application.*(powerpoint)")) {
            return "file-powerpoint";
        }
        if (mimeType.contains("text/")) {
            return "file-text";
        }
        if (mimeType.contains("image/")) {
            return "file-image";
        }
        if (mimeType.contains("video/")) {
            return "file-video";
        }
        if (mimeType.contains("audio/")) {
            return "file-audio";
        }
        if (mimeType.matches(".*(zip|compress)")) {
            return "file";
        }

        return ICON_CLASS_DEFAULT;
    }

    private boolean isImage(String mimeType) {
        return mimeType != null && mimeType.matches("image.*");
    }
}
