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
package info.magnolia.ui.widget.tabsheet.client;


import java.util.Set;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Tab class for a tabsheet.
 * @author apchelintcev
 *
 */
public class VShellTab extends SimplePanel implements Container {

    protected String paintableId;
    
    protected ApplicationConnection client;
    
    private Paintable content;
    
    private String tabId = null;
    
    private boolean isClosable = false;
    
    public VShellTab() {
        super();
        setStyleName("v-shell-tab");
    }
    
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.paintableId = uidl.getId();
        this.client = client;
        if (!client.updateComponent(this, uidl, true)) {
            if (uidl.hasAttribute("shellTabId")) {
                tabId = uidl.getStringAttribute("shellTabId");
            }
            final Paintable content = client.getPaintable(uidl.getChildUIDL(0));
            if (this.content != content) {
                if (this.content != null) {
                    client.unregisterPaintable(this.content);
                }
                this.content = content;
                setWidget((Widget)content);
                content.updateFromUIDL(uidl.getChildUIDL(0), client);
            }
        }      
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return component == content;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace();
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {}

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}
    
    @Override
    public boolean requestLayout(Set<Paintable> children) {return false;}

    public String getTabId() {
        return tabId;
    }
    
    public void setClosable(boolean isClosable) {
        this.isClosable = isClosable;
    }

    public boolean isClosable() {
        return isClosable;
    }
}
