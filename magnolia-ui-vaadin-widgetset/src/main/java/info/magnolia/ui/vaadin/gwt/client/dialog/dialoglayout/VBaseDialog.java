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
package info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout;

import java.util.Iterator;
import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * VBaseDialog.
 */
public class VBaseDialog extends Composite implements VAbstractDialog, Container, ClientSideHandler, HasWidgets, VBaseDialogView.Presenter {

    private final VBaseDialogView view = createView();
    
    private final ClientSideProxy proxy = new ClientSideProxy(this) {{
        register("addAction", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String name = String.valueOf(params[0]);
                final String label = String.valueOf(params[1]);
                getView().addAction(name, label);
            }
        });

        register("setDescription", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String description = String.valueOf(params[0]);
                getView().setDescription(description);
            }
        });

        register("setCaption", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String caption = String.valueOf(params[0]);
                getView().setCaption(caption);
            }
        }); 
        
        register("setActionLabel", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String actionName = String.valueOf(params[0]);
                final String label = String.valueOf(params[1]);
                getView().setActionLabel(actionName, label);
            }
        }); 
    }};
    
    

    public VBaseDialog() {
        initWidget(view.asWidget());
        view.setPresenter(this);
    }
    
    public VBaseDialogView getView() {
        return view;
    }
    
    protected VBaseDialogView createView() {
        return new VBaseDialogViewImpl();
    }
    
    protected ClientSideProxy getProxy() {
        return proxy;
    }
    
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        final Paintable contentPaintable = client.getPaintable(uidl.getChildUIDL(0));
        final Widget contentWidget = (Widget)contentPaintable;
        view.setContent(contentWidget);
        contentPaintable.updateFromUIDL(uidl.getChildUIDL(0), client);
        proxy.update(this, uidl, client);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (view != null) {
            view.asWidget().setHeight(height);
        }
    }
    
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        view.asWidget().setWidth(width);
    }
    
    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return view.getContent() == component;
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        if (uidl.hasAttribute("caption")) {
            view.setCaption(uidl.getStringAttribute("caption"));
        }
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        return new RenderSpace(view.getContentWidth(), view.getContentHeight());
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unknown call from server " + method);
    }

    @Override
    public void add(Widget w) {
        view.add(w);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return view.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return view.remove(w);
    }

    @Override
    public void updateErrorAmount() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fireAction(String action) {
        getProxy().call("fireAction", action);
        
    }

    @Override
    public void closeDialog() {
        getProxy().call("closeDialog");
    }

}
