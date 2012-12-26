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
package info.magnolia.ui.vaadin.gwt.client.tabsheet.connector;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEventHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.rpc.MagnoliaTabSheetClientRpc;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.rpc.MagnoliaTabSheetServerRpc;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.connector.MagnoliaTabConnector;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.widget.MagnoliaTabSheetView;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.widget.MagnoliaTabSheetViewImpl;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import java.util.List;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;


/**
 * MagnoliaTabSheetConnector.
 */
@Connect(MagnoliaTabSheet.class)
public class MagnoliaTabSheetConnector extends AbstractComponentContainerConnector implements
        MagnoliaTabSheetView.Presenter {

    private final MagnoliaTabSheetServerRpc rpc = RpcProxy.create(MagnoliaTabSheetServerRpc.class, this);
    
    private MagnoliaTabSheetView view;

    private final EventBus eventBus = new SimpleEventBus();

    @Override
    public MagnoliaTabSheetState getState() {
        return (MagnoliaTabSheetState)super.getState();
    }

    @Override
    protected Widget createWidget() {
        this.view = new MagnoliaTabSheetViewImpl(eventBus, this);
        return view.asWidget();
    }

    @Override
    protected void init() {
        super.init();
        
        addStateChangeHandler("activeTab", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                final MagnoliaTabConnector tabConnector = (MagnoliaTabConnector)getState().activeTab;
                if (tabConnector != null) {
                    view.setActiveTab(tabConnector.getWidget());
                    eventBus.fireEvent(new ActiveTabChangedEvent(tabConnector.getWidget(), false));   
                }
            }
        });
        
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                view.getTabContainer().addShowAllTab(getState().showAllEnabled, getState().showAllLabel);
            }
        });
        
        registerRpc(MagnoliaTabSheetClientRpc.class, new MagnoliaTabSheetClientRpc() {
            @Override
            public void toggleFullScreenMode(boolean isFullScreen) {
                view.setShowActiveTabFullscreen(isFullScreen);
                getLayoutManager().setNeedsMeasure(MagnoliaTabSheetConnector.this);
            }
        });
        
        eventBus.addHandler(TabCloseEvent.TYPE, new TabCloseEventHandler() {
            @Override
            public void onTabClosed(TabCloseEvent event) {
                rpc.closeTab(Util.findConnectorFor(event.getTab()));
            }
        });

        eventBus.addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedEvent.Handler() {
            @Override
            public void onActiveTabChanged(ActiveTabChangedEvent event) {
                if (event.isNotifyServer()) {
                      rpc.setActiveTab(Util.findConnectorFor(event.getTab()));
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
    }
    
    @Override
    public void updateCaption(ComponentConnector connector) {
        final String caption = connector.getState().caption;
        view.getTabContainer().updateTab((MagnoliaTabWidget)connector.getWidget(), caption);
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        final List<ComponentConnector> childConnectors = getChildComponents();
        final List<ComponentConnector> oldChildren = event.getOldChildren();

        oldChildren.removeAll(childConnectors);
        for (final ComponentConnector cc : oldChildren) {
            view.removeTab((MagnoliaTabWidget)cc.getWidget());
        }
        
        for (final ComponentConnector cc : childConnectors) {
            view.updateTab((MagnoliaTabWidget)cc.getWidget());
        }
    }

    @Override
    public void updateLayoutOfActiveTab() {
        if (getState().activeTab != null) {
            ((ComponentConnector)getState().activeTab).getLayoutManager().layoutNow();
        }
    }

    @Override
    public void updateLayout() {
        getLayoutManager().setNeedsMeasure(this);
    }
   
    @Override
    public void onUnregister() {
        new Timer() {
            @Override
            public void run() {
                MagnoliaTabSheetConnector.super.onUnregister();
            }
        }.schedule(1000);
    }
}
