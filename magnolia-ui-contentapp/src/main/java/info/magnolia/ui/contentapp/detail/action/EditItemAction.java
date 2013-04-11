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
package info.magnolia.ui.contentapp.detail.action;


import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.view.ConfirmationCallback;
import info.magnolia.ui.vaadin.view.MessageStyleType;
import info.magnolia.ui.vaadin.view.ModalCloser;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for editing items in {@link info.magnolia.ui.contentapp.detail.DetailSubApp}.
 *
 * @see EditItemActionDefinition
 */
public class EditItemAction extends ActionBase<EditItemActionDefinition> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JcrItemNodeAdapter nodeItemToEdit;
    private final LocationController locationController;
    private final SubAppContext subAppContext;

    public EditItemAction(EditItemActionDefinition definition, JcrItemNodeAdapter nodeItemToEdit, LocationController locationController, SubAppContext subAppContext) {
        super(definition);
        this.nodeItemToEdit = nodeItemToEdit;
        this.locationController = locationController;
        this.subAppContext = subAppContext;
    }

    @Override
    public void execute() throws ActionExecutionException {



        final ModalCloser modalCloser = subAppContext.openConfirmation(
                MessageStyleType.INFO, "Do you really want to edit this page?", "Really, are you super sure?", "Do it", "Dont!", true,
                new ConfirmationCallback() {
            @Override
            public void onSuccess(String actionName) {
                try {
                    EditItemAction.this.executeAfterConfirmation();
                } catch (ActionExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                //nothing
            }
        });



    }

    public void executeAfterConfirmation() throws ActionExecutionException {
        try {
            if (StringUtils.isNotBlank(getDefinition().getNodeType()) && !getDefinition().getNodeType().equals(nodeItemToEdit.getNode().getPrimaryNodeType().getName())) {
                log.warn("EditItemAction requested for a node type definition {}. Current node type is {}. No action will be performed.",
                        getDefinition().getNodeType(), nodeItemToEdit.getNode().
                        getPrimaryNodeType().getName());
                return;
            }
            final String path = nodeItemToEdit.getNode().getPath();
            DetailLocation location = new DetailLocation(getDefinition().getAppId(), getDefinition().getSubAppId(), DetailView.ViewType.EDIT, path);
            locationController.goTo(location);

        } catch (RepositoryException e) {
            throw new ActionExecutionException("Could not execute EditItemAction: ", e);
        }
    }

}
