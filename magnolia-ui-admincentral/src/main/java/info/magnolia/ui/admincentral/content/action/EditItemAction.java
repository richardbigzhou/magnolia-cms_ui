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
package info.magnolia.ui.admincentral.content.action;

import info.magnolia.ui.admincentral.app.content.location.ItemLocation;
import info.magnolia.ui.admincentral.content.item.ItemView;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Action for editing items in {@link info.magnolia.ui.admincentral.app.content.AbstractItemSubApp}.
 * 
 * @see EditItemActionDefinition
 */
public class EditItemAction extends ActionBase<EditItemActionDefinition> {

    private final Node nodeToEdit;
    private final LocationController locationController;

    public EditItemAction(EditItemActionDefinition definition, Node nodeToEdit, LocationController locationController) {
        super(definition);
        this.nodeToEdit = nodeToEdit;
        this.locationController = locationController;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {

            final String path = nodeToEdit.getPath();
            ItemLocation location = new ItemLocation(getDefinition().getAppId(), getDefinition().getSubAppId(), ItemView.ViewType.EDIT, path);
            locationController.goTo(location);

        } catch (RepositoryException e) {
            throw new ActionExecutionException("Could not execute EditItemAction: ", e);
        }
    }
}
