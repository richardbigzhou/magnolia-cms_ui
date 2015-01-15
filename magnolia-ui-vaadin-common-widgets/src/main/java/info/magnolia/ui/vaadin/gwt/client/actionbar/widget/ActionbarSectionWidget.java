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
package info.magnolia.ui.vaadin.gwt.client.actionbar.widget;

import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The client-side widget for a section of the action bar.
 */
public class ActionbarSectionWidget extends FlowPanel {

    public static final String CLASSNAME = "v-actionbar-section";

    public static final String TITLE_TAGNAME = "h3";

    private final Element heading = DOM.createElement("h3");

    private final ActionbarSection data;

    private final Map<String, VActionbarGroup> groups = new LinkedHashMap<String, VActionbarGroup>();

    private Widget preview;

    /**
     * Instantiates a new action bar section with given data.
     *
     * @param data the data
     */
    public ActionbarSectionWidget(ActionbarSection data) {
        this.data = data;
        setStyleName(CLASSNAME);
        heading.addClassName("v-actionbar-section-title");
        getElement().appendChild(heading);
        update();
    }

    public String getName() {
        return data.getName();
    }

    public Map<String, VActionbarGroup> getGroups() {
        return groups;
    }

    public void addGroup(VActionbarGroup group) {
        groups.put(group.getName(), group);
        if (this.preview != null) {
            int idx = Math.max(getWidgetIndex(preview), 0);
            insert(group, idx);
        } else {
            add(group);
        }
    }

    public void setPreview(Widget preview) {
        if (this.preview != null) {
            remove(this.preview);
        }
        add(preview);
        this.preview = preview;
    }

    public void update() {
        heading.setInnerText(data.getName());
        heading.setInnerText(data.getCaption());
    }

    public ActionbarSection getData() {
        return data;
    }

}
