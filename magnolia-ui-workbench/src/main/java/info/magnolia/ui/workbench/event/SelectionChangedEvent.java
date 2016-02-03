/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.workbench.event;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This event is fired when an item is selected (i.e. a row in the data grid within the workbench
 * representing either a {@link javax.jcr.Node} or a {@link javax.jcr.Property}).
 */
public class SelectionChangedEvent implements Event<SelectionChangedEvent.Handler> {

    private static final Logger log = LoggerFactory.getLogger(SelectionChangedEvent.class);

    /**
     * Handles {@link SelectionChangedEvent} events.
     */
    public interface Handler extends EventHandler {

        void onSelectionChanged(SelectionChangedEvent event);
    }

    private final String workspace;

    private final List<JcrItemAdapter> items;

    public SelectionChangedEvent(String workspace, Set<JcrItemAdapter> items) {
        this.workspace = workspace;
        List<JcrItemAdapter> itemList = new ArrayList<JcrItemAdapter>(items.size());
        for (JcrItemAdapter item : items) {
            itemList.add(item);
        }
        this.items = itemList;
    }

    public String getWorkspace() {
        return workspace;
    }

    public List<String> getItemIds() {
        List<String> itemIds = new ArrayList<String>(items.size());
        for (JcrItemAdapter item : items) {
            itemIds.add(item.getItemId());
        }
        return itemIds;
    }

    public JcrItemAdapter getFirstItem() {
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null;
    }

    public String getFirstItemId() {
        JcrItemAdapter item = getFirstItem();
        if (item != null) {
            try {
                return JcrItemUtil.getItemId(item.getJcrItem());
            } catch (RepositoryException e) {
                log.debug("Cannot get ID for item [{}]. Error: {}", item, e.getMessage());
            }
        }
        return null;
    }

    public List<JcrItemAdapter> getItems() {
        return items;
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onSelectionChanged(this);
    }
}
