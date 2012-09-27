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
import info.magnolia.jcr.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Superclass for all thumbnail providers.
 */
public class DefaultImageProvider implements ImageProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultImageProvider.class);

    private String originalImageNodeName = ORIGINAL_IMAGE_NODE_NAME;

    private String imagingServletPath = IMAGING_SERVLET_PATH;

    private String imageExtension = IMAGE_EXTENSION;

    private final String PORTRAIT_GENERATOR = "portrait";
    private final String THUMBNAIL_GENERATOR = "thumbnail";


    @Override
    public String getPortraitPath(final String path, final String workspace) {
        Node node = SessionUtil.getNode(workspace, path);

        return getGeneratorImagePath(workspace, node, PORTRAIT_GENERATOR);
    }

    @Override
    public String getThumbnailPath(final String path, final String workspace) {
        Node node = SessionUtil.getNode(workspace, path);

        return getGeneratorImagePath(workspace, node, THUMBNAIL_GENERATOR);
    }

    @Override
    public String getPortraitPathByUuid(String uuid, String workspace) {
        Node node = SessionUtil.getNodeByIdentifier(workspace, uuid);

        return getGeneratorImagePath(workspace, node, PORTRAIT_GENERATOR);
    }

    @Override
    public String getThumbnailPathByUuid(String uuid, String workspace) {
        Node node = SessionUtil.getNodeByIdentifier(workspace, uuid);

        return getGeneratorImagePath(workspace, node, THUMBNAIL_GENERATOR);
    }

    private String getGeneratorImagePath(String workspace, Node node, String generator) {
        String imagePath = null;

        if (node != null) {
            try {
                Node imageNode = node.getNode(originalImageNodeName);

                String imageName;
                if (imageNode.hasProperty("fileName")) {
                    imageName = imageNode.getProperty("fileName").getString();
                }
                else {
                    imageName = node.getName();
                }
                imagePath = MgnlContext.getContextPath() + "/" + imagingServletPath + "/" + generator + "/" + workspace + "/" + imageNode.getIdentifier() + "/" + imageName + "." + imageExtension;
            } catch (RepositoryException e) {
                log.warn("Could not get name or identifier from imageNode: {}", e.getMessage());
            }
        }

        return imagePath;
    }

    @Override
    public String getOriginalImageNodeName() {
        return originalImageNodeName;
    }

    @Override
    public void setOriginalImageNodeName(String originalImageNodeName) {
            this.originalImageNodeName = originalImageNodeName;
    }

    @Override
    public String getImagingServletPath() {
        return imagingServletPath;
    }

    @Override
    public void setImagingServletPath(String imagingServletPath) {
        this.imagingServletPath = imagingServletPath;
    }

    @Override
    public String getImageExtension() {
        return imageExtension;
    }

    @Override
    public void setImageExtension(String imageExtension) {
        this.imageExtension = imageExtension;
    }
}
