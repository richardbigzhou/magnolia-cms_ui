/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.SupportsCreation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Action for creating a new item.
 * The {@link info.magnolia.ui.contentapp.detail.DetailSubApp} only gets a location containing nodePath and {@link DetailView.ViewType}.
 * When creating a new node, we either create it here and pass the new path to the subapp or
 * we pass all needed parameters to the location. This is less messy, but not optimal.. at all.
 * See MGNLUI-222.
 *
 * @see CreateItemActionDefinition
 */
public class CreateItemAction extends AbstractAction<CreateItemActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(CreateItemAction.class);

    private final LocationController locationController;
    private final Item parentItem;
    private final ContentConnector contentConnector;

    public CreateItemAction(CreateItemActionDefinition definition, LocationController locationController, Item parentItem, ContentConnector contentConnector) {
        super(definition);
        this.locationController = locationController;
        this.parentItem = parentItem;
        this.contentConnector = contentConnector;
    }

    @Override
    public void execute() throws ActionExecutionException {

        Object parentId = contentConnector.getItemId(parentItem);
        if (!contentConnector.canHandleItem(parentId)) {
            log.warn("ContentConnector {{}} cannot handle parent itemId {{}}. No action will be performed.", contentConnector, String.valueOf(parentId));
            return;
        }
        if (!(contentConnector instanceof SupportsCreation)) {
            log.warn("ContentConnector {{}} doesn't support creation of new items. No action will be performed.", contentConnector);
            return;
        }
        // Create a new id and retrieve its path with the content connector
        String path = contentConnector.getItemUrlFragment(((SupportsCreation)contentConnector).getNewItemId(parentId, getDefinition().getNodeType()));
        DetailLocation location = new DetailLocation(getDefinition().getAppName(), getDefinition().getSubAppId(), DetailView.ViewType.EDIT, path, null);
        locationController.goTo(location);
    }
}
