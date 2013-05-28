/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.app.security.action;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.app.security.dialog.field.AclFieldDefinition;
import info.magnolia.ui.dialog.FormDialogPresenter;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Action for opening the role edit dialog.
 */
public class OpenRoleEditDialogAction extends AbstractAction<ActionDefinition> {

    private final JcrNodeAdapter itemToEdit;
    private final FormDialogPresenter formDialogPresenter;
    private final UiContext uiContext;
    private final EventBus eventBus;
    private RepositoryManager repositoryManager;

    @Inject
    public OpenRoleEditDialogAction(ActionDefinition definition, JcrNodeAdapter itemToEdit, FormDialogPresenter formDialogPresenter, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, RepositoryManager repositoryManager) {
        super(definition);
        this.itemToEdit = itemToEdit;
        this.formDialogPresenter = formDialogPresenter;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.repositoryManager = repositoryManager;
    }

    @Override
    public void execute() throws ActionExecutionException {

        ConfiguredDialogDefinition dialogDefinition;

        try {

            // We read the definition from the JCR directly rather than getting it from the registry and then clone it
            Node node = MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getNode("/modules/ui-security-app/dialogs/roleEdit");
            dialogDefinition = (ConfiguredDialogDefinition) Components.getComponent(Node2BeanProcessor.class).toBean(node, DialogDefinition.class);

            if (dialogDefinition == null) {
                throw new ActionExecutionException("Unable to load dialog [roleEdit]");
            }

            List<TabDefinition> tabs = dialogDefinition.getForm().getTabs();
            for (TabDefinition tab : tabs) {
                if (tab.getName().equals("acls")) {

                    ArrayList<String> workspaceNames = new ArrayList<String>(repositoryManager.getWorkspaceNames());
                    Collections.sort(workspaceNames);
                    for (String workspaceName : workspaceNames) {

                        if (workspaceName.equals("mgnlVersion") || workspaceName.equals("mgnlSystem")) {
                            continue;
                        }

                        AclFieldDefinition field = new AclFieldDefinition();
                        field.setName(workspaceName);
                        field.setLabel(StringUtils.capitalize(workspaceName));
                        field.setAclName("acl_" + workspaceName);

                        tab.getFields().add(field);
                    }
                }
            }

        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        } catch (Node2BeanException e) {
            throw new ActionExecutionException(e);
        }

        formDialogPresenter.start(itemToEdit, dialogDefinition, uiContext, new EditorCallback() {

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
