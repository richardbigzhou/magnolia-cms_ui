/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.widget.actionbar.gwt.client;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VNativeButton;


/**
 * The client side implementation of ActionButton.
 */
public class VActionButton extends VNativeButton {

    private String sectionTitle;

    private String groupName;

    private String actionName;

    public VActionButton() {
        super();
        setStylePrimaryName("v-action-button");
    }

    /**
     * Gets the section title.
     * 
     * @return the section title
     */
    public String getSectionTitle() {
        return sectionTitle;
    }

    /**
     * Gets the group name.
     * 
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets the action name.
     * 
     * @return the action name
     */
    public String getActionName() {
        return actionName;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        updateNames(uidl.getStringAttribute("sectionTitle"),
            uidl.getStringAttribute("groupName"),
            uidl.getStringAttribute("actionName"));
    }

    private void updateNames(String sectionTitle, String groupName, String actionName) {
        this.sectionTitle = sectionTitle;
        this.groupName = groupName;
        this.actionName = actionName;
    }
}
