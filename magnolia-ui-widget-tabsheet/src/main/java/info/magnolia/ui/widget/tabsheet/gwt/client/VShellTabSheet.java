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
package info.magnolia.ui.widget.tabsheet.gwt.client;


import info.magnolia.ui.widget.tabsheet.gwt.client.event.ActiveTabChangedEvent;
import info.magnolia.ui.widget.tabsheet.gwt.client.event.ActiveTabChangedHandler;
import info.magnolia.ui.widget.tabsheet.gwt.client.event.TabCloseEvent;
import info.magnolia.ui.widget.tabsheet.gwt.client.event.TabCloseEventHandler;
import info.magnolia.ui.widget.tabsheet.gwt.client.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.dom.client.Style.Display;
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
 * Client side implementation of the simple tab sheet.
 * @author apchelintcev
 *
 */
@SuppressWarnings("serial")
public class VShellTabSheet extends Composite implements VShellTabSheetView.Presenter, Container, ClientSideHandler {

    protected String paintableId;

    protected ApplicationConnection client;

    private final List<VShellTab> tabs = new LinkedList<VShellTab>();

    private VShellTab activeTab = null;

    VShellTabSheetView view;
    private final EventBus eventBus = new SimpleEventBus();

    public VShellTabSheet() {
        super();
        this.view = new VShellTabSheetViewImpl(eventBus);

        eventBus.addHandler(TabCloseEvent.TYPE, new TabCloseEventHandler() {
            @Override
            public void onTabClosed(TabCloseEvent event) {
                closeTab(event.getTab());
            }
        });

        eventBus.addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedHandler() {
            @Override
            public void onActiveTabChanged(ActiveTabChangedEvent event) {
                activateTab(event.getTab());
            }
        });
        initWidget(view.asWidget());
    }

    protected VShellTab getTabById(String tabId) {
        for (final VShellTab tab : tabs) {
            if (tab.getTabId().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }

    protected void closeTab(final VShellTab tab) {
        if (tab != null) {
            client.unregisterPaintable(tab);
            proxy.call("closeTab", tab.getTabId());
            if (activeTab == tab) {
                final VShellTab nextTab = CollectionUtil.getNext(tabs, tab);
                if (nextTab != null) {
                    doSetActiveTab(nextTab);
                }
            }
            tabs.remove(tab);
            view.remove(tab);
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
            final List<VShellTab> possibleTabsToOrphan = new ArrayList<VShellTab>(tabs);
            while (it.hasNext()) {
                final UIDL tabUidl = (UIDL)it.next();
                final Paintable tab = client.getPaintable(tabUidl);
                if (!tabs.contains(tab)) {
                    tabs.add((VShellTab)tab);
                    view.add((Widget)tab);
                }
                tab.updateFromUIDL(tabUidl, client);
                possibleTabsToOrphan.remove(tab);
            }

            for (final VShellTab tabToOrphan : possibleTabsToOrphan) {
                tabs.remove(tabToOrphan);
                client.unregisterPaintable(tabToOrphan);
                view.remove(tabToOrphan);
            }
        }
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}

    @Override
    public boolean hasChildComponent(Widget component) {
        return tabs.contains(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        if (component instanceof VShellTab) {
            view.getTabContainer().updateTab((VShellTab)component, uidl);
        }
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight() - view.getTabContainer().getOffsetHeight());
        }
        return new RenderSpace();
    }

    public ApplicationConnection getAppConnection() {
        return client;
    }

    private void setTabClosable(String tabId, boolean isClosable) {
        for (final VShellTab tab: tabs) {
            if (tab.getTabId().equals(tabId)) {
                boolean isAlreadyClosable = tab.isClosable();
                if (isAlreadyClosable != isClosable) {
                    tab.setClosable(isClosable);
                    view.getTabContainer().setTabClosable(tab, isClosable);
                    break;
                }
            }
        }
    }

    void activateTab(final VShellTab tab) {
        doSetActiveTab(tab);
        proxy.call("activateTab", tab.getTabId());
    }

    private void doSetActiveTab(final VShellTab tab) {
        for (final VShellTab shellTab : tabs) {
            shellTab.getElement().getStyle().setDisplay(Display.NONE);
        }
        tab.getElement().getStyle().setDisplay(Display.BLOCK);
        activeTab = tab;
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {

    }

    public EventBus getEventBus() {
        return eventBus;
    }


    private ClientSideProxy proxy = new ClientSideProxy(this) {{
        register("setActiveTab", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final VShellTab tab = getTabById(String.valueOf(params[0]));
                if (tab != null) {
                    eventBus.fireEvent(new ActiveTabChangedEvent(tab));
                }
            }
        });

        register("closeTab", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {

            }
        });

        register("setTabClosable", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                setTabClosable(String.valueOf(params[0]), (Boolean)params[1]);
            }
        });

        register("updateTabNotification", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final VShellTab tab = getTabById(String.valueOf(params[0]));
                if (tab != null) {
                    view.getTabContainer().updateTabNotification(tab, String.valueOf(params[1]));
                }
            }
        });

        register("hideTabNotification", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final VShellTab tab = getTabById(String.valueOf(params[0]));
                if (tab != null) {
                    view.getTabContainer().hideTabNotification(tab);
                }
            }
        });
    }};

}
