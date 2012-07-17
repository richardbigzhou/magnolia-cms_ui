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
package info.magnolia.ui.widget.dialog;

import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VDialogLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * Dialog layout server side implementation. 
 */
@ClientWidget(value = VDialogLayout.class, loadStyle = LoadStyle.EAGER)
public class DialogLayout extends AbstractLayout {

    private List<Component> components = new LinkedList<Component>();
    
    private Map<Component, String> helpDescriptions = new HashMap<Component, String>();
    
    public DialogLayout() {
        addStyleName("v-dialog-layout");
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        final Iterator<Component> it = getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            target.startTag("component");
            c.addStyleName("v-dialog-field");
            c.setSizeUndefined();
            c.paint(target);
            if (helpDescriptions.containsKey(c)) {
                target.addAttribute("helpDescription", helpDescriptions.get(c));
            }
            target.endTag("component");
        }
    }
    
    public void setComponentHelpDescription(Component c, String description) {
        if (components.contains(c)) {
            helpDescriptions.put(c, description);
            requestRepaint();   
        } else {
            throw new IllegalArgumentException("Layout doesn't contain this component.");
        }
    }
    
    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        components.add(c);
        requestRepaint();
    }
    
    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {}

    @Override
    public Iterator<Component> getComponentIterator() {
        return components.iterator();
    }

}
