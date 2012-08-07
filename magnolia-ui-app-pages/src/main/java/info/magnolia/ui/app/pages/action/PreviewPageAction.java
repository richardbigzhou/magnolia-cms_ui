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


import com.google.inject.Inject;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.pageexport.renderer.Renderer;
import info.magnolia.module.pageexport.renderer.definition.RendererDefinition;
import info.magnolia.module.pageexport.renderer.registry.RendererRegistry;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.thumbnail.AbstractThumbnailProvider;

import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * The Class PreviewPageAction. Opens a full screen preview of the selected page.
 */
public class PreviewPageAction extends ActionBase<PreviewPageActionDefinition> {

    private final Node nodeToPreview;
    private static final Logger log = LoggerFactory.getLogger(PreviewPageAction.class);

    private LocationController locationController;

    private static final String TOKEN = "preview";
    private static final String IMAGE_NODE_NAME = AbstractThumbnailProvider.ORIGINAL_IMAGE_NODE_NAME;
    private RendererRegistry registry;

    private static final String IMAGE_TYPE = "png";

    //private PngRenderer renderer;

    /**
     * Instantiates a new preview page action.
     *
     * @param definition the definition
     * @param nodeToPreview the node to preview
     */
    @Inject
    public PreviewPageAction(PreviewPageActionDefinition definition, RendererRegistry registry, LocationController locationController, Node nodeToPreview) {

        super(definition);
        this.registry = registry;
        this.locationController = locationController;
        this.nodeToPreview = nodeToPreview;
    }


    @Override
    public void execute() throws ActionExecutionException {
        try {
            final String path = nodeToPreview.getPath();
            locationController.goTo(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", TOKEN + ":" + path));
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }

        User user = MgnlContext.getUser();

        try {
            RendererDefinition definition = registry.getDefinition(IMAGE_TYPE);
            Renderer renderer = registry.getRenderer(definition);

            InputStream is = renderer.render(nodeToPreview.getPath(), user);
            saveImage(nodeToPreview, is, definition.getContentType(), definition.getName());

        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void saveImage(Node node, InputStream inputStream, String contentType, String extension) throws RepositoryException, IOException {

        String fileName = node.getName();
        Node child;
        if (node.hasNode(IMAGE_NODE_NAME)) {
            child = node.getNode(IMAGE_NODE_NAME);
        } else {
            child = node.addNode(IMAGE_NODE_NAME, MgnlNodeType.NT_RESOURCE);
        }


        BinaryImpl binaryImpl = new BinaryImpl(inputStream);



        child.setProperty(MgnlNodeType.JCR_DATA, binaryImpl);

        child.setProperty(FileProperties.PROPERTY_FILENAME, fileName);
        child.setProperty(FileProperties.PROPERTY_CONTENTTYPE, contentType);
        child.setProperty(FileProperties.PROPERTY_EXTENSION, extension);

        child.setProperty(FileProperties.PROPERTY_LASTMODIFIED, new GregorianCalendar(TimeZone.getDefault()));

        child.setProperty(FileProperties.PROPERTY_SIZE, binaryImpl.getSize());

        ImageSize imageSize = ImageSize.valueOf(binaryImpl.getStream());
        child.setProperty(FileProperties.PROPERTY_WIDTH, imageSize!=null ? imageSize.getWidth():150);
        child.setProperty(FileProperties.PROPERTY_HEIGHT, imageSize!=null ? imageSize.getHeight():150);
        child.getSession().save();

    }

}
