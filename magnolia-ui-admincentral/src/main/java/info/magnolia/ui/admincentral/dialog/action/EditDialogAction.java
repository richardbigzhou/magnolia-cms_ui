/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.dialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.OverlayLayer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

/**
 * Opens a dialog for editing a node. We need to manually take care of nodeName changes. This must be properly solved by
 * passing the node Identifier to {@link ContentChangedEvent}.
 *
 * See MGNLUI-226.
 *
 * @see EditDialogActionDefinition
 */
public class EditDialogAction extends ActionBase<EditDialogActionDefinition> {

    private final JcrNodeAdapter itemToEdit;
    private FormDialogPresenter formDialogPresenter;

    private final OverlayLayer overlayLayer;
    private EventBus eventBus;

    @Inject
    public EditDialogAction(EditDialogActionDefinition definition, JcrNodeAdapter itemToEdit, FormDialogPresenter formDialogPresenter, final SubAppContext subAppContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus) {
        super(definition);
        this.itemToEdit = itemToEdit;
        this.formDialogPresenter = formDialogPresenter;
        this.overlayLayer = subAppContext;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {
        String tempParentNodePath;
        try {
            tempParentNodePath = itemToEdit.getNode().getParent().getPath();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        final String parentNodePath = tempParentNodePath;
        formDialogPresenter.start(itemToEdit, getDefinition().getDialogName(), overlayLayer, new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                final String newItemId = (String) itemToEdit.getItemProperty(ModelConstants.JCR_NAME).getValue();
                final String itemId = newItemId == null ? itemToEdit.getPath() : NodeUtil.combinePathAndName(parentNodePath, newItemId);

                eventBus.fireEvent(new ContentChangedEvent(itemToEdit.getWorkspace(), itemId));
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
    }
}
