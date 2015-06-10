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

import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.CaptionChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.CaptionChangedHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ResizeEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ResizeHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabClickedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEventHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabLabel;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.util.CollectionUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ui.VButton;

/**
 * A bar that contains the tab labels and controls the switching between tabs.
 */
public class TabBarWidget extends ComplexPanel {

    private static final String SINGLE_TAB_CLASS_NAME = "single-tab";

    private final List<MagnoliaTabLabel> tabLabels = new LinkedList<MagnoliaTabLabel>();

    private final List<MagnoliaTabLabel> visibleTabs = new LinkedList<MagnoliaTabLabel>();

    private final Map<MagnoliaTabLabel, Integer> tabSizes = new HashMap<MagnoliaTabLabel, Integer>();

    private final Element tabContainer = DOM.createElement("ul");

    private EventBus eventBus;

    private VShellShowAllTabLabel showAllTab;

    private VShellMoreTabLabel moreTabs;

    private MagnoliaTabLabel activeTab;

    public TabBarWidget(EventBus eventBus) {
        this.eventBus = eventBus;
        setElement(tabContainer);
        setStyleName("nav");
        addStyleDependentName("tabs");

        moreTabs = new VShellMoreTabLabel();
        moreTabs.setVisible(false);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        bindHandlers();
    }

    private void calculateHiddenTabs() {
        if (tabLabels.isEmpty()) {
            return;
        }

        if (activeTab == null) {
            activeTab = tabLabels.get(0);
        }

        if (visibleTabs.contains(activeTab)) {
            return;
        }

        int tabBarWidth = this.getElement().getOffsetWidth();
        int tabsWidth = ((showAllTab != null) ? showAllTab.getElement().getOffsetWidth() : 0) + moreTabs.getElement().getOffsetWidth();

        List<MagnoliaTabLabel> tabs = CollectionUtil.reserveItemToFirst(tabLabels, activeTab);

        for (MagnoliaTabLabel tab : tabs) {
            tabsWidth += getTabWidth(tab);
            toggleTabsVisibility(tab, tabsWidth <= tabBarWidth);
        }
    }

    private int getTabWidth(MagnoliaTabLabel tab) {
        int tabWidth = tab.getElement().getOffsetWidth();
        if (tabWidth != 0) {
            tabSizes.put(tab, tabWidth);
        } else {
            tabWidth = (tabSizes.containsKey(tab)) ? tabSizes.get(tab).intValue() : 0;
        }

        return tabWidth;
    }

    private void toggleTabsVisibility(MagnoliaTabLabel tab, boolean visible) {
        tab.setVisible(visible);
        moreTabs.setVisible(!visible);
        moreTabs.menuItems.get(tab).getElement().getParentElement().getStyle().setDisplay(!visible ? Display.BLOCK : Display.NONE);
        if (visible) {
            visibleTabs.add(tab);
        } else {
            visibleTabs.remove(tab);
        }
    }

    private void bindHandlers() {
        eventBus.addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedEvent.Handler() {
            @Override
            public void onActiveTabChanged(final ActiveTabChangedEvent event) {
                final MagnoliaTabWidget tab = event.getTab();
                final MagnoliaTabLabel label = tab.getLabel();
                if (label != null) {
                    for (final MagnoliaTabLabel tabLabel : tabLabels) {
                        tabLabel.removeStyleName("active");
                    }
                    label.addStyleName("active");
                    showAll(false);
                    activeTab = label;
                    moreTabs.menuWrapper.hide();
                }
            }
        });

        eventBus.addHandler(TabCloseEvent.TYPE, new TabCloseEventHandler() {
            @Override
            public void onTabClosed(TabCloseEvent event) {
                final MagnoliaTabLabel tabLabel = event.getTab().getLabel();
                boolean wasActive = tabLabel.getStyleName().contains("active");
                if (wasActive) {
                    final MagnoliaTabLabel nextLabel = getNextLabel(tabLabel);
                    if (nextLabel != null) {
                        nextLabel.addStyleName("active");
                        activeTab = nextLabel;
                    }
                }
                tabLabels.remove(tabLabel);
                visibleTabs.clear();
                remove(tabLabel);
                updateSingleTabStyle();
                calculateHiddenTabs();
            }
        });

        eventBus.addHandler(ShowAllTabsEvent.TYPE, new ShowAllTabsHandler() {

            @Override
            public void onShowAllTabs(ShowAllTabsEvent event) {
                for (final MagnoliaTabLabel tabLabel : tabLabels) {
                    tabLabel.removeStyleName("active");
                }
                showAll(true);
            }
        });

