/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.security.app.dialog.action;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;

/**
 * Save role dialog action. Transforms nodes added by {@link info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory} to its final representation.
 */
public class SaveRoleDialogAction extends SaveDialogAction {

    public SaveRoleDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback) {
        super(definition, item, validator, callback);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;

            try {

                final Node node = itemChanged.applyChanges();

                for (Node aclNode : NodeUtil.getNodes(node)) {

                    // Any node marked as using the intermediary format we read in, remove all its sub nodes and then
                    // add new sub nodes based on the read in ACL
                    if (aclNode.hasProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME)) {

                        AccessControlList acl = new AccessControlList();

                        for (Node entryNode : NodeUtil.getNodes(aclNode)) {

                            if (entryNode.hasProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME)) {
                                String path = entryNode.getProperty(AccessControlList.PATH_PROPERTY_NAME).getString();
                                long accessType = (int) entryNode.getProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME).getLong();
                                long permissions = entryNode.getProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME).getLong();

                                if (path.equals("/")) {
                                } else if (path.equals("/*")) {
                                    path = "/";
                                } else {
                                    path = StringUtils.removeEnd(path, "/*");
                                    path = StringUtils.removeEnd(path, "/");
                                }

                                if (StringUtils.isNotBlank(path)) {
                                    acl.addEntry(new AccessControlList.Entry(permissions, accessType, path));
                                }
                            }

                            entryNode.remove();
                        }

                        aclNode.setProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, (Value)null);
                        acl.saveEntries(aclNode);
                    }
                }

                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());

        } else {
            // validation errors are displayed in the UI.
        }
    }

}
