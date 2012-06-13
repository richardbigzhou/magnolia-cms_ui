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
package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;


/**
 * The Class VActionbarSection, which displays a section within the action bar.
 */
public class VActionbarSection extends FlowPanel {

    public static final String CLASSNAME = "v-actionbar-section";

    public static final String TITLE_TAGNAME = "h3";

    private final Element header = DOM.createElement("header");

    private String label = "";

    private int position;

    private final Map<String, VActionbarGroup> groups = new HashMap<String, VActionbarGroup>();

    private final HTMLPanel titleElement = new HTMLPanel(TITLE_TAGNAME, "");

    /**
     * Instantiates a new action bar section with given title.
     * 
     * @param title the section title
     */
    public VActionbarSection() {
        setStyleName(CLASSNAME);

        // header
        getElement().appendChild(header);
        titleElement.setStyleName("v-actionbar-section-title");

        add(titleElement, header);
    }

    /**
     * Gets the label.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * 
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
        updateLabel();
    }

    /**
     * Gets the position.
     * 
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position.
     * 
     * @param position the new position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Gets the groups.
     * 
     * @return the groups
     */
    public Map<String, VActionbarGroup> getGroups() {
        return groups;
    }

    /**
     * Adds an action group to this section.
     * 
     * @param group the action group
     */
    public void addGroup(VActionbarGroup group) {
        groups.put(group.getName(), group);
        add(group);
    }

    /**
     * Updates the title label.
     */
    public void updateLabel() {
        titleElement.getElement().setInnerText(getLabel());
    }

}
