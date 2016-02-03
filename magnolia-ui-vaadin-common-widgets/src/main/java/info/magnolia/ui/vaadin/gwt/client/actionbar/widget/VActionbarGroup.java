/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class VActionbarGroup, which displays a group of actions within a section of the action bar.
 */
public class VActionbarGroup extends ComplexPanel {

    private static final String CLASSNAME = "v-actionbar-group";

    private boolean isOpenHorizontal = false;

    private final Element list = DOM.createElement("ul");

    private final List<ActionbarItemWidget> actions = new ArrayList<ActionbarItemWidget>();

    private final String name;

    /**
     * Instantiates a new action bar group.
     */
    public VActionbarGroup(String name) {
        setElement(list);
        setStyleName(CLASSNAME);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void toggleHorizontal() {
        isOpenHorizontal = !isOpenHorizontal;
        if (isOpenHorizontal) {
            addStyleName("open");
        } else {
            removeStyleName("open");
        }
    }

    public void setOpenHorizontally(boolean isOpenHorizontally) {
        this.isOpenHorizontal = isOpenHorizontally;
        if (isOpenHorizontal) {
            addStyleName("open");
        } else {
            removeStyleName("open");
        }
    }

    @Override
    public void add(Widget w) {
        add(w, getElement());
    }

    public List<ActionbarItemWidget> getActions() {
        return actions;
    }

    public int getNumActions() {
        return actions.size();
    }

    public void addAction(ActionbarItemWidget action) {
        actions.add(action);
        add(action);
    }

    public void update() {
    }
}
