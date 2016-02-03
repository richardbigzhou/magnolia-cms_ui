/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.usermenu.action;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

/**
 * Action which open dialog for editing profile information of currently logged user.
 */
public class EditUserProfileDialogAction extends AbstractAction<EditUserProfileDialogActionDefinition>{

    private final FormDialogPresenterFactory formDialogPresenterFactory;
    private final UiContext uiContext;
    private final EventBus eventBus;
    private final SimpleTranslator i18n;

    @Inject
    public EditUserProfileDialogAction(EditUserProfileDialogActionDefinition definition, FormDialogPresenterFactory formDialogPresenterFactory, UiContext uiContext, @Named(AdmincentralEventBus.NAME) EventBus eventBus, SimpleTranslator i18n) {
        super(definition);
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final String dialogName = getDefinition().getDialogName();
        if(StringUtils.isBlank(dialogName)){
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.no.dialog.definition", getDefinition().getName()));
            return;

        }
        final FormDialogPresenter formDialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(dialogName);
        if(formDialogPresenter == null){
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.dialog.not.registered", dialogName));
            return;
        }

        User user = MgnlContext.getUser();
        if (!(user instanceof MgnlUser)){
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("actions.editUserProfile.not.available", dialogName));
            return;
        }
        final JcrNodeAdapter itemToEdit = new JcrNodeAdapter(SessionUtil.getNode(RepositoryConstants.USERS, ((MgnlUser)user).getPath()));
        formDialogPresenter.start(itemToEdit, dialogName, uiContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(itemToEdit.getWorkspace(), itemToEdit.getItemId()));
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
    }
}
