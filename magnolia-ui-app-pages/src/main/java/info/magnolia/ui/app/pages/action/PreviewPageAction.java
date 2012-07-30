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

import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.pageexport.http.MgnlHttpClient;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
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

    private MgnlHttpClient client;

    final static String PHOTO_NODE_NAME = "photo";

    /**
     * Instantiates a new preview page action.
     *
     * @param definition the definition
     * @param nodeToPreview the node to preview
     */
    @Inject
    public PreviewPageAction(PreviewPageActionDefinition definition, MgnlHttpClient client, LocationController locationController, Node nodeToPreview) {

        super(definition);
        this.locationController = locationController;
        this.nodeToPreview = nodeToPreview;
        this.client = client;

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
        client.initCredentials(user);

        System.out.println("preview page should open full screen preview.");
        try {
            System.out.println(nodeToPreview.getPath());
            String url = "http://localhost:8080" + nodeToPreview.getPath();
            client.setUri(url);
            client.addParameter("exportType", "png");
            InputStream is = client.get();
            saveImage(nodeToPreview, is);

        } catch (RepositoryException e) {
            System.err.println("ERROR GETTING NODE PATH");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void saveImage(Node node, InputStream inputStream) throws RepositoryException, IOException {

        String fileName = node.getName();
        Node child;
        if (node.hasNode(PHOTO_NODE_NAME)) {
            child = node.getNode(PHOTO_NODE_NAME);
        } else {
            child = node.addNode(PHOTO_NODE_NAME, MgnlNodeType.NT_RESOURCE);
        }


        BinaryImpl binaryImpl = new BinaryImpl(inputStream);



        child.setProperty(MgnlNodeType.JCR_DATA, binaryImpl);

        child.setProperty(FileProperties.PROPERTY_FILENAME, fileName);
        child.setProperty(FileProperties.PROPERTY_CONTENTTYPE, "image/png");
        child.setProperty(FileProperties.PROPERTY_EXTENSION, "png");

        child.setProperty(FileProperties.PROPERTY_LASTMODIFIED, new GregorianCalendar(TimeZone.getDefault()));

        child.setProperty(FileProperties.PROPERTY_SIZE, binaryImpl.getSize());

        ImageSize imageSize = ImageSize.valueOf(binaryImpl.getStream());
        child.setProperty(FileProperties.PROPERTY_WIDTH, imageSize!=null ? imageSize.getWidth():150);
        child.setProperty(FileProperties.PROPERTY_HEIGHT, imageSize!=null ? imageSize.getHeight():150);
        child.getSession().save();

    }

}
