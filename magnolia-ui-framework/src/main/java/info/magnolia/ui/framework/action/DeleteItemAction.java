/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node or property from the repository.
 *
 * @see DeleteItemActionDefinition
 */
public class DeleteItemAction extends AbstractMultiItemAction<DeleteItemActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UiContext uiContext;
    private final EventBus eventBus;
    private final SimpleTranslator i18n;

    public DeleteItemAction(DeleteItemActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, Collections.singletonList(item), uiContext);
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    public DeleteItemAction(DeleteItemActionDefinition definition, List<JcrItemAdapter> items, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, uiContext);
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {

        uiContext.openConfirmation(
                MessageStyleTypeEnum.WARNING, getConfirmationQuestion(),
                i18n.translate("ui-framework.actions.deleteItem.warningText"),
                i18n.translate("ui-framework.actions.deleteItem.confirmText"),
                i18n.translate("ui-framework.actions.deleteItem.cancelText"),
                true,
                new ConfirmationCallback() {

                    @Override
                    public void onSuccess() {
                        DeleteItemAction.this.executeAfterConfirmation();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    private String getConfirmationQuestion() {
        if (getItems().size() == 1) {
            return i18n.translate("ui-framework.actions.deleteItem.confirmationQuestionOneItem");
        }
        return String.format(i18n.translate("ui-framework.actions.deleteItem.confirmationQuestionManyItems"), getItems().size());
    }

    protected void executeAfterConfirmation() {
        try {
            super.execute();
        } catch (ActionExecutionException e) {
            log.error("Problem occured during deleting items.", e);
        }
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        try {
            final Item jcrItem = item.getJcrItem();
            if (jcrItem.getDepth() == 0) {
                // cannot delete root node
                throw new IllegalArgumentException(i18n.translate("ui-framework.actions.deleteItem.cannotDeleteRootItem"));
            }
            JcrItemId itemIdOfChangedItem = JcrItemUtil.getItemId(jcrItem.getParent());
            Session session = jcrItem.getSession();
            jcrItem.remove();
            session.save();

            eventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));
        } catch (PathNotFoundException e) {
            // ignore - the item has been probably already deleted (with a parent node)
        }
    }

    @Override
    protected String getSuccessMessage() {
        if (getItems().size() == 1) {
            return i18n.translate("ui-framework.actions.deleteItem.successOneItemDeleted");
        } else {
            return i18n.translate("ui-framework.actions.deleteItem.successManyItemsDeleted", getItems().size());
        }
    }

    @Override
    protected String getFailureMessage() {
        return i18n.translate("ui-framework.actions.deleteItem.deletionfailure", getFailedItems().size(), getItems().size());
    }
}
