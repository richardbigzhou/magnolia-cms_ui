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
package info.magnolia.ui.vaadin.intergration.jcr;

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

    private boolean isNode;

    private  String jcrPropertyName;
    private  String jcrNodeIdentifier;
    private  String jcrWorkspace;
    private  String jcrPath;

    public JcrAbstractAdapter (Item jcrItem) {
        setPath(jcrItem);
        if(jcrItem.isNode()) {
            isNode = true;
            initNode((Node) jcrItem);
        } else {
            isNode = false;
            initProperty((Property) jcrItem);
        }
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
    public String getPropertyName() {
        return jcrPropertyName;
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

    private void initNode(Node jcrNode) {
        String identifier = null;
        String workspace = null;
        try {
            identifier = jcrNode.getIdentifier();
            workspace = jcrNode.getSession().getWorkspace().getName();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr node", e);
            identifier = UN_IDENTIFIED;
            workspace = UN_IDENTIFIED;
        }
        this.jcrNodeIdentifier = identifier;
        this.jcrWorkspace = workspace;
    }

    private void initProperty(Property jcrProperty) {
        String propertyIdentifier = null;
        String nodeIdentifier = null;
        String workspace = null;
        try {
            nodeIdentifier = jcrProperty.getParent().getIdentifier();
            workspace = jcrProperty.getParent().getSession().getWorkspace().getName();
            propertyIdentifier =jcrProperty.getName();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr property", e);
            propertyIdentifier = UN_IDENTIFIED;
            workspace = UN_IDENTIFIED;
            nodeIdentifier = UN_IDENTIFIED;
        }
        this.jcrNodeIdentifier = nodeIdentifier;
        this.jcrPropertyName = propertyIdentifier;
        this.jcrWorkspace = workspace;
    }

    private void setPath(Item item) {
        this.jcrPath = null;
        try {
            this.jcrPath = item.getPath();
        }catch (RepositoryException re) {
            log.error("",re);
        }
    }
}
