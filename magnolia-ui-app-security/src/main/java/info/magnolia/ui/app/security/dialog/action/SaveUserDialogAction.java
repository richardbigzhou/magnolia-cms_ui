/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.security.dialog.action;

import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenter;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Save user dialog action.
 */
public class SaveUserDialogAction extends SaveDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveUserDialogAction.class);

    public SaveUserDialogAction(SaveUserDialogActionDefinition definition, FormDialogPresenter presenter) {
        super(definition, presenter);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        getPresenter().getForm().showValidation(true);
        if (getPresenter().getForm().isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) getItem();

            try {
                final Node node = itemChanged.getNode();
                // ENCRYPT PASSWORD
                String password = itemChanged.getItemProperty("password").getValue().toString();
                if (StringUtils.isNotBlank(password)) {
                    String encryptedPassword = SecurityUtil.getBCrypt(password);
                    PropertyUtil.setProperty(node, "password", encryptedPassword);
                }
                // ENABLED
                String enabled = itemChanged.getItemProperty("enabled").getValue().toString();
                PropertyUtil.setProperty(node, "enabled", enabled);
                // GROUPS
                try {
                    replacePropertyWithSubnode(node, "groups", itemPropertyToArray(itemChanged, "groups"));
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage(),ex);
                    throw new ActionExecutionException(ex.getMessage(),ex);
                }
                // ROLES
                try {
                    replacePropertyWithSubnode(node, "roles", itemPropertyToArray(itemChanged, "roles"));
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage(),ex);
                    throw new ActionExecutionException(ex.getMessage(),ex);
                }
                // THE REST
                NodeTypes.LastModified.update(node);
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            getPresenter().getCallback().onSuccess(getDefinition().getName());

        } else {
            //validation errors are displayed in the UI.
        }
    }

    private String[] itemPropertyToArray(JcrNodeAdapter item, String propertyName) {
        String identifiers = item.getItemProperty(propertyName).getValue().toString();
        identifiers = StringUtils.remove(identifiers, '[');
        identifiers = StringUtils.remove(identifiers, ']');
        return StringUtils.split(identifiers, ',');
    }

    private void replacePropertyWithSubnode(Node node, String name, String[] ids) throws RepositoryException {
        try {
            node.getProperty(name).remove();
        } catch (RepositoryException ex) {
            log.warn("Cannot remove ["+name+"] property of the user ["+node.getName()+"]: "+ex.getMessage());
        }
        try {
            // create subnode (or get it, if it already exists)
            Node subnode = NodeUtil.createPath(node, name, NodeTypes.ContentNode.NAME);
            // sanity: remove all possible non-jcr properties
            PropertyIterator pi = subnode.getProperties();
            while (pi.hasNext()) {
                javax.jcr.Property p = pi.nextProperty();
                if (!p.getName().startsWith(NodeTypes.JCR_PREFIX)) {
                    p.remove();
                }
            }
            // add new groups
            int i = 0;
            for (String id : ids) {
                PropertyUtil.setProperty(subnode, ""+i, id.trim());
                i++;
            }
        } catch (RepositoryException ex) {
            log.error("Error saving assigned "+name+" of the ["+node.getName()+"] user.",ex);
            throw new RepositoryException("Error saving assigned "+name+" of the ["+node.getName()+"] user.",ex);
        }
    }

}
