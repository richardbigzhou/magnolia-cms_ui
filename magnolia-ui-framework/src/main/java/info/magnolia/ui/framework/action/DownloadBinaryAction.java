/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;

/**
 * Action for downloading a binary.
 *
 * @param <D> action definition
 */
public class DownloadBinaryAction<D extends DownloadBinaryActionDefinition> extends AbstractAction<D> {

    private static final String CONTENT_TYPE = "application/octet-stream";
    private final DownloadBinaryActionDefinition definition;
    private final JcrItemAdapter item;

    public DownloadBinaryAction(D definition, JcrItemAdapter item) {
        super(definition);
        this.definition = definition;
        this.item = item;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (item instanceof JcrNodeAdapter) {
            final Node node = (Node) item.getJcrItem();
            final InputStream inputStream;
            Node binaryNode;
            String fileName;
            try {
                binaryNode = getBinaryNode(node);
                fileName = getFileName(binaryNode);
                inputStream = getInputStream(binaryNode);
            } catch (RepositoryException e) {
                throw new ActionExecutionException(e);
            }

            StreamResource.StreamSource source = new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    return inputStream;
                }
            };

            StreamResource resource = new StreamResource(source, fileName);
            resource.setCacheTime(-1);
            resource.getStream().setParameter("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            resource.setMIMEType(CONTENT_TYPE);
            resource.setCacheTime(0);

            Page.getCurrent().open(resource, null, false);
        }
    }

    protected InputStream getInputStream(Node binaryNode) throws RepositoryException {
            Binary binary = binaryNode.getProperty(definition.getDataProperty()).getBinary();
            return binary.getStream();
    }

    protected String getFileName(Node binaryNode) throws RepositoryException {
        String fileName = binaryNode.getProperty(definition.getFileNameProperty()).getString();
        String extension = binaryNode.getProperty(definition.getExtensionProperty()).getString();
        return fileName + "." + extension;
    }

    protected Node getBinaryNode(Node node) throws RepositoryException {
        return node.getNode(definition.getBinaryNodeName());
    }
}
