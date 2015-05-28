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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Table;

/**
 * Column formatter for displaying the activation status of an item. Creates icons that represents the activation and
 * permission status. Use the definition to configure which icons should be included.
 */
public class StatusColumnFormatter extends AbstractColumnFormatter<StatusColumnDefinition> {    

    private static List<String> DEFAULT_MESSAGES =  Arrays.asList("activated", "modified", "not-activated");
    
    @Inject
    public StatusColumnFormatter(StatusColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {

        final Item jcrItem = getJcrItem(source, itemId);
        List<String> messages = new ArrayList<String>();
        if (jcrItem != null && jcrItem.isNode()) {
            Node node = (Node) jcrItem;

            String activationStatus = "";
            String permissionStatus = "";
            String activationStatusMessage="";
            
            messages = new ArrayList<String>(DEFAULT_MESSAGES);
            if(!StringUtils.isEmpty(definition.getMessages())){               
                String[] messageArr = definition.getMessages().split(",");
                if(!ArrayUtils.isEmpty(messageArr)){
                    messages = Arrays.asList(messageArr);
                }
            }
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
                    activationStatusMessage = messages.get(1)!=null?messages.get(1):DEFAULT_MESSAGES.get(1);
                    break;
                case NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED:
                    activationStatus += "color-green";
                    activationStatusMessage = messages.get(1)!=null?messages.get(0):DEFAULT_MESSAGES.get(0);
                    break;
                default:
                    activationStatus += "color-red";
                    activationStatusMessage = messages.get(1)!=null?messages.get(2):DEFAULT_MESSAGES.get(2);
                }
                activationStatus = "<span class=\"" + activationStatus + "\"></span>";
                activationStatus = activationStatus + "<span class=\"hidden\">" + activationStatusMessage + "</span>";
            }

            // permission status
            if (definition.isPermissions()) {
                try {
                    node.getSession().checkPermission(node.getPath(), Session.ACTION_ADD_NODE + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY);
                } catch (AccessControlException e) {
                    permissionStatus = "<span class=\"icon-read-only\"></span>";
                } catch (RepositoryException e) {
                    throw new RuntimeException("Could not access the JCR permissions for the following node identifier " + itemId, e);
                }
            }

            return activationStatus + permissionStatus;
        }
        return null;
    }

}
