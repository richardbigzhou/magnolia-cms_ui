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
package info.magnolia.m5.dialog.gwt.client;

import info.magnolia.m5vaadin.tabsheet.client.VShellTabSheet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Vaadin implementation of MagnoliaShell client side.
 */
@SuppressWarnings("serial")
public class VDialog extends FlowPanel implements Container, HasWidgets {

    protected String paintableId;

    private List<Paintable> paintables = new LinkedList<Paintable>();

    protected Element dialogView;

    protected ApplicationConnection client;
    private VShellTabSheet tabsheet;

    //private ClientSideProxy proxy = new ClientSideProxy(this) {};

    public VDialog() {
        setStylePrimaryName("dialog-panel");
        this.dialogView = getElement();

    }


    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        updateTabs(uidl);

        //proxy.update(this, uidl, client);
    }

    private void updateTabs(UIDL uidl) {
        final UIDL tagUidl = uidl.getChildByTagName("dialogTabsheet");
        if (tagUidl != null) {
            final UIDL dialogUidl = tagUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(dialogUidl);
            if (p instanceof VShellTabSheet) {
                VShellTabSheet tsheet = (VShellTabSheet)p;
                if (tabsheet == null) {
                    this.tabsheet = tsheet;
                    add(tsheet);
                }
                tsheet.updateFromUIDL(dialogUidl, client);
            }
        }
    }

    @Override
    protected void add(final Widget child, Element container) {
        if (child instanceof VShellTabSheet) {
            this.tabsheet = (VShellTabSheet)child;
            super.add(child, container);
        }
    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#replaceChildComponent(com.google.gwt.user.client.ui.Widget, com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#hasChildComponent(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean hasChildComponent(Widget component) {
        final Iterator<Widget> it = iterator();
        boolean result = false;
        while (it.hasNext() && !result) {
            result = component == it.next();
        }
        return result;
    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#updateCaption(com.vaadin.terminal.gwt.client.Paintable, com.vaadin.terminal.gwt.client.UIDL)
     */
    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.vaadin.terminal.gwt.client.Container#requestLayout(java.util.Set)
     */
    @Override
    public boolean requestLayout(Set<Paintable> children) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace();
    }


}
