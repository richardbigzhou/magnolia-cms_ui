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
package info.magnolia.ui.vaadin.gwt.client.tabsheet;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.TabSetChangedEvent.Handler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.TabSetChangedEvent.HasTabSetChangedHandlers;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEventHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent.HasActiveTabChangeHandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
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
 * Client side implementation of the simple tab sheet.
 */
public class VMagnoliaTabSheet extends Composite implements HasWidgets, VMagnoliaTabSheetView.Presenter, 
    Container, ClientSideHandler, HasTabSetChangedHandlers, HasActiveTabChangeHandlers {

    protected String paintableId;

    protected ApplicationConnection client;

    private VMagnoliaTabSheetView view;

    private EventBus eventBus = new SimpleEventBus();

    private ClientSideProxy proxy;

    public VMagnoliaTabSheet() {
        super();
        this.view = createView();
        this.proxy = createProxy();

        eventBus.addHandler(TabCloseEvent.TYPE, new TabCloseEventHandler() {
            @Override
            public void onTabClosed(TabCloseEvent event) {
                closeTab(event.getTab());
            }
        });

        eventBus.addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedEvent.Handler() {
            
            @Override
            public void onActiveTabChanged(ActiveTabChangedEvent event) {
                view.setActiveTab(event.getTab());
                if (event.isNotifyServer()) {
                    proxy.call("activateTab", event.getTab().getTabId());
                }
            }
        });

        eventBus.addHandler(ShowAllTabsEvent.TYPE, new ShowAllTabsHandler() {

            @Override
            public void onShowAllTabs(ShowAllTabsEvent event) {
                view.showAllTabContents(true);
                view.getTabContainer().showAll(true);
            }

        });

        initWidget(view.asWidget());
    }

    protected VMagnoliaTabSheetView createView() {
        return new VMagnoliaTabSheetViewImpl(eventBus, this);
    }

    protected ClientSideProxy createProxy() {
        return new ClientSideProxy(this) {
            {
                register("setActiveTab", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final VMagnoliaTab tab = view.getTabById(String.valueOf(params[0]));
                        if (tab != null) {
                            eventBus.fireEvent(new ActiveTabChangedEvent(tab, false));
                        }
                    }
                });

                register("closeTab", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {

                    }
                });

                register("setActiveTabFullscreen", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        boolean isFullscreen = (Boolean)params[0];
                        view.setShowActiveTabFullscreen(isFullscreen);
                        client.runDescendentsLayout(VMagnoliaTabSheet.this);
                    }
                });

                register("addShowAllTab", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        boolean showAll = (Boolean) params[0];
                        String label = String.valueOf(params[1]);
                        view.getTabContainer().addShowAllTab(showAll, label);
                    }
                });

            }};
    }

    protected void closeTab(final VMagnoliaTab tab) {
        if (tab != null) {
            client.unregisterPaintable(tab);
            proxy.call("closeTab", tab.getTabId());
            view.removeTab(tab);
        }
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.paintableId = uidl.getId();
        this.client = client;
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        updateTabs(uidl);
        proxy.update(this, uidl, client);
    }
    
    private void updateTabs(final UIDL uidl) {
        final UIDL tabsUidl = uidl.getChildByTagName("tabs");
        if (tabsUidl != null) {
            final Iterator<?> it = tabsUidl.getChildIterator();
            final List<VMagnoliaTab> possibleTabsToOrphan = new ArrayList<VMagnoliaTab>(view.getTabs());
            while (it.hasNext()) {
                final UIDL tabUidl = (UIDL) it.next();
                final Paintable tab = client.getPaintable(tabUidl);
                view.updateTab((VMagnoliaTab) tab);
                tab.updateFromUIDL(tabUidl, client);
                possibleTabsToOrphan.remove(tab);
            }

            for (final VMagnoliaTab tabToOrphan : possibleTabsToOrphan) {
                view.getTabs().remove(tabToOrphan);
                client.unregisterPaintable(tabToOrphan);
                view.removeTab(tabToOrphan);
            }
        }
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}

    @Override
    public boolean hasChildComponent(Widget component) {
        return view.getTabs().contains(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        if (component instanceof VMagnoliaTab) {
            view.getTabContainer().updateTab((VMagnoliaTab) component, uidl);
        }
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (child instanceof VMagnoliaTab && hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), view.getTabHeight((VMagnoliaTab)child));
        }
        return new RenderSpace();
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled method call from the server: " + method);
    }

    public List<VMagnoliaTab> getTabs() {
        return view.getTabs();
    }
    
    public VMagnoliaTab getActiveTab() {
        return view.getActiveTab();
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }

    protected VMagnoliaTabSheetView getView() {
        return view;
    }

    protected ClientSideProxy getProxy() {
        return proxy;
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
    public void updateLayout() {
        client.runDescendentsLayout(VMagnoliaTabSheet.this);
    }

    @Override
    public void updateLayoutOfActiveTab() {
        client.runDescendentsLayout(view.getActiveTab());
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        view.asWidget().setHeight(height);
    }

    @Override
    public HandlerRegistration addTabSetChangedHandlers(Handler handler) {
        return view.asWidget().addHandler(handler, TabSetChangedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addActiveTabChangedHandler(ActiveTabChangedEvent.Handler handler) {
        return view.asWidget().addHandler(handler, ActiveTabChangedEvent.TYPE);
    }

}
