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
package info.magnolia.ui.widget.actionbar;

import info.magnolia.ui.widget.actionbar.gwt.client.VActionButton;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.NativeButton;


/**
 * The ActionButton vaadin component, which goes inside the action bar.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VActionButton.class, loadStyle = LoadStyle.EAGER)
public class ActionButton extends NativeButton {

    private String sectionTitle;

    private String groupName;

    private String actionName;

    /**
     * Instantiates a new action button.
     * 
     * @param caption the caption
     */
    public ActionButton(final String caption) {
        super(caption);
        // addStyleName("v-action-button");
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
     * Sets the section title.
     * 
     * @param sectionTitle the new section title
     */
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
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
     * Sets the group name.
     * 
     * @param groupName the new group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Gets the action name.
     * 
     * @return the action name
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Sets the action name.
     * 
     * @param actionName the new action name
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("sectionTitle", sectionTitle);
        target.addAttribute("groupName", groupName);
        target.addAttribute("actionName", actionName);
    }

}
