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

import java.util.LinkedList;
import java.util.List;

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

    private String title = "";

    private final List<VActionbarGroup> groups = new LinkedList<VActionbarGroup>();

    /**
     * Instantiates a new action bar section with given title.
     * 
     * @param title the section title
     */
    public VActionbarSection() {
        init();
    }

    /**
     * Instantiates a new action bar section with given title.
     * 
     * @param title the section title
     */
    public VActionbarSection(String title) {
        this.title = title;
        init();
    }

    private void init() {
        setStyleName(CLASSNAME);

        // header
        getElement().appendChild(header);
        add(new HTMLPanel(TITLE_TAGNAME, title), header);
    }

    /**
     * Gets the title.
     * 
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Updates the title.
     * 
     * @param title the new title
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * Updates the action groups.
     */
    public void updateGroups() {
        VActionbarGroup crudGroup = new VActionbarGroup();
        VActionbarGroup viewGroup = new VActionbarGroup();
        groups.add(crudGroup);
        groups.add(viewGroup);

        for (VActionbarGroup group : groups) {
            add(group);
            group.updateActions();
        }
    }

}
