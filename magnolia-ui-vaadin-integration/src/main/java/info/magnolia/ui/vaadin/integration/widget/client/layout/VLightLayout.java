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
package info.magnolia.ui.vaadin.integration.widget.client.layout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Client-side implementation for the LightLayout.
 */
public class VLightLayout extends FlowPanel implements Paintable, Container {

    private static final String CLASSNAME = "v-lightlayout";

    private ApplicationConnection client;

    public VLightLayout() {
        super();
        setStyleName(CLASSNAME);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        // for later requests
        this.client = client;

        final Collection<Widget> oldWidgets = new HashSet<Widget>();
        for (final Iterator<Widget> iterator = iterator(); iterator.hasNext();) {
            oldWidgets.add(iterator.next());
        }

        int lastIndex = 0;
        for (final Iterator<Object> i = uidl.getChildIterator(); i
            .hasNext();) {
            final UIDL r = (UIDL) i.next();
            final Paintable child = client.getPaintable(r);
            final Widget widget = (Widget) child;
            if (widget.getParent() == this) {
                oldWidgets.remove(child);
            }

            addOrMove(widget, lastIndex++);

            if (!r.getBooleanAttribute("cached")) {
                child.updateFromUIDL(r, client);
            }
        }

        // loop oldWidgets that where not re-attached and unregister them
        for (Widget w : oldWidgets) {
            remove(w);
            if (w instanceof Paintable) {
                final Paintable p = (Paintable) w;
                client.unregisterPaintable(p);
            }
        }
    }

    private void addOrMove(Widget child, int index) {
        if (child.getParent() == this) {
            int currentIndex = getWidgetIndex(child);
            if (index == currentIndex) {
                return;
            }
        }
        insert(child, index);
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return component.getParent() == this;
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        int index = getWidgetIndex(oldComponent);
        if (index >= 0) {
            remove(oldComponent);
            insert(newComponent, index);
        }
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return true;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        return null;
    }

}
