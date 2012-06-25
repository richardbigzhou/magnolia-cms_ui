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
package info.magnolia.ui.vaadin.integration.jcr;

import info.magnolia.context.MgnlContext;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Common base for {JcrItemAdapter} implementation.
 */
public abstract class JcrAbstractAdapter implements JcrItemAdapter {

    private static final Logger log = LoggerFactory.getLogger(JcrAbstractAdapter.class);

    static final String UN_IDENTIFIED = "?";
    //Common variable
    private boolean isNode;
    private  String jcrNodeIdentifier;
    private  String jcrWorkspace;
    private  String jcrPath;

    public JcrAbstractAdapter (Item jcrItem) {
        setCommonAttributes(jcrItem);
    }

    /**
     * Init common Item attributes.
     */
    private void setCommonAttributes(Item jcrItem) {
        String nodeIdentifier = null;
        String workspace = null;
        String path = null;
        try {
            isNode = jcrItem.isNode();
            Node node = isNode ? ((Node)jcrItem) : ((Property)jcrItem).getParent();
            nodeIdentifier = node.getIdentifier();
            workspace = node.getSession().getWorkspace().getName();
            path = jcrItem.getPath();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr property", e);
            path = UN_IDENTIFIED;
            workspace = UN_IDENTIFIED;
            nodeIdentifier = UN_IDENTIFIED;
        }
        this.jcrPath = path;
        this.jcrNodeIdentifier = nodeIdentifier;
        this.jcrWorkspace = workspace;
    }

    @Override
    public boolean isNode() {
        return isNode;
    }

    @Override
    public String getNodeIdentifier() {
        return jcrNodeIdentifier;
    }

    @Override
    public javax.jcr.Item getJcrItem() throws RepositoryException{
        return MgnlContext.getJCRSession(jcrWorkspace).getItem(jcrPath);
    }

    @Override
    public void save() throws RepositoryException {
        getJcrItem().getSession().save();
    }

    @Override
    public String getItemId() {
        return this.jcrPath;
    }

    public String getWorkspace() {
        return this.jcrWorkspace;
    }

}
