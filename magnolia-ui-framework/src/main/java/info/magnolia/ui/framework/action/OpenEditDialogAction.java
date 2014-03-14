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
package info.magnolia.ui.framework.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.Item;

/**
 * Opens a dialog for editing a node.
 *
 * @see OpenEditDialogActionDefinition
 */
public class OpenEditDialogAction extends AbstractAction<OpenEditDialogActionDefinition> {

    private final Item itemToEdit;
    private final FormDialogPresenterFactory formDialogPresenterFactory;
    private final UiContext uiContext;
    private final EventBus eventBus;
    private Map<Object, Item> idToItem;

    @Inject
    public OpenEditDialogAction(OpenEditDialogActionDefinition definition, Item itemToEdit, FormDialogPresenterFactory formDialogPresenterFactory, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, ContentConnector contentConnector, Map<Object, Item> idToItem) {
        super(definition);
        this.itemToEdit = itemToEdit;
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.idToItem = idToItem;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final Object itemId = getItemId(itemToEdit);
        final FormDialogPresenter formDialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(getDefinition().getDialogName());
        formDialogPresenter.start(itemId, getDefinition().getDialogName(), uiContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(itemId));
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
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
