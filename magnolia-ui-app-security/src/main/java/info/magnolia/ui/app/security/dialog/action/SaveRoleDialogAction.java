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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.action.SaveFormAction;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

/**
 * Save role dialog action.
 */
public class SaveRoleDialogAction extends SaveFormAction {

    public SaveRoleDialogAction(SaveRoleDialogActionDefinition definition, FormPresenter presenter) {
        super(definition, presenter);
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        getPresenter().showValidation(true);
        if (getPresenter().getView().isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) getItem();

            try {
                final Node node = itemChanged.getNode();
                // the ACL handling has to be added here, once the ACLs are (re)defined for M5
                MetaDataUtil.updateMetaData(node);
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            getPresenter().getCallback().onSuccess(getDefinition().getName());

        } else {
            //validation errors are displayed in the UI.
        }
    }

}
