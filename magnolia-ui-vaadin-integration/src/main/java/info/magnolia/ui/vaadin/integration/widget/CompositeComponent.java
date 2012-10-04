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
package info.magnolia.ui.vaadin.integration.widget;

import info.magnolia.ui.vaadin.integration.widget.layout.LightLayout;

import java.util.Iterator;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;


/**
 * The CompositeComponent is a lightened version of vaadin's CustomComponent. It still abstracts
 * layout capabilities for server-side extending components, but has no client-side representation.
 * Instead, it delegates the vaadin paint mechanism straight to its composition root.
 */
@SuppressWarnings("serial")
public class CompositeComponent extends AbstractComponentContainer {

    protected final LightLayout root = new LightLayout();

    @Override
    public void paint(PaintTarget target) throws PaintException {
        root.paint(target);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        root.paintContent(target);
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return new Iterator<Component>() {

            private boolean first = true;

            @Override
            public boolean hasNext() {
                return first;
            }

            @Override
            public Component next() {
                first = false;
                return root;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public String getStyleName() {
        return root.getStyleName();
    }

    @Override
    public void addStyleName(String style) {
        root.addStyleName(style);
    }

    @Override
    public void setStyleName(String style) {
        root.addStyleName(style);
    }

    @Override
    public void removeStyleName(String style) {
        root.removeStyleName(style);
    }

    /**
     * Gets the number of contained components. Consistent with the iterator returned by
     * {@link #getComponentIterator()}.
     * 
     * @return the number of contained components (zero or one)
     */
    public int getComponentCount() {
        return 1;
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addComponent(Component c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveComponentsFrom(ComponentContainer source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllComponents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeComponent(Component c) {
        throw new UnsupportedOperationException();
    }

}