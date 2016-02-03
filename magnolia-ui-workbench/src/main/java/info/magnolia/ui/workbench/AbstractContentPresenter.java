/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.workbench;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;

/**
 * Abstract generic logic for content presenters.
 */
public abstract class AbstractContentPresenter extends AbstractContentPresenterBase {

    private static final Logger log = LoggerFactory.getLogger(AbstractContentPresenterBase.class);

    @Inject
    public AbstractContentPresenter(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getIcon(Item item)  {
        try {
            if (item instanceof JcrPropertyAdapter) {
                return ICON_PROPERTY;
            } else if (item instanceof JcrNodeAdapter) {
                Node node = ((AbstractJcrNodeAdapter)item).getJcrItem();
                if (NodeUtil.hasMixin(node, NodeTypes.Deleted.NAME)) {
                    return ICON_TRASH;
                }

                NodeTypeDefinition nodeTypeDefinition = getNodeTypeDefinitionForNode(node);
                if (nodeTypeDefinition != null) {
                    return nodeTypeDefinition.getIcon();
                }
            }

        } catch (RepositoryException e) {
            log.warn("Unable to resolve icon", e);
        }
        return null;
    }

    @Override
    public void refresh() {
        final List<Object> newSelection = Lists.newLinkedList();
        for (final Object id : getSelectedItemIds()) {
            if (contentConnector.canHandleItem(id)) {
                newSelection.add(id);
            }
        }

        if (newSelection.size() != getSelectedItemIds().size()) {
            select(newSelection);
            setSelectedItemIds(newSelection);
        }
    }

    private NodeTypeDefinition getNodeTypeDefinitionForNode(Node node) throws RepositoryException {
        String primaryNodeTypeName = node.getPrimaryNodeType().getName();
        JcrContentConnectorDefinition connectorDefinition = ((JcrContentConnector)contentConnector).getContentConnectorDefinition();
        for (NodeTypeDefinition nodeTypeDefinition : connectorDefinition.getNodeTypes()) {
            if (nodeTypeDefinition.isStrict()) {
                if (primaryNodeTypeName.equals(nodeTypeDefinition.getName())) {
                    return nodeTypeDefinition;
                }
            } else if (NodeUtil.isNodeType(node, nodeTypeDefinition.getName())) {
                return nodeTypeDefinition;
            }
        }
        return null;
    }

}
