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
package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * The Class VActionbarViewImpl, GWT implementation for the VActionbarView interface.
 */
public class VActionbarViewImpl extends ComplexPanel implements VActionbarView {

    public static final String CLASSNAME = "v-actionbar";

    private final Element root = DOM.createElement("section");

    private Presenter presenter;

    private final Map<String, VActionbarSection> sections = new HashMap<String, VActionbarSection>();

    public VActionbarViewImpl() {
        setElement(root);
        setStyleName(CLASSNAME);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    private VActionbarSection registerSection(final String sectionTitle) {
        VActionbarSection section = getSections().get(sectionTitle);
        if (section == null) {
            section = new VActionbarSection();
            section.setLabel(sectionTitle);
            addSection(section);
        }
        return section;
    }

    private VActionbarGroup registerGroup(final String groupName, VActionbarSection
        section) {
        VActionbarGroup group = section.getGroups().get(groupName);
        if (group == null) {
            group = new VActionbarGroup();
            group.setName(groupName);
            section.addGroup(group);
        }
        return group;
    }

    private VActionbarItem registerItem(final String actionName, VActionbarGroup
        group) {
        VActionbarItem item = group.getItems().get(actionName);
        if (item == null) {
            item = new VActionbarItem();
            item.setName(actionName);
            group.addItem(item);
        }
        return item;
    }

    public Map<String, VActionbarSection> getSections() {
        return sections;
    }

    public void addSection(VActionbarSection section) {
        sections.put(section.getLabel(), section);
        add(section);
    }

    @Override
    public void addActionButton(VActionButton button) {
        VActionbarSection section = registerSection(button.getSectionTitle());
        VActionbarGroup group = registerGroup(button.getGroupName(), section);
        VActionbarItem item = registerItem(button.getActionName(), group);
        item.add(button);
    }

    @Override
    public void clearAll() {
        // not yet implemented
    }

    @Override
    public void add(Widget w) {
        add(w, getElement());
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return sections.containsValue(component);
    }

}
