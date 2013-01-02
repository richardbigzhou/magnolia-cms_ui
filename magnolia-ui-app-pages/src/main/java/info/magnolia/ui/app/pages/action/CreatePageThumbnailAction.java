/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.pages.action;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.pageexport.renderer.Renderer;
import info.magnolia.module.pageexport.renderer.definition.RendererDefinition;
import info.magnolia.module.pageexport.renderer.registry.RendererRegistry;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CreatePageThumbnailAction.
 */
public class CreatePageThumbnailAction extends ActionBase<CreatePageThumbnailActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PreviewPageAction.class);

    private RendererRegistry registry;
    private Node nodeToExport;

    private static final String IMAGE_NODE_NAME = ImageProviderDefinition.ORIGINAL_IMAGE_NODE_NAME;

    public CreatePageThumbnailAction(CreatePageThumbnailActionDefinition definition, RendererRegistry registry, Node nodeToExport) {
        super(definition);
        this.registry = registry;
        this.nodeToExport = nodeToExport;
    }

    @Override
    public void execute() throws ActionExecutionException {

        User user = MgnlContext.getUser();

        try {
            RendererDefinition definition = registry.getDefinition(getDefinition().getExportType());
            Renderer renderer = registry.getRenderer(definition);

            InputStream is = renderer.render(nodeToExport.getPath(), user);
            saveImage(nodeToExport, is, definition.getContentType(), definition.getName());
            is.close();

        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Save the image to jcr.
     * 
     * @param inputStream
     *            containing the image. Caution: it'll not be closed in here.
     */
    private void saveImage(Node node, InputStream inputStream, String contentType, String extension) throws RepositoryException, IOException {

        String fileName = node.getName();
        Node child;
        if (node.hasNode(IMAGE_NODE_NAME)) {
            child = node.getNode(IMAGE_NODE_NAME);
        } else {
            child = node.addNode(IMAGE_NODE_NAME, NodeTypes.Resource.NAME);
        }

        final Session session = node.getSession();
        final Binary binary = session.getValueFactory().createBinary(inputStream);

        child.setProperty(JcrConstants.JCR_DATA, binary);

        child.setProperty(FileProperties.PROPERTY_FILENAME, fileName);
        child.setProperty(FileProperties.PROPERTY_CONTENTTYPE, contentType);
        child.setProperty(FileProperties.PROPERTY_EXTENSION, extension);

        child.setProperty(FileProperties.PROPERTY_LASTMODIFIED, new GregorianCalendar(TimeZone.getDefault()));

        child.setProperty(FileProperties.PROPERTY_SIZE, binary.getSize());

        final InputStream streamFromBinary = binary.getStream();
        ImageSize imageSize = ImageSize.valueOf(streamFromBinary);

        child.setProperty(FileProperties.PROPERTY_WIDTH, imageSize == null ? 150 : imageSize.getWidth());
        child.setProperty(FileProperties.PROPERTY_HEIGHT, imageSize == null ? 150 : imageSize.getHeight());

        child.getSession().save();
        streamFromBinary.close();
        binary.dispose();
    }
}
