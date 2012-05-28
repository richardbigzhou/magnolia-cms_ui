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
package info.magnolia.ui.widget.dialog.gwt.client;

import info.magnolia.ui.widget.tabsheet.gwt.client.VShellTabSheet;

import java.util.Set;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Vaadin implementation of Dialog client side (Presenter).
 */
@SuppressWarnings("serial")
public class VDialog extends Composite implements Container, VDialogView.Presenter {

    protected String paintableId;

    protected ApplicationConnection client;

    private final VDialogView view;

    private final EventBus eventBus;
    //private ClientSideProxy proxy = new ClientSideProxy(this) {};

    public VDialog() {
        eventBus = new SimpleEventBus();
        this.view = new VDialogViewImpl(eventBus);
        this.view.setPresenter(this);
        initWidget(view.asWidget());

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
                VShellTabSheet tabsheet = (VShellTabSheet)p;
                if (this.view.getTabSheet() == null) {
                    this.view.setTabSheet(tabsheet);
                }
                tabsheet.updateFromUIDL(dialogUidl, client);
            }
        }
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {}

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace();
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return view.hasChildComponent(component);
    }

}
