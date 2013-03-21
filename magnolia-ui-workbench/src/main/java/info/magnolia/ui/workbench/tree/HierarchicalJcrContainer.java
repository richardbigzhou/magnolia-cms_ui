/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;

/**
 * Hierarchical implementation of {@link info.magnolia.ui.workbench.container.AbstractJcrContainer}.
 */
public class HierarchicalJcrContainer extends AbstractJcrContainer implements Container.Hierarchical {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalJcrContainer.class);

    private static class ItemNameComparator implements Comparator<Item> {
        @Override
        public int compare(Item lhs, Item rhs) {
            try {
                return lhs.getName().compareTo(rhs.getName());
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    public HierarchicalJcrContainer(WorkbenchDefinition workbenchDefinition) {
        super(workbenchDefinition);
    }

    @Override
    public Collection<String> getChildren(Object itemId) {
        try {
            long start = System.currentTimeMillis();
            Collection<Item> children = getChildren(getItemByPath((String) itemId));
            log.debug("Fetched {} children in {}ms", children.size(), System.currentTimeMillis() - start);
            return createContainerIds(children);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public String getParent(Object itemId) {
        try {
            Item item = getItemByPath((String) itemId);
            if (item.isNode() && item.getDepth() == 0) {
                return null;
            }
            return item.getParent().getPath();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Collection<String> rootItemIds() {
        try {
            return createContainerIds(getRootItemIds());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        final JcrItemAdapter item = ((JcrItemAdapter) getItem(itemId));
        return item.isNode() && hasChildren(itemId);
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot(Object itemId) {
        try {
            return isRoot(getItemByPath((String) itemId));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        try {
            final Item item = getItemByPath((String) itemId);
            return item.isNode() && !getChildren(item).isEmpty();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected Collection<String> createContainerIds(Collection<Item> children) throws RepositoryException {
        ArrayList<String> ids = new ArrayList<String>();
        for (Item child : children) {
            ids.add(child.getPath());
        }
        return ids;
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        // at present tree view is not sortable
        return Collections.emptyList();
    }

    public Collection<Item> getChildren(Item item) throws RepositoryException {
        if (!item.isNode()) {
            return Collections.emptySet();
        }

        Node node = (Node) item;

        ArrayList<Item> items = new ArrayList<Item>();

        ArrayList<Node> nodesWithMatchingTypes = new ArrayList<Node>();
        NodeIterator iterator = node.getNodes();
        final List<NodeTypeDefinition> nodeTypes = getWorkbenchDefinition().getNodeTypes();
        String currentNodeTypeName;
        while (iterator.hasNext()) {
            Node next = iterator.nextNode();
            currentNodeTypeName = next.getPrimaryNodeType().getName();
            for (NodeTypeDefinition current: nodeTypes) {
                if (current.getName().equals(currentNodeTypeName)) {
                    nodesWithMatchingTypes.add(next);
                    break;
                }
            }
        }

        items.addAll(nodesWithMatchingTypes);

        if (getWorkbenchDefinition().includeProperties()) {
            ArrayList<Property> properties = new ArrayList<Property>();
            PropertyIterator propertyIterator = node.getProperties();
            while (propertyIterator.hasNext()) {
                final Property property = propertyIterator.nextProperty();
                final String propertyName = property.getName();
                if (!propertyName.startsWith(NodeTypes.JCR_PREFIX) && !propertyName.startsWith(NodeTypes.MGNL_PREFIX)) {
                    properties.add(property);
                }
            }
            ItemNameComparator itemNameComparator = new ItemNameComparator();
            Collections.sort(properties, itemNameComparator);
            items.addAll(properties);
        }

        return Collections.unmodifiableCollection(items);
    }

    public Collection<Item> getRootItemIds() throws RepositoryException {
        return getChildren(getRootNode());
    }

    /**
     * Checks if an item is a root. Since root node is never shown, we consider its child nodes and properties as roots
     * to remove unnecessary offset in trees.
     */
    public boolean isRoot(Item item) throws RepositoryException {
        int rootDepth = getRootNode().getDepth();
        return item.getDepth() <= rootDepth + 1;
    }

    public Item getItemByPath(String path) throws RepositoryException {
        String absolutePath = getPathInWorkspace(path);
        return getSession().getItem(absolutePath);
    }

    // Move operations performed by drag-n-drop in JcrBrowser

    // TODO these move methods need to be commands instead

    public boolean moveItem(Item source, Item target) {
        if (!basicMoveCheck(source, target)) {
            return false;
        }
        try {
            NodeUtil.moveNode((Node) source, (Node) target);
            source.getSession().save();
            return true;
        } catch (RepositoryException re) {
            log.warn("Got exception in drag and drop action", re);
            return false;
        }
    }

    public boolean moveItemBefore(Item source, Item target) {
        if (!basicMoveCheck(source, target)) {
            return false;
        }
        try {
            NodeUtil.moveNodeBefore((Node) source, (Node) target);
            source.getSession().save();
            return true;
        } catch (RepositoryException re) {
            log.warn("Got exception in drag and drop action", re);
            return false;
        }
    }

    public boolean moveItemAfter(Item source, Item target) {
        if (!basicMoveCheck(source, target)) {
            return false;
        }
        try {
            NodeUtil.moveNodeAfter((Node) source, (Node) target);
            source.getSession().save();
            return true;
        } catch (RepositoryException re) {
            log.warn("Got exception in drag and drop action", re);
            return false;
        }
    }

    /**
     * Perform basic check.
     */
    private boolean basicMoveCheck(Item source, Item target) {
        try {
            // One or both are not node... do nothing
            if (!target.isNode() || !source.isNode()) {
                return false;
            }
            // Source and origin are the same... do nothing
            if (target.getPath().equals(source.getPath())) {
                return false;
            }
            // Source can not be a child of target.
            return !target.getPath().startsWith(source.getPath());
        } catch (RepositoryException re) {
            log.warn("Got exception in drag and drop action", re);
            return false;
        }
    }

    /**
     * Only used in tests.
     */
    String getPathInTree(Item item) throws RepositoryException {
        String base = getWorkbenchDefinition().getPath();
        return "/".equals(base) ? item.getPath() : StringUtils.substringAfter(item.getPath(), base);
    }

    private Session getSession() throws RepositoryException {
        return MgnlContext.getJCRSession(getWorkspace());
    }

    private Node getRootNode() throws RepositoryException {
        return getSession().getNode(getWorkbenchDefinition().getPath());
    }

    private String getPathInWorkspace(String pathInTree) {
        // if path is absolute, just return it
        if (pathInTree.startsWith("/")) {
            return pathInTree;
        }
        String base = getWorkbenchDefinition().getPath();
        return base + pathInTree;
    }

}
