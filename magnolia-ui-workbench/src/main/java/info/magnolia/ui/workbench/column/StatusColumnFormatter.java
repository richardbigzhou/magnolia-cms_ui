/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.workbench.column;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.workbench.column.definition.StatusColumnDefinition;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlException;

import com.vaadin.ui.Table;

/**
 * Column formatter for displaying the activation status of an item. Creates icons that represents the activation and
 * permission status. Use the definition to configure which icons should be included.
 */
public class StatusColumnFormatter extends AbstractColumnFormatter<StatusColumnDefinition> {

    @Inject
    public StatusColumnFormatter(StatusColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {

        final Item jcrItem = getJcrItem(source, itemId);
        if (jcrItem != null && jcrItem.isNode()) {
            Node node = (Node) jcrItem;

            String activationStatus = "";
            String permissionStatus = "";

            // activation status
            if (definition.isActivation()) {
                activationStatus += "icon-shape-circle activation-status ";

                Integer status;
                try {
                    status = NodeTypes.Activatable.getActivationStatus(node);
                } catch (RepositoryException e) {
                    status = NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED;
                }

                switch (status) {
                case NodeTypes.Activatable.ACTIVATION_STATUS_MODIFIED:
                    activationStatus += "color-yellow";
                    break;
                case NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED:
                    activationStatus += "color-green";
                    break;
                default:
                    activationStatus += "color-red";
                }
                activationStatus = "<span class=\"" + activationStatus + "\"></span>";
            }

            // permission status
            if (definition.isPermissions()) {
                try {
                    permissionStatus += "icon-edit ";
                    node.getSession().checkPermission(node.getPath(), Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY);
                    permissionStatus += "color-blue";
                } catch (AccessControlException e) {
                    // does not have permission to set properties - in that case will return two Icons
                    // in a layout for being displayed...
                    permissionStatus += "color-red";
                } catch (RepositoryException e) {
                    throw new RuntimeException("Could not access the JCR permissions for the following node identifier " + itemId, e);
                }
                permissionStatus = "<span class=\"" + permissionStatus + "\"></span>";
            }

            return activationStatus + permissionStatus;
        }
        return null;
    }

}
