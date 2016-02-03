/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.magnoliashell;

import java.util.Iterator;
import java.util.LinkedList;

import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;

/**
 * Component container showing only the most recently added component and keeping previously shown components in a stack
 * for easy switching to the previously shown.
 */
public class DeckLayout extends AbstractComponentContainer {

    private LinkedList<Component> children = new LinkedList<Component>();

    public void display(Component content) {
        if (content != null) {
            addComponent(content);
        }
    }

    @Override
    public void addComponent(Component c) {
        // Check first if this is the same as the currently shown, eliminates flicker
        if (children.isEmpty() || children.getLast() != c) {
            super.addComponent(c);
            if (children.contains(c)) {
                children.remove(c);
            }
            children.addLast(c);
            markAsDirty();
        }
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        children.remove(c);
        markAsDirty();
    }

    @Override
    public void removeAllComponents() {
        super.removeAllComponents();
        children.clear();
        markAsDirty();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        removeComponent(oldComponent);
        addComponent(newComponent);
    }

    public void pop() {
        if (!children.isEmpty()) {
            removeComponent(children.removeLast());
        }
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public int getComponentCount() {
        return children.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return children.iterator();
    }
}
