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
package info.magnolia.ui.admincentral.column;

import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

/**
 * Status Column formatter.
 * Used to create activation and permission Icons based on the related Item.
 * Use the Definition to configure Icon's to be displayed.
 */
public class StatusColumnFormatter extends AbstractColumnFormatter<StatusColumnDefinition> {

    @Inject
    public StatusColumnFormatter(StatusColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {

        final JcrItemAdapter item = (JcrItemAdapter)source.getItem(itemId);
        Item jcrItem = item.getJcrItem();
        if(jcrItem != null && jcrItem.isNode()) {
            Node node = (Node) jcrItem;
            Integer status;
            Label activationStatus = null;
            Label permissionStatus = null;
            if (definition.isActivation()) {
                activationStatus = new Label();
                activationStatus.setSizeUndefined();
                activationStatus.setStyleName("icon-shape-circle");
                activationStatus.addStyleName("activation-status");
                // Get Status
                String color = "";
                status = MetaDataUtil.getMetaData(node).getActivationStatus();
                switch (status) {
                    case MetaData.ACTIVATION_STATUS_MODIFIED:
                        color = "color-yellow";
                        break;
                    case MetaData.ACTIVATION_STATUS_ACTIVATED:
                        color = "color-green";
                        break;
                    default:
                        color = "color-red";
                    }

                activationStatus.addStyleName(color);
            }
            if (definition.isPermissions()) {
                try {
                    permissionStatus = new Label();
                    permissionStatus.setSizeUndefined();
                    permissionStatus.setStyleName("icon-edit");
                    // TODO dlipp: verify, this shows the same behavior as old Content-API based
                    // implementation:
                    // if (permissions && !node.isGranted(info.magnolia.cms.security.Permission.WRITE))
                    node.getSession().checkPermission(node.getPath(), Session.ACTION_SET_PROPERTY);
                    permissionStatus.addStyleName("color-blue");
                }
                catch (RepositoryException e) {
                    // does not have permission to set properties - in that case will return two Icons
                    // in a layout for being displayed...
                    permissionStatus.addStyleName("color-red");
                }
            }
            if (definition.isActivation() && definition.isPermissions()) {
                CssLayout root = new CssLayout();
                root.addComponent(activationStatus);
                root.addComponent(permissionStatus);
                return root;
            }
            else if (definition.isActivation()) {
                return activationStatus;
            }
            else if (definition.isPermissions()) {
                return permissionStatus;
            }
            return new CssLayout();
        }
        return null;
    }

}
