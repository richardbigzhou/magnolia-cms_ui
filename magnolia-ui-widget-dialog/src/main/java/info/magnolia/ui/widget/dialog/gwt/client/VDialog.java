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

import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.HelpAccessibilityEvent;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VHelpAccessibilityNotifier;
import info.magnolia.ui.widget.tabsheet.gwt.client.VShellTabSheet;

import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;


/**
 * Vaadin implementation of Dialog client side (Presenter).
 */
@SuppressWarnings("serial")
public class VDialog extends Composite implements VHelpAccessibilityNotifier, Container, VDialogView.Presenter, ClientSideHandler {

    protected String paintableId;

    protected ApplicationConnection client;

    private final VDialogView view;

    private boolean isHelpAccessible = false;
    
    private final EventBus eventBus;
    
    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("addAction", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String name = String.valueOf(params[0]);
                    final String label = String.valueOf(params[1]);
                    view.addAction(name, label);
                }
            });
            register("setDescription", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String description = String.valueOf(params[0]);
                    view.setDescription(description);
                }
            });
        }
    };

    public VDialog() {
        eventBus = new SimpleEventBus();
        this.view = new VDialogViewImpl(eventBus);
        initWidget(view.asWidget());
        view.setPresenter(this);

    }


    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        updateTabs(uidl);

        proxy.update(this, uidl, client);
    }

    private void updateTabs(UIDL uidl) {
        final UIDL tagUidl = uidl.getChildByTagName("tabsheet");
        if (tagUidl != null) {
            final UIDL dialogUidl = tagUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(dialogUidl);
            if (p instanceof VShellTabSheet) {
                final VShellTabSheet tabsheet = (VShellTabSheet) p;
                if (view.getTabSheet() == null) {
                    view.addTabSheet(tabsheet);
                }
                 this.view.getTabSheet().updateFromUIDL(dialogUidl, client);
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

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled RPC call from server: " + method);
    }


    /* (non-Javadoc)
     * @see info.magnolia.ui.widget.dialog.gwt.client.VDialogView.Presenter#fireAction(java.lang.String)
     */
    @Override
    public void fireAction(String action) {
        proxy.call("fireAction", action);
    }


    /* (non-Javadoc)
     * @see info.magnolia.ui.widget.dialog.gwt.client.VDialogView.Presenter#closeDialog()
     */
    @Override
    public void closeDialog() {
        proxy.call("closeDialog");
    }


    VHelpAccessibilityNotifier.Delegate delegate = new Delegate();
    
    @Override
    public HandlerRegistration addHelpAccessibilityHandler(HelpAccessibilityEvent.Handler handler) {
        return delegate.addHelpAccessibilityHandler(handler);
    }


    @Override
    public void changeHelpAccessibility(boolean isEnabled) {
        delegate.changeHelpAccessibility(isHelpAccessible);
    }


    @Override
    public void notifyOfHelpAccessibilityChange(boolean isAccessible) {
        isHelpAccessible = !isHelpAccessible;
        changeHelpAccessibility(isHelpAccessible);
    }
}
