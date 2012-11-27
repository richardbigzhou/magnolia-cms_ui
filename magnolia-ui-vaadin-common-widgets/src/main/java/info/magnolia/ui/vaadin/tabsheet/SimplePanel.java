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
package info.magnolia.ui.vaadin.tabsheet;

import java.util.Iterator;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;


/**
 * Component container capable of holding a single component.
 */
public class SimplePanel extends AbstractComponentContainer {

    private ComponentContainer content;

    public SimplePanel() {
        super();
        this.content = new VerticalLayout();
    }

    public SimplePanel(ComponentContainer c) {
        super();
        setContent(c);
    }

    public void setContent(ComponentContainer content) {
        if (this.content != content) {
            if (this.content != null) {
                super.removeComponent(this.content);
            }
            if (content != null) {
                if (content.getParent() != null) {
                    content.setParent(null);
                }
                super.addComponent(content);   
            }
            this.content = content;
            requestRepaint();
        };
    }

    public ComponentContainer getContent() {
        return content;
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        if (content != null) {
            content.paint(target);
        }
    }

    @Override
    public void addComponent(Component c) {
        content.addComponent(c);
    }

    @Override
    public void removeComponent(Component c) {
        content.removeComponent(c);
    }

    @Override
    public void removeAllComponents() {
        content.removeAllComponents();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        content.replaceComponent(oldComponent, newComponent);
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        if (content != null) {
            return content.getComponentIterator();
        } else {
            return new Iterator<Component>() {
                
                @Override
                public void remove() {}
                
                @Override
                public Component next() {return null;}
                
                @Override
                public boolean hasNext() {
                    return false;
                }
            };   
        } 
    }
}
