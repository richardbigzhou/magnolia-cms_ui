/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.tabsheet.widget;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.FadeAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppPreloader;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabSetChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabSetChangedEvent.Handler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.util.CollectionUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Contains the tabs at the top and the tabs themselves. The tabs are all
 * contained in a ScrollPanel, this enables a single showing tab to be scrolled
 * - or the contents of all the tabs to be scrolled together when they are
 * stacked in the 'ShowAllTabs' mode.
 */
public class MagnoliaTabSheetViewImpl extends FlowPanel implements MagnoliaTabSheetView {

    private final List<MagnoliaTabWidget> tabs = new LinkedList<MagnoliaTabWidget>();

    private final ScrollPanel scroller = new ScrollPanel();

    private final FlowPanel tabPanel = new FlowPanel();

    private final TabBarWidget tabBar;

    private MagnoliaTabWidget activeTab = null;

    private AppPreloader preloader = new AppPreloader();

    private FadeAnimation preloaderFadeOut = new FadeAnimation(0d, true);

    private Element logo;

    public MagnoliaTabSheetViewImpl(EventBus eventBus) {
        super();
        this.tabBar = new TabBarWidget(eventBus);
        this.logo = DOM.createDiv();
        addStyleName("v-shell-tabsheet");
        scroller.addStyleName("v-shell-tabsheet-scroller");
        tabPanel.addStyleName("v-shell-tabsheet-tab-wrapper");
        getElement().appendChild(logo);
        add(tabBar);
        add(scroller);
        scroller.setWidget(tabPanel);
        scroller.getElement().getStyle().setPosition(Position.ABSOLUTE);
        this.preloaderFadeOut.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                tabPanel.remove(preloader);
            }
        });
    }


    @Override
    public TabBarWidget getTabContainer() {
        return tabBar;
    }

    @Override
    public void removeTab(MagnoliaTabWidget tabToOrphan) {
        if (activeTab == tabToOrphan) {
            final MagnoliaTabWidget nextTab = CollectionUtil.getNext(getTabs(), tabToOrphan);
            if (nextTab != null) {
                setActiveTab(nextTab);
            }
        }
        getTabs().remove(tabToOrphan);
        tabPanel.remove(tabToOrphan);
    }

    @Override
    public void setActiveTab(final MagnoliaTabWidget tab) {
        this.activeTab = tab;
        // Hide all tabs
        showAllTabContents(false);
        tab.getElement().getStyle().setDisplay(Display.BLOCK);
    }


    @Override
    public MagnoliaTabWidget getActiveTab() {
        return activeTab;
    }

    @Override
    public List<MagnoliaTabWidget> getTabs() {
        return tabs;
    }

    @Override
    public void showAllTabContents(boolean visible) {
        Display display = visible ? Display.BLOCK : Display.NONE;
        for (MagnoliaTabWidget tab : getTabs()) {
            tab.getElement().getStyle().setDisplay(display);
            // MGNLUI-542. Style height breaks show all tab.
            if (visible) {
                tab.getElement().getStyle().clearHeight();
            } else {
                tab.getElement().getStyle().setHeight(100, Unit.PCT);
            }
        }
        if (visible) {
            fireEvent(new ActiveTabChangedEvent(true, false));
        }
    }

    @Override
    public void updateTab(MagnoliaTabWidget tab) {
        if (!tabs.contains(tab)) {
            showPreloader();
            getTabs().add(tab);
            tabPanel.add(tab);
            tabBar.addTabLabel(tab.getLabel());
            fireEvent(new TabSetChangedEvent(this));
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        fireEvent(new TabSetChangedEvent(this));
    }

    @Override
    public HandlerRegistration addTabSetChangedHandler(Handler handler) {
        return addHandler(handler, TabSetChangedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addActiveTabChangedHandler(ActiveTabChangedEvent.Handler handler) {
        return addHandler(handler, ActiveTabChangedEvent.TYPE);
    }

    @Override
    public void setLogo(String logo, String logoBgColor) {
        this.logo.addClassName("v-shell-tabsheet-logo");
        this.logo.addClassName(logo);
        this.logo.getStyle().setBackgroundColor(logoBgColor);
    }

    @Override
    public void setMaxHeight(int height) {
        height -= tabBar.getOffsetHeight();
        scroller.getElement().setAttribute("style", "overflow:auto; position:absolute; zoom:1; max-height:" + height + "px;");
    }

    @Override
    public void showPreloader() {
        preloaderFadeOut.cancel();
        if (tabPanel != preloader.getParent()) {
            preloader.getElement().getStyle().setTop(0, Unit.PX);
            preloader.getElement().getStyle().setZIndex(10000);
            tabPanel.add(preloader);
        }
    }

    @Override
    public void removePreloader() {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (tabPanel == preloader.getParent() ) {
                            preloaderFadeOut.run(500, preloader.getElement());
                        }
                    }
                });
    }

    @Override
    public void clearTabs() {
        final Iterator<Widget> it = tabPanel.iterator();
        while (it.hasNext()) {
            final Widget w = it.next();
            if (w != preloader) {
                w.getElement().getStyle().setDisplay(Display.NONE);
            }
        }
    }
}
