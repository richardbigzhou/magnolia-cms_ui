/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.pages.app.action;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * EditElementAction.
 */
public class EditElementAction extends AbstractAction<EditElementActionDefinition> {
    private FormDialogPresenterFactory dialogPresenterFactory;
    private AbstractElement element;
    private SubAppContext subAppContext;
    private EventBus eventBus;

    @Inject
    public EditElementAction(EditElementActionDefinition definition, AbstractElement element,
                             SubAppContext subAppContext, @Named(SubAppEventBus.NAME) EventBus eventBus, FormDialogPresenterFactory dialogPresenterFactory) {
        super(definition);
        this.element = element;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.dialogPresenterFactory = dialogPresenterFactory;
    }

    @Override
    public void execute() throws ActionExecutionException {

        try {
            String workspace = element.getWorkspace();
            String path = element.getPath();
            String dialogId = element.getDialog();
            Session session = MgnlContext.getJCRSession(workspace);
            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);
            final JcrNodeAdapter item = new JcrNodeAdapter(node);
            final FormDialogPresenter formDialogPresenter = dialogPresenterFactory.createFormDialogPresenter(dialogId);
            formDialogPresenter.start(item, dialogId, subAppContext, new EditorCallback() {

                @Override
                public void onSuccess(String actionName) {
                    eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                    formDialogPresenter.closeDialog();
                }

                @Override
                public void onCancel() {
                    formDialogPresenter.closeDialog();
                }
            });
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }
}
