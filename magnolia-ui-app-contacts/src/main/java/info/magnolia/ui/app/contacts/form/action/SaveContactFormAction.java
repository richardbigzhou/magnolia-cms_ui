/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.app.contacts.form.action;

import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.action.SaveFormAction;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Action for saving contacts.
 * We currently can't rename the node on change.
 * This must be properly solved by passing the node Identifier to {@link info.magnolia.ui.admincentral.event.ContentChangedEvent}.
 *
 * See MGNLUI-226.
 * @see SaveContactFormActionDefinition
 */
public class SaveContactFormAction extends SaveFormAction {
    public SaveContactFormAction(SaveContactFormActionDefinition definition, FormPresenter presenter) {
        super(definition, presenter);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        getPresenter().showValidation(true);
        if (getPresenter().isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) getItem();

            try {
                final Node node = itemChanged.getNode();

                // Can't use this anymore, breaks when renaming node, ContentChangedEvent is still using the old path
                //generateUniqueNodeNameForContact(node);

                MetaDataUtil.updateMetaData(node);
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            getPresenter().getCallback().onSuccess(getDefinition().getName());
        }

    }

    // we have already utilities in core to generate unique names. Why not reuse them rather then inventing your own code?
    private void generateUniqueNodeNameForContact(final Node node) throws RepositoryException {
        String firstName = node.getProperty("firstName").getString();
        String lastName =  node.getProperty("lastName").getString();
        String newNodeName = (firstName.charAt(0) + lastName.replaceAll("\\s+", "")).toLowerCase();
        String parentPath = node.getParent().getPath();
        String newNodeAbsPath = NodeUtil.combinePathAndName(parentPath, newNodeName);
        int i = 1;

        while(node.getSession().itemExists(newNodeAbsPath)) {
            newNodeAbsPath = NodeUtil.combinePathAndName(parentPath, newNodeName + i);
            i++;
        }
        node.getSession().move(node.getPath(), newNodeAbsPath);
    }
}
