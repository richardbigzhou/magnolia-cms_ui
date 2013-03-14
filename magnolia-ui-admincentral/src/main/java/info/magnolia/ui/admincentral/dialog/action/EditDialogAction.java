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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenter;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenterFactory;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.view.ModalLayer;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
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

    private final FormDialogPresenterFactory dialogPresenterFactory;

    private final Node nodeToEdit;

    private final ModalLayer modalLayer;

    @Inject
    public EditDialogAction(EditDialogActionDefinition definition, Node nodeToEdit, FormDialogPresenterFactory dialogPresenterFactory, final SubAppContext subAppContext) {
        super(definition);
        this.nodeToEdit = nodeToEdit;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.modalLayer = subAppContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final FormDialogPresenter dialogPresenter = dialogPresenterFactory.createDialogPresenterByName(getDefinition().getDialogName());
        final EventBus eventBus = dialogPresenter.getEventBus();

        String tempParentNodePath;
        try {
            tempParentNodePath = nodeToEdit.getParent().getPath();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        final String parentNodePath = tempParentNodePath;

        final JcrNodeAdapter item = new JcrNodeAdapter(nodeToEdit);
        dialogPresenter.start(item, modalLayer, new FormDialogPresenter.Callback() {

            @Override
            public void onSuccess(String actionName) {
                final String newItemId = (String) item.getItemProperty(ModelConstants.JCR_NAME).getValue();

                final String itemId = newItemId == null ? item.getPath() : NodeUtil.combinePathAndName(parentNodePath, newItemId);
                eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), itemId));
                dialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                dialogPresenter.closeDialog();
            }
        });
    }
}
