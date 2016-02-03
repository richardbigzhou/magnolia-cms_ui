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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;

/**
 * Save group dialog action.
 */
public class SaveGroupDialogAction extends SaveDialogAction {

    public SaveGroupDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback) {
        super(definition, item, validator, callback);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        // validation errors are displayed in the UI.
        if (validator.isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;

            try {
                final Node node = itemChanged.applyChanges();
                // the roles (that are assigned to this group) and groups (this group belongs to) handling have to be added here
                // GROUPS
                replacePropertyWithSubnode(node, "groups", itemPropertyToArray(itemChanged, "groups"));

                // ROLES
                replacePropertyWithSubnode(node, "roles", itemPropertyToArray(itemChanged, "roles"));

                // THE REST
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());
        }
    }

    private String[] itemPropertyToArray(JcrNodeAdapter item, String propertyName) {
        String identifiers = item.getItemProperty(propertyName).getValue().toString();
        identifiers = StringUtils.remove(identifiers, '[');
        identifiers = StringUtils.remove(identifiers, ']');
        return StringUtils.split(identifiers, ',');
    }

    private void replacePropertyWithSubnode(Node node, String name, String[] ids) throws RepositoryException {
        node.getProperty(name).remove();
        // create subnode (or get it, if it already exists)
        Node subnode = NodeUtil.createPath(node, name, NodeTypes.ContentNode.NAME);
        // sanity: remove all possible non-jcr properties
        PropertyIterator pi = subnode.getProperties();
        while (pi.hasNext()) {
            javax.jcr.Property p = pi.nextProperty();
            if (!p.getName().startsWith(NodeTypes.JCR_PREFIX) && !p.getName().startsWith(NodeTypes.MGNL_PREFIX)) {
                p.remove();
            }
        }
        // add new groups
        int i = 0;
        for (String id : ids) {
            PropertyUtil.setProperty(subnode, "" + i, id.trim());
            i++;
        }
    }

}
