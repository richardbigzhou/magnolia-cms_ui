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
package info.magnolia.m5vaadin.shell.gwt.client;

import info.magnolia.m5vaadin.shell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.m5vaadin.shell.gwt.client.VShellMessage.MessageType;

import java.util.Iterator;
import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;


/**
 * Vaadin implementation of MagnoliaShell client side.
 */
@SuppressWarnings("serial")
public class VMagnoliaShell extends Composite implements HasWidgets, Container, ClientSideHandler, VMagnoliaShellView.Presenter {

    protected String paintableId;

    protected ApplicationConnection client;
       
    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("navigate", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String historyToken = String.valueOf(params[0]);
                    final String title = String.valueOf(params[1]);
                    view.navigate(historyToken, title);
                }
            });
            
            register("activeViewportChanged", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    view.changeActiveViewport();
                }
            });
            
            register("showMessage", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final MessageType type = MessageType.valueOf(String.valueOf(params[0]));
                    final String message = String.valueOf(params[1]);
                    view.showMessage(type, message);
                }
            });
        }
    };
    
    private final VMagnoliaShellView view;
            
    private final EventBus eventBus;
    
    public VMagnoliaShell() {
        super();
        eventBus = new SimpleEventBus();
        view = new VMagnoliaShellViewImpl(eventBus);
        view.setPresenter(this);
        initWidget(view.asWidget());
    }
    
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        
        updateShellAppViewport(uidl);
        updateAppViewport(uidl);
        updateDialogViewport(uidl);
        
        proxy.update(this, uidl, client);
    }
    
    private void updateDialogViewport(UIDL uidl) {
        final UIDL tagUidl = uidl.getChildByTagName("dialogViewport");
        if (tagUidl != null) {
            final UIDL viewportUidl = tagUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(viewportUidl);
            if (p instanceof VShellViewport) {
                final VShellViewport dialogViewport = (VShellViewport)p;
                view.updateDialogs(dialogViewport);
                p.updateFromUIDL(viewportUidl, client);
                if (dialogViewport.getWidgetCount() == 0) {
                    view.removeDialogViewport();   
                }
            }
        }
    }

    private void updateAppViewport(UIDL uidl) {
        final UIDL tagUidl = uidl.getChildByTagName("appViewport");
        if (tagUidl != null) {
            final UIDL viewportUidl = tagUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(viewportUidl);
            if (p instanceof VShellViewport) {
                view.updateAppViewport((VShellViewport)p);
                p.updateFromUIDL(viewportUidl, client);
            }
        }
    }
    
    private void updateShellAppViewport(UIDL uidl) {
        final UIDL tagUidl = uidl.getChildByTagName("shellAppViewport");
        if (tagUidl != null) {
            final UIDL viewportUidl = tagUidl.getChildUIDL(0);
            final Paintable p = client.getPaintable(viewportUidl);
            if (p instanceof VShellViewport) {
                view.updateShellAppViewport((VShellViewport)p);
                p.updateFromUIDL(viewportUidl, client);
            }
        }
    }

    @Override
    public void loadShellApp(final ShellAppType type, final String param) {
        proxy.call("activateShellApp", type.name().toLowerCase(), param);
    }
    
    @Override
    public void loadApp(String fragment) {
        proxy.call("activateApp", fragment);
    }
    
    @Override
    public void updateViewportLayout(VShellViewport viewport) {
        client.runDescendentsLayout(viewport);
    }
    
    @Override
    public boolean initWidget(Object[] params) {
        History.fireCurrentHistoryState();
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled RPC call from server: " + method);
    }
    
    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}

    @Override
    public boolean hasChildComponent(Widget component) {
        final Iterator<Widget> it = view.iterator();
        boolean result = false;
        while (it.hasNext() && !result) {
            result = component == it.next();
        }
        return result;
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {}

    @Override
    public boolean requestLayout(Set<Paintable> children) {return false;}
    
    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(view.getViewportWidth(), view.getViewportHeight());
        }
        return new RenderSpace();
    }

    @Override
    public void destroyChild(final Widget child) {
        if (child instanceof Paintable) {
            client.unregisterPaintable((Paintable)child);
        }
    }
    
    @Override
    public void closeCurrentApp() {
        proxy.call("closeCurrentApp");
    }
    
    @Override
    public void setWidth(String width) {
        view.asWidget().setWidth(width);
        super.setWidth(width);
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

}
