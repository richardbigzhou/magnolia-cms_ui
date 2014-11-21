/**
 * This file Copyright (c) 2014 Magnolia International
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

import info.magnolia.context.WebContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.server.Page;

/**
 * Action for downloading a binary.
 *
 * @param <D> action definition
 */
public class DownloadBinaryAction<D extends DownloadBinaryActionDefinition> extends AbstractAction<D> {

    private final DownloadBinaryActionDefinition definition;
    private final JcrItemAdapter item;
    private WebContext webContext;

    public static final String FOR_DOWNLOAD = "forDownload";

    @Inject
    public DownloadBinaryAction(D definition, JcrItemAdapter item, WebContext webContext) {
        super(definition);
        this.definition = definition;
        this.item = item;
        this.webContext = webContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (item instanceof JcrNodeAdapter) {
            final Node node = (Node) item.getJcrItem();
            Node binaryNode;
            try {
                String nodePath = node.getPath();
                binaryNode = getBinaryNode(node);
                String downloadUri = getDownloadUri(nodePath, getFileExtension(binaryNode));
                Page.getCurrent().open(downloadUri, null, false);
            } catch (RepositoryException e) {
                throw new ActionExecutionException(String.format("Error getting data for node [%s] to download.", node), e);
            }
        }
    }

    protected String getDownloadUri(String nodePath, String fileExtension) {
        StringBuilder uri = new StringBuilder(webContext.getContextPath());
        uri.append("/dam");
        uri.append(nodePath);
        uri.append(fileExtension);
        uri.append("?").append(FOR_DOWNLOAD).append("=1");
        return uri.toString();
    }

    protected String getFileExtension(Node binaryNode) throws RepositoryException {
        return "." + binaryNode.getProperty(definition.getExtensionProperty()).getString();
    }

    protected Node getBinaryNode(Node node) throws RepositoryException {
        return node.getNode(definition.getBinaryNodeName());
    }

}
