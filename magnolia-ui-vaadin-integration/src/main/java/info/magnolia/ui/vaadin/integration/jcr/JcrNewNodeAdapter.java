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

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.MetaDataUtil;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;


/**
 * Used to create a new Node based on an Vaadin Item.
 * This node adapter uses the parent node to initialize the global properties (workspace, path....).
 * No references is made to an existing JCR node (except for the parent of the node to create).
 */
public class JcrNewNodeAdapter extends JcrNodeAdapter{

    private static final Logger log = LoggerFactory.getLogger(JcrNewNodeAdapter.class);
    private String nodeType;
    private String nodeName = null;

    /**
     * @param parentNode: Parent of the node to create.
     * @param nodeType: Type node to create.
     */
    public JcrNewNodeAdapter(Node parentNode, String nodeType) {
        super(parentNode);
        this.nodeType = nodeType;
    }

    /**
     * @param parentNode: Parent of the node to create.
     * @param nodeType: Type node to create.
     * @param nodeName: Name of the new node.
     */
    public JcrNewNodeAdapter(Node parentNode, String nodeType, String nodeName) {
        super(parentNode);
        this.nodeType = nodeType;
        this.nodeName = nodeName;
    }

    /**
     * Create pure Vaadin Property fully decoupled for Jcr.
     */
    @Override
    public Property getItemProperty(Object id) {
        DefaultProperty property = null;

        if(changedProperties.containsKey(id)) {
            property = (DefaultProperty) changedProperties.get(id);
        }

        return property;
    }

    /**
     * Create a new subNode of the parent Node.
     *
     * @throws IllegalAccessError: If the node was already created.
     */
    @Override
    public Node getNode() {
        if(nodeName !=null) {
            //TODO ehe: Check what exactly to do in this case. throw exception or search for the last
            // created node. In this case, what to do if the node was created in another session and not yet stored.
            log.warn("Node already created. getNode() should only be called once. ");
            throw new IllegalAccessError("Should only call this method once");
        }

        return createNode();
    }

    /**
     * Return the parent Node.
     */
    public Node getParentNode() {
       return (Node)getJcrItem();
    }

    /**
     * Create a new node linked o the parent node used to
     * initialized this NewNodeAdapter.
     */
    private Node createNode() {
        Node node = null;
        try {
            Node parent = (Node)getJcrItem();
            nodeName = getUniqueNewItemName(parent);
            node = parent.addNode(nodeName, this.nodeType);
            log.debug("create a new node for parent "+parent.getPath()+" with nodeId "+nodeName);
            //Create MetaData
            MetaDataUtil.getMetaData(node);
            //Update property
            updateProperty(node);

            return node;
        }
        catch (LoginException e) {
            throw new RuntimeRepositoryException(e);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Create a new unique nodeName.
     * If JCR_NAME if part of the properties, use this property as desired nodeName.
     */
    private String getUniqueNewItemName(final Item item) throws RepositoryException, ItemNotFoundException, AccessDeniedException {
        if(item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        String nodeName = "";
        if(changedProperties.containsKey(JCR_NAME)) {
            nodeName = changedProperties.get(JCR_NAME).toString();
            changedProperties.remove(JCR_NAME);
        }

        return Path.getUniqueLabel(item.getSession(), item.getPath(), StringUtils.isNotBlank(nodeName)?nodeName:"untitled");
    }

}
