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
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.action.SaveFormAction;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

/**
 * Save group dialog action.
 */
public class SaveGroupDialogAction extends SaveFormAction {

    private static final Logger log = LoggerFactory.getLogger(SaveGroupDialogAction.class);

    public SaveGroupDialogAction(SaveGroupDialogActionDefinition definition, FormPresenter presenter) {
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
                // the roles (that are assigned to this group) and groups (this group belongs to) handling has to be added here
                // GROUPS
                String _ids = itemChanged.getItemProperty("groups").getValue().toString();
                _ids = StringUtils.remove(_ids, '[');
                _ids = StringUtils.remove(_ids, ']');
                String[] ids = StringUtils.split(_ids, ',');
                try {
                    node.getProperty("groups").remove();
                } catch (Exception ex) {
                    log.warn("Cannot remove [groups] property of the group ["+node.getName()+"]: "+ex.getMessage());
                }
                try {
                    // create "groups" subnode (or get it, if it already exists)
                    Node grps = NodeUtil.createPath(node, "groups", MgnlNodeType.NT_CONTENTNODE);
                    // sanity: remove all possible non-jcr properties
                    PropertyIterator pi = grps.getProperties();
                    while (pi.hasNext()) {
                        javax.jcr.Property p = pi.nextProperty();
                        if (!p.getName().startsWith("jcr:")) {
                            p.remove();
                        }
                    }
                    // add new groups
                    int i = 0;
                    for (String id : ids) {
                        PropertyUtil.setProperty(grps, ""+i, id.trim());
                        i++;
                    }
                } catch (Exception ex) {
                    log.error("Error saving assigned groups of the ["+node.getName()+"] group: "+ex.getMessage());
                    log.debug("Error saving assigned groups of the ["+node.getName()+"] group.",ex);
                }
                // ROLES
                _ids = itemChanged.getItemProperty("roles").getValue().toString();
                _ids = StringUtils.remove(_ids, '[');
                _ids = StringUtils.remove(_ids, ']');
                ids = StringUtils.split(_ids, ',');
                try {
                    node.getProperty("roles").remove();
                } catch (Exception ex) {
                    log.warn("Cannot remove [roles] property of the group ["+node.getName()+"]: "+ex.getMessage());
                }
                try {
                    // create "groups" subnode (or get it, if it already exists)
                    Node grps = NodeUtil.createPath(node, "roles", MgnlNodeType.NT_CONTENTNODE);
                    // sanity: remove all possible non-jcr properties
                    PropertyIterator pi = grps.getProperties();
                    while (pi.hasNext()) {
                        javax.jcr.Property p = pi.nextProperty();
                        if (!p.getName().startsWith("jcr:")) {
                            p.remove();
                        }
                    }
                    // add new groups
                    int i = 0;
                    for (String id : ids) {
                        PropertyUtil.setProperty(grps, ""+i, id.trim());
                        i++;
                    }
                } catch (Exception ex) {
                    log.error("Error saving assigned roles of the ["+node.getName()+"] group: "+ex.getMessage());
                    log.debug("Error saving assigned roles of the ["+node.getName()+"] group.",ex);
                }
                // THE REST
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