        eventBus.addHandler(CaptionChangedEvent.TYPE, new CaptionChangedHandler() {

            @Override
            public void onCaptionChanged(CaptionChangedEvent event) {
                moreTabs.menuItems.get(event.getLabel()).setText(event.getLabel().getElement().getInnerText());
                visibleTabs.clear();
                calculateHiddenTabs();
            }
        });

        eventBus.addHandler(ResizeEvent.TYPE, new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                activeTab = event.getLabel();
                visibleTabs.clear();
                calculateHiddenTabs();
            }
        });

        eventBus.addHandler(TabClickedEvent.TYPE, new TabClickedEvent.Handler() {
            @Override
            public void onTabClicked(TabClickedEvent event) {
                if (moreTabs.isVisible()) {
                    DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), moreTabs);
                }
            }
        });
    }

    protected MagnoliaTabLabel getNextLabel(final MagnoliaTabLabel label) {
        return CollectionUtil.getNext(tabLabels, label);
    }

    public void addTabLabel(MagnoliaTabLabel label) {
        label.setEventBus(eventBus);
        if (!tabLabels.contains(label)) {
            tabLabels.add(label);
            moreTabs.addTabLabel(label);
            add(label, getElement());
            updateSingleTabStyle();
            calculateHiddenTabs();
        }
    }

    public void updateSingleTabStyle() {
        if (tabLabels.size() <= 1) {
            tabContainer.addClassName(SINGLE_TAB_CLASS_NAME);
        } else {
            tabContainer.removeClassName(SINGLE_TAB_CLASS_NAME);
        }
    }

    public void addShowAllTab(boolean showAll, String label) {
        if (showAll && showAllTab == null) {
            showAllTab = new VShellShowAllTabLabel(label);
            add(showAllTab, getElement());
        } else if (!showAll && showAllTab != null) {
            remove(showAllTab);
            showAllTab = null;
        }

        add(moreTabs, getElement());
        calculateHiddenTabs();
    }

    private class VShellShowAllTabLabel extends SimplePanel {

        private final VButton textWrapper = new VButton();

        public VShellShowAllTabLabel(String label) {
            super(DOM.createElement("li"));
            addStyleName("show-all");
            textWrapper.getElement().setInnerHTML(label);
            textWrapper.getElement().setClassName("tab-title");
            this.add(textWrapper);
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            bindHandlers();
        }

        private void bindHandlers() {
            addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onClickGeneric(event.getNativeEvent());
                }
            }, ClickEvent.getType());

            textWrapper.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    textWrapper.setFocus(false);
                    onClickGeneric(event.getNativeEvent());
                }
            });
        }

        private void onClickGeneric(NativeEvent nativeEvent) {
            eventBus.fireEvent(new ShowAllTabsEvent());
            nativeEvent.stopPropagation();
        }

    }

    private class VShellMoreTabLabel extends SimplePanel {

        DialogBox menuWrapper = new DialogBox(true);
        MenuBar menubar = new MenuBar(true);

        Map<MagnoliaTabLabel, MenuItem> menuItems = new HashMap<MagnoliaTabLabel, MenuItem>();

        public VShellMoreTabLabel() {
            super(DOM.createElement("li"));
            addStyleName("icon-arrow2_e");
            addStyleName("v-shell-more-tabs");
            getElement().getStyle().setFontSize(18, Unit.PX);

            menubar.setStyleName("context-menu");
            menubar.addStyleName("more-tabs-menu");
            menuWrapper.add(menubar);
            menuWrapper.setStyleName("context-menu-wrapper");
            menuWrapper.getElement().getStyle().setZIndex(10000);
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            bindHandlers();
        }

        public void addTabLabel(final MagnoliaTabLabel label) {
            MenuItem item = menubar.addItem(label.getTitle(), new ScheduledCommand() {
                @Override
                public void execute() {
                    menuWrapper.hide();
                    eventBus.fireEvent(new ActiveTabChangedEvent(label.getTab()));
                }
            });
            item.addStyleName("menu-item");
            menuItems.put(label, item);
        }

        private void bindHandlers() {
            addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.preventDefault();
                    event.stopPropagation();
                    menuWrapper.setPopupPosition(moreTabs.getAbsoluteLeft() + moreTabs.getOffsetWidth(), moreTabs.getAbsoluteTop());
                    menuWrapper.show();
                }

            }, ClickEvent.getType());
        }
    }

    public void showAll(boolean showAll) {
        if (showAllTab != null) {
            if (showAll) {
                showAllTab.addStyleName("active");
            } else {
                if (showAllTab.getStyleName().contains("active")) {
                    showAllTab.removeStyleName("active");
                }
            }
        }
    }

}
