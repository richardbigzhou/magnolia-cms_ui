/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.detail.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.integration.datasource.DataSource;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Action for editing items in {@link info.magnolia.ui.contentapp.detail.DetailSubApp}.
 *
 * @see EditItemActionDefinition
 */
public class EditItemAction extends AbstractAction<EditItemActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<Object, Item> idToItem;

    private final Item nodeItemToEdit;

    private final LocationController locationController;

    private DataSource dataSource;

    public EditItemAction(EditItemActionDefinition definition, Map<Object, Item> idToItem, Item nodeItemToEdit, LocationController locationController, DataSource dataSource) {
        super(definition);
        this.idToItem = idToItem;
        this.nodeItemToEdit = nodeItemToEdit;
        this.locationController = locationController;
        this.dataSource = dataSource;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            // TODO JCR FREE - availability voter looks more appropriate here!
//            if (StringUtils.isNotBlank(getDefinition().getNodeType()) && !getDefinition().getNodeType().equals(nodeItemToEdit.getJcrItem().getPrimaryNodeType().getName())) {
//                log.warn("EditItemAction requested for a node type definition {}. Current node type is {}. No action will be performed.",
//                        getDefinition().getNodeType(), nodeItemToEdit.getJcrItem().
//                        getPrimaryNodeType().getName());
//                return;
//            }
            Object itemId = getItemId(nodeItemToEdit);
            final String path = dataSource.getItemUrlFragment(itemId);
            DetailLocation location = new DetailLocation(getDefinition().getAppName(), getDefinition().getSubAppId(), DetailView.ViewType.EDIT, path, "");
            locationController.goTo(location);

        } catch (Exception e) {
            throw new ActionExecutionException("Could not execute EditItemAction: ", e);
        }
    }

    private Object getItemId(Item nodeItemToEdit) {
        Iterator<Map.Entry<Object, Item>> entryIt = idToItem.entrySet().iterator();
        while (entryIt.hasNext()) {
            Map.Entry<Object, Item> entry = entryIt.next();
            if (entry.getValue() == nodeItemToEdit) {
                return entry.getKey();
            }
        }
        return null;
    }
}
