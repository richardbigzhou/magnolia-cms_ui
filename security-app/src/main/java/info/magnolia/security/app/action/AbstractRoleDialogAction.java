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
package info.magnolia.security.app.action;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.security.app.dialog.field.WorkspaceAccessFieldDefinition;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for actions that open dialogs for adding or editing roles.
 *
 * @param <D> the action definition type
 * @see OpenAddRoleDialogAction
 * @see OpenEditRoleDialogAction
 */
public abstract class AbstractRoleDialogAction<D extends ActionDefinition> extends AbstractAction<D> {

    private RepositoryManager repositoryManager;

    protected AbstractRoleDialogAction(D definition, RepositoryManager repositoryManager) {
        super(definition);
        this.repositoryManager = repositoryManager;
    }

    /**
     * Loads the dialog definition and adds access control fields for workspaces that have not been explicitly added.
     */
    protected FormDialogDefinition getDialogDefinition(String dialogName) throws ActionExecutionException {

        try {

            // We read the definition from the JCR directly rather than getting it from the registry and then clone it
            Node node = MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getNode("/modules/security-app/dialogs/" + dialogName);
            ConfiguredFormDialogDefinition dialogDefinition = (ConfiguredFormDialogDefinition) Components.getComponent(Node2BeanProcessor.class).toBean(node, FormDialogDefinition.class);

            if (dialogDefinition == null) {
                throw new ActionExecutionException("Unable to load dialog [" + dialogName + "]");
            }
            dialogDefinition.setId("security-app:" + dialogName);

            List<TabDefinition> tabs = dialogDefinition.getForm().getTabs();
            for (TabDefinition tab : tabs) {
                if (tab.getName().equals("acls")) {

                    ArrayList<String> workspaceNames = new ArrayList<String>(repositoryManager.getWorkspaceNames());
                    Collections.sort(workspaceNames);
                    for (String workspaceName : workspaceNames) {

                        if (workspaceName.equals("mgnlVersion") || workspaceName.equals("mgnlSystem")) {
                            continue;
                        }

                        String aclName = "acl_" + workspaceName;

                        boolean hasFieldForAcl = hasField(tab, aclName);

                        if (!hasFieldForAcl) {
                            WorkspaceAccessFieldDefinition field = new WorkspaceAccessFieldDefinition();
                            field.setName(aclName);
                            field.setLabel(StringUtils.capitalize(workspaceName));
                            field.setWorkspace(workspaceName);
                            field.setNodeTypes(getNodeTypesForWorkspace(workspaceName));
                            tab.getFields().add(field);
                        }
                    }
                }
            }

            return dialogDefinition;

        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        } catch (Node2BeanException e) {
            throw new ActionExecutionException(e);
        }
    }

    protected List<NodeTypeDefinition> getNodeTypesForWorkspace(String workspaceName) {

        List<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();

        if (workspaceName.equals(RepositoryConstants.WEBSITE)) {
            addNodeType(nodeTypes, NodeTypes.Content.NAME, "icon-file-webpage", false);
        } else if (workspaceName.equals(RepositoryConstants.CONFIG)) {
            addNodeType(nodeTypes, NodeTypes.ContentNode.NAME, "icon-node-content");
            addNodeType(nodeTypes, NodeTypes.Content.NAME, "icon-folder-l");
        } else if (workspaceName.equals(RepositoryConstants.USERS)) {
            addNodeType(nodeTypes, NodeTypes.Folder.NAME, "icon-folder-l");
            addNodeType(nodeTypes, NodeTypes.User.NAME, "icon-user-magnolia");
        } else if (workspaceName.equals(RepositoryConstants.USER_ROLES)) {
            addNodeType(nodeTypes, NodeTypes.Folder.NAME, "icon-folder-l");
            addNodeType(nodeTypes, NodeTypes.Role.NAME, "icon-user-role");
        } else if (workspaceName.equals(RepositoryConstants.USER_GROUPS)) {
            addNodeType(nodeTypes, NodeTypes.Folder.NAME, "icon-folder-l");
            addNodeType(nodeTypes, NodeTypes.Group.NAME, "icon-user-group");
        } else {
            // Let the field use a default set of node types instead
            return null;
        }

        return nodeTypes;
    }

    protected void addNodeType(List<NodeTypeDefinition> nodeTypes, String nodeTypeName, String icon) {
        addNodeType(nodeTypes, nodeTypeName, icon, true);
    }

    protected void addNodeType(List<NodeTypeDefinition> nodeTypes, String nodeTypeName, String icon, boolean strict) {
        ConfiguredNodeTypeDefinition nodeType = new ConfiguredNodeTypeDefinition();
        nodeType.setName(nodeTypeName);
        nodeType.setIcon(icon);
        nodeType.setStrict(strict);
        nodeTypes.add(nodeType);
    }

    private boolean hasField(TabDefinition tab, String name) {
        for (FieldDefinition fieldDefinition : tab.getFields()) {
            if (fieldDefinition.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
