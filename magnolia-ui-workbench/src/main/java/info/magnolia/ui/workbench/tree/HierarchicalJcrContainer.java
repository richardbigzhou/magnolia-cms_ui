/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
package info.magnolia.ui.workbench.tree;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.container.OrderBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;

/**
 * Hierarchical implementation of {@link info.magnolia.ui.workbench.container.AbstractJcrContainer}.
 */
public class HierarchicalJcrContainer extends AbstractJcrContainer implements Container.Hierarchical {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalJcrContainer.class);

    private static final String WHERE_CLAUSE_FOR_PATH = " ISCHILDNODE('%s')";

    private String nodePath;

    private static class ItemNameComparator implements Comparator<Item> {
        @Override
        public int compare(Item lhs, Item rhs) {
            try {
                return lhs.getName().compareTo(rhs.getName());
            } catch (RepositoryException e) {
                log.warn("Cannot compare item names: " + e);
                return 0;
            }
        }
    }

    public HierarchicalJcrContainer(JcrContentConnectorDefinition definition) {
        super(definition);
    }

    @Override
    public Collection<JcrItemId> getChildren(Object itemId) {
        long start = System.currentTimeMillis();
        Collection<Item> children = getChildren(getJcrItem(itemId));
        log.debug("Fetched {} children in {}ms", children.size(), System.currentTimeMillis() - start);
        return createContainerIds(children);
    }

    @Override
    public JcrItemId getParent(Object itemId) {
        try {
            Item item = getJcrItem(itemId);
            if (item.isNode() && item.getDepth() == 0) {
                return null;
            }
            return JcrItemUtil.getItemId(item.getParent());
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot determine parent for itemId: " + itemId, e);
            return null;
        }
    }

    @Override
    public Collection<JcrItemId> rootItemIds() {
        try {
            return createContainerIds(getRootItemIds());
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot retrieve root item id's", e);
            return Collections.emptySet();
        }
    }

    @Override
    public void refresh() {
        resetOffset();
        clearItemIndexes();
        fireItemSetChange();
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        final JcrItemAdapter item = ((JcrItemAdapter) getItem(itemId));
        if (item == null) {
            return false;
        }
        return item.isNode() && hasChildren(itemId);
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot(Object itemId) {
        try {
            return isRoot(getJcrItem(itemId));
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot determine whether item is root - itemId: " + itemId, e);
            return true;
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        final Item item = getJcrItem(itemId);
        if (item.isNode()) {
            final Node node = (Node) item;
            try {
                final NodeIterator it = node.getNodes();
                while (it.hasNext()) {
                    if (isNodeVisible(it.nextNode())) {
                        return true;
                    }
                }
            } catch (RepositoryException e) {
                log.warn("Failed to get child nodes of {}", NodeUtil.getPathIfPossible((node)));
            }


            if (getConfiguration().isIncludeProperties()) {
                try {
                    final PropertyIterator propertyIterator = node.getProperties();
                    while (propertyIterator.hasNext()) {
                        final Property property = propertyIterator.nextProperty();
                        if (!isJcrOrMgnlProperty(property)) {
                            return true;
                        }
                    }
                } catch (RepositoryException e) {
                    log.warn("Failed to get child nodes of {}", NodeUtil.getPathIfPossible((node)));
                }
            }
        }
        return false;
    }

    @Override
    public void sort(final Object[] propertyId, final boolean[] ascending) {
        sorters.clear();
        for (int i = 0; i < propertyId.length; i++) {
            if (getSortableContainerPropertyIds().contains(String.valueOf(propertyId[i]))) {
                OrderBy orderBy = new OrderBy((String) propertyId[i], ascending[i]);
                sorters.add(orderBy);
            }
        }
        fireItemSetChange();
    }

    private boolean isJcrOrMgnlProperty(Property property) throws RepositoryException {
        final String propertyName = property.getName();
        return propertyName.startsWith(NodeTypes.JCR_PREFIX) || propertyName.startsWith(NodeTypes.MGNL_PREFIX);
    }

    protected Collection<JcrItemId> createContainerIds(Collection <Item> children) {
        ArrayList<JcrItemId> ids = new ArrayList<JcrItemId>();
        for (Item child : children) {
            try {
                JcrItemId itemId = JcrItemUtil.getItemId(child);
                ids.add(itemId);
            } catch (RepositoryException e) {
                handleRepositoryException(log, "Cannot retrieve currentId", e);
            }
        }
        return ids;
    }

    public Collection<Item> getChildren(Item item) {
        if (!item.isNode()) {
            return Collections.emptySet();
        }

        Node node = (Node) item;
        ArrayList<Item> items = new ArrayList<>();
        try {
            NodeIterator iterator;
            // we cannot use query just for getting children of a node as SQL2 is not returning them in natural order
            // so if any sorter is defined use query, otherwise get all children from the node
            if (sorters.size() > 0) {
                nodePath = node.getPath();
                String query = constructJCRQuery(true);
                iterator = QueryUtil.search(getWorkspace(), query, Query.JCR_SQL2);
            } else {
                iterator = node.getNodes();
            }

            while (iterator.hasNext()) {
                Node next = iterator.nextNode();
                if (isNodeVisible(next)) {
                    items.add(next);
                }
            }

            if (getConfiguration().isIncludeProperties()) {
                ArrayList<Property> properties = new ArrayList<>();
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    final Property property = propertyIterator.nextProperty();
                    if (!isJcrOrMgnlProperty(property)) {
                        properties.add(property);
                    }
                }
                ItemNameComparator itemNameComparator = new ItemNameComparator();
                Collections.sort(properties, itemNameComparator);
                items.addAll(properties);
            }
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Could not retrieve children", e);
        }

        return Collections.unmodifiableCollection(items);
    }

    protected boolean isNodeVisible(Node node) throws RepositoryException {

        if (!getConfiguration().isIncludeSystemNodes() && node.getName().startsWith("jcr:") || node.getName().startsWith("rep:")) {
            return false;
        }

        String primaryNodeTypeName = node.getPrimaryNodeType().getName();
        for (NodeTypeDefinition nodeTypeDefinition : getConfiguration().getNodeTypes()) {
            if (nodeTypeDefinition.isStrict()) {
                if (primaryNodeTypeName.equals(nodeTypeDefinition.getName())) {
                    return true;
                }
            } else if (NodeUtil.isNodeType(node, nodeTypeDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    public Collection<Item> getRootItemIds() throws RepositoryException {
        return getChildren(getRootNode());
    }

    /**
     * Checks if an item is a root. Since root node is never shown, we consider its child nodes and properties as roots
     * to remove unnecessary offset in trees.
     */
    public boolean isRoot(Item item) throws RepositoryException {
        if (item != null) {
            try {
                int rootDepth = getRootNode().getDepth();
                return item.getDepth() <= rootDepth + 1;
            } catch (RepositoryException e) {
                handleRepositoryException(log, "Cannot determine depth of jcr item", e);
            }
        }
        return true;
    }

    @Override
    protected String getQueryWhereClause() {
        String whereClause;
        String clauseWorkspacePath = getQueryWhereClauseForNodePath(nodePath);
        String clauseNodeTypes = getQueryWhereClauseNodeTypes();
        if (StringUtils.isNotBlank(clauseNodeTypes)) {
            whereClause = " where ((" + clauseNodeTypes + ") ";
            if (StringUtils.isNotBlank(clauseWorkspacePath)) {
                whereClause += "and " + clauseWorkspacePath;
            }
            whereClause += ") ";
        } else {
            whereClause = " where ";
        }

        log.debug("JCR query WHERE clause is {}", whereClause);
        return whereClause;
    }

    private String getQueryWhereClauseForNodePath(final String nodePath) {
        return String.format(WHERE_CLAUSE_FOR_PATH, nodePath);
    }

    @Override
    protected String getQueryWhereClauseNodeTypes() {
        List<String> defs = new ArrayList<>();
        for (NodeType nt : getSearchableNodeTypes()) {
            if (nt.isMixin()) {
                // Mixin type information is found in jcr:mixinTypes property see http://www.day.com/specs/jcr/2.0/10_Writing.html#10.10.3%20Assigning%20Mixin%20Node%20Types
                defs.add("[jcr:mixinTypes] = '" + nt.getName() + "'");
            } else {
                defs.add("[jcr:primaryType] = '" + nt.getName() + "'");
            }
        }
        return StringUtils.join(defs, " or ");
    }

    /**
     * Only used in tests.
     */
    String getPathInTree(Item item) throws RepositoryException {
        String base = getConfiguration().getRootPath();
        return "/".equals(base) ? item.getPath() : StringUtils.substringAfter(item.getPath(), base);
    }

    private Session getSession() throws RepositoryException {
        return MgnlContext.getJCRSession(getWorkspace());
    }

    protected Node getRootNode() throws RepositoryException {
        return getSession().getNode(getConfiguration().getRootPath());
    }
}
