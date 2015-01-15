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
package info.magnolia.ui.framework.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Named;
import javax.jcr.Node;

/**
 * Opens a dialog for creating a new node in a tree.
 *
 * @see OpenCreateDialogActionDefinition
 */
public class OpenCreateDialogAction extends AbstractAction<OpenCreateDialogActionDefinition> {

    private final AbstractJcrNodeAdapter parentItem;
    private final FormDialogPresenterFactory formDialogPresenterFactory;
    private final UiContext uiContext;
    private final EventBus eventBus;

    public OpenCreateDialogAction(OpenCreateDialogActionDefinition definition, AbstractJcrNodeAdapter parentItem, FormDialogPresenterFactory formDialogPresenterFactory, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus) {
        super(definition);
        this.parentItem = parentItem;
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {

        Node parentNode = parentItem.getJcrItem();

        final JcrNodeAdapter item = new JcrNewNodeAdapter(parentNode, getDefinition().getNodeType());

        final FormDialogPresenter formDialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(getDefinition().getDialogName());

        formDialogPresenter.start(item, getDefinition().getDialogName(), uiContext, new EditorCallback() {

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
    }
}
