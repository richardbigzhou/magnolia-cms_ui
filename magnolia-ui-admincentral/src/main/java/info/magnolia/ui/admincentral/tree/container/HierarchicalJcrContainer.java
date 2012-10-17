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
package info.magnolia.ui.admincentral.tree.container;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer;
import info.magnolia.ui.vaadin.integration.jcr.container.JcrContainerSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;


/**
 * Hierarchical implementation of {@link info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer}.
 */
public class HierarchicalJcrContainer extends AbstractJcrContainer implements Container.Hierarchical {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalJcrContainer.class);

    public HierarchicalJcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        super(jcrContainerSource, workbenchDefinition);
    }

    @Override
    public Collection<String> getChildren(Object itemId) {
        try {
            long start = System.currentTimeMillis();
            Collection<Item> children = getJcrContainerSource().getChildren(getJcrContainerSource().getItemByPath((String) itemId));
            log.debug("Fetched {} children in {}ms", children.size(), System.currentTimeMillis() - start);
            return createContainerIds(children);
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public String getParent(Object itemId) {
        try {
            Item item = getJcrContainerSource().getItemByPath((String) itemId);
            if (item.isNode() && item.getDepth() == 0) {
                return null;
            }
            return item.getParent().getPath();
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Collection<String> rootItemIds() {
        try {
            return createContainerIds(getJcrContainerSource().getRootItemIds());
        }
        catch (RepositoryException e) {
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
        try {
            return getJcrContainerSource().getItemByPath((String)itemId).isNode();
        } catch (RepositoryException re) {
            return false;
        }
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot(Object itemId) {
        try {
            return getJcrContainerSource().isRoot(getJcrContainerSource().getItemByPath((String) itemId));
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        try {
            return getJcrContainerSource().hasChildren(getJcrContainerSource().getItemByPath((String) itemId));
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected Collection<String> createContainerIds(Collection<Item> children) throws RepositoryException {
        ArrayList<String> ids = new ArrayList<String>();
        for (javax.jcr.Item child : children) {
            ids.add(child.getPath());
        }
        return ids;
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        //at present tree view is not sortable
        return Collections.emptyList();
    }

}
