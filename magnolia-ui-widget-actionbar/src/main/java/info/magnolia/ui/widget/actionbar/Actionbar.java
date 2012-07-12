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
package info.magnolia.ui.widget.actionbar;

import info.magnolia.ui.widget.actionbar.gwt.client.VActionbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;


/**
 * The Actionbar widget, consisting of sections and groups of actions.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VActionbar.class, loadStyle = LoadStyle.EAGER)
public class Actionbar extends AbstractComponentContainer implements ActionbarView {

    private final List<Component> actionButtons = new ArrayList<Component>();

    public Actionbar() {
        setWidth("270px");
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        for (final Component child : actionButtons) {
            if (child instanceof ActionButton) {
                child.paint(target);
            }
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
    }

    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        if (!actionButtons.contains(c)) {
            actionButtons.add(c);
        }
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        actionButtons.remove(c);
    }

    @Override
    public void removeAllComponents() {
        super.removeAllComponents();
        actionButtons.clear();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        actionButtons.set(actionButtons.indexOf(oldComponent), newComponent);
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return actionButtons.iterator();
    }

    @Override
    public void addSection(String sectionName) {
    }

    @Override
    public void addGroup(String groupName, String sectionName) {
    }

    @Override
    public void addAction(String actionName, String groupName, String sectionName) {
    }

    @Override
    public void addPreview(Component component, String sectionName) {
    }

    @Override
    public void enable(String actionName) {
    }

    @Override
    public void enable(String actionName, String groupName) {
    }

    @Override
    public void enable(String actionName, String groupName, String sectionName) {
    }

    @Override
    public void enableGroup(String groupName) {
    }

    @Override
    public void enableGroup(String groupName, String sectionName) {
    }

    @Override
    public void disable(String actionName) {
    }

    @Override
    public void disable(String actionName, String groupName) {
    }

    @Override
    public void disable(String actionName, String groupName, String sectionName) {
    }

    @Override
    public void disableGroup(String groupName) {
    }

    @Override
    public void disableGroup(String groupName, String sectionName) {
    }

    @Override
    public void showSection(String sectionName) {
    }

    @Override
    public void hideSection(String sectionName) {
    }

}
