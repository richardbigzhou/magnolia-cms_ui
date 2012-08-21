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

import info.magnolia.ui.widget.actionbar.gwt.client.event.ActionTriggerEvent;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.terminal.gwt.client.ui.Icon;


/**
 * The Class VActionbarViewImpl, GWT implementation for the VActionbarView interface.
 */
public class VActionbarViewImpl extends ComplexPanel implements VActionbarView, ActionTriggerEvent.Handler {

    public static final String CLASSNAME = "v-actionbar";

    private final Element root = DOM.createElement("section");

    private final EventBus eventBus;

    private Presenter presenter;

    private final Map<String, VActionbarSection> sections = new LinkedHashMap<String, VActionbarSection>();

    public VActionbarViewImpl(final EventBus eventBus) {
        setElement(root);
        setStyleName(CLASSNAME);

        this.eventBus = eventBus;
        this.eventBus.addHandler(ActionTriggerEvent.TYPE, this);
    }

    @Override
    public Map<String, VActionbarSection> getSections() {
        return sections;
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addSection(VActionbarSectionJSO sectionParams) {
        VActionbarSection section = new VActionbarSection(sectionParams);
        sections.put(sectionParams.getName(), section);
        add(section, root);
    }

    @Override
    public void removeSection(String sectionName) {
        VActionbarSection section = sections.remove(sectionName);
        section.removeFromParent();
    }

    @Override
    public void addAction(VActionbarItemJSO actionParams, Icon icon, String groupName, String sectionName) {
        VActionbarSection section = sections.get(sectionName);
        if (section != null) {
            VActionbarGroup group = section.getGroups().get(groupName);
            if (group == null) {
                group = new VActionbarGroup(groupName);
                section.addGroup(group);
            }
            VActionbarItem action = new VActionbarItem(actionParams, eventBus, icon);
            group.addAction(action);
        }
    }

    @Override
    public void addAction(VActionbarItemJSO actionParams, String groupName, String sectionName) {
        VActionbarSection section = sections.get(sectionName);
        if (section != null) {
            VActionbarGroup group = section.getGroups().get(groupName);
            if (group == null) {
                group = new VActionbarGroup(groupName);
                section.addGroup(group);
            }
            VActionbarItem action = new VActionbarItem(actionParams, eventBus);
            group.addAction(action);
        }
    }

    @Override
    public void onActionTriggered(ActionTriggerEvent event) {
        VActionbarItem action = event.getSource();
        VActionbarSection section = getParentSection(action);
        presenter.triggerAction(section.getName() + ":" + action.getName());
    }

    private VActionbarSection getParentSection(VActionbarItem item) {
        // parent is group, grandparent is section
        return (VActionbarSection) item.getParent().getParent();
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        if (sections.containsValue(component)) {
            return true;
        } else {
            for (VActionbarSection section : sections.values()) {
                if (section.getWidgetIndex(component) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

}
