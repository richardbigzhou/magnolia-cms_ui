/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * The WorkbenchPresenter is responsible for creating, configuring and updating the workbench view, as well as handling its interaction.
 */
public class WorkbenchPresenter extends WorkbenchPresenterBase<String> {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public WorkbenchPresenter(WorkbenchView view, ComponentProvider componentProvider, WorkbenchStatusBarPresenter statusBarPresenter) {
        super(view, componentProvider, statusBarPresenter);
    }

    @Override
    public String resolveWorkbenchRoot() {
        try {
            return JcrItemUtil.getItemId(getWorkbenchDefinition().getWorkspace(), getWorkbenchDefinition().getPath());
        } catch (RepositoryException e) {
            log.error("Could not find workbench root node", e);
            return null;
        }
    }

    @Override
    public Item getItemFor(String itemId) {
        javax.jcr.Item jcrItem;
        try {
            jcrItem = JcrItemUtil.getJcrItem(getWorkspace(), itemId);
            JcrItemAdapter itemAdapter;
            if (jcrItem.isNode()) {
                itemAdapter = new JcrNodeAdapter((Node) jcrItem);
            } else {
                itemAdapter = new JcrPropertyAdapter((Property) jcrItem);
            }
            return itemAdapter;
        } catch (RepositoryException e) {
            log.error("Failed to find item for id", e);
            return null;
        }
    }

    @Override
    protected List<String> filterExistingItems(List<String> itemIds) {
        List<String> filteredIds = new ArrayList<String>();
        for (String itemId : itemIds) {
            try {
                if (JcrItemUtil.itemExists(getWorkspace(), itemId)) {
                    filteredIds.add(itemId);
                } else {
                    WorkbenchDefinition def = getWorkbenchDefinition();
                    log.info("Trying to re-sync workbench with no longer existing path {} at workspace {}. Will reset path to its configured root {}.", new Object[] { itemId, def.getWorkspace(), def.getPath() });
                }
            } catch (RepositoryException e) {
                log.error("Error occurred while filtering existing ids", e);
                continue;
            }
        }
        return filteredIds;
    }

    @Override
    protected Container getContainer() {
        return new HierarchicalJcrContainer(getWorkbenchDefinition());
    }
}