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
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ShowAllTabsHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEventHandler;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabLabel;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.util.CollectionUtil;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ui.VButton;

/**
 * A bar that contains the tab labels and controls the switching between tabs.
 */
public class TabBarWidget extends ComplexPanel {

    private static final String SINGLE_TAB_CLASS_NAME = "single-tab";

    private final List<MagnoliaTabLabel> tabLabels = new LinkedList<MagnoliaTabLabel>();

    private final Element tabContainer = DOM.createElement("ul");

    private final EventBus eventBus;

    private VShellShowAllTabLabel showAllTab;

    private HiddenTabsPopup hiddenTabsPopup;

    private MagnoliaTabLabel activeTab;

    public TabBarWidget(EventBus eventBus) {
        this.eventBus = eventBus;
        setElement(tabContainer);
        setStyleName("nav");
        addStyleDependentName("tabs");

        hiddenTabsPopup = new HiddenTabsPopup();
        add(hiddenTabsPopup, tabContainer);
        bindHandlers();
    }

    void reArrangeTabVisibility() {
        if (tabLabels.isEmpty()) {
            return;
        }

        // Is this ever possible?
        if (activeTab == null) {
            activeTab = tabLabels.get(0);
        }

        int tabBarWidth = this.getElement().getOffsetWidth();
        int showAllTabWidth = (showAllTab != null) ? showAllTab.getElement().getOffsetWidth() : 0;
        int hiddenTabsMenuButtonWidth = hiddenTabsPopup.getElement().getOffsetWidth();
        int tabsWidth = showAllTabWidth + hiddenTabsMenuButtonWidth;

        hiddenTabsPopup.menubar.clearItems();
        hiddenTabsPopup.hide();

        int count = 0;
        int index = tabLabels.indexOf(activeTab);
        // Cyclicly iterate back-wards over the labels estimating the possibility to squeeze each of them into the visible area of the
        // tab-bar, we start with the active tab so that it is always visible
        int tabCount = tabLabels.size();
        while (count < tabCount) {
            final MagnoliaTabLabel tab = tabLabels.get((index + tabCount - 1) % tabCount);
            tabsWidth += getTabWidth(tab);
            toggleTabVisibility(tab, tabsWidth <= tabBarWidth);
            ++index;
            ++count;
        }

        hiddenTabsPopup.showControlIfNeeded();
    }

    private int getTabWidth(MagnoliaTabLabel tab) {
        boolean isVisible = tab.isVisible();
        if (!isVisible) {
            tab.setVisible(true);
        }
        int tabWidth = tab.getElement().getOffsetWidth();
        tab.setVisible(isVisible);
        return tabWidth;
    }

    private void toggleTabVisibility(MagnoliaTabLabel tabLabel, boolean visible) {
        tabLabel.setVisible(visible);
        if (!visible) {
            hiddenTabsPopup.addTabLabel(tabLabel);
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
                    hiddenTabsPopup.menuWrapper.hide();
                    if (!label.isVisible()) {
                        reArrangeTabVisibility();
                    }
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
                remove(tabLabel);
                updateSingleTabStyle();
                reArrangeTabVisibility();
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
    }

    protected MagnoliaTabLabel getNextLabel(final MagnoliaTabLabel label) {
        return CollectionUtil.getNext(tabLabels, label);
    }

    public void addTabLabel(MagnoliaTabLabel label) {
        label.setEventBus(eventBus);
        if (!tabLabels.contains(label)) {
            tabLabels.add(label);
            // Keep hidden-tabs toggle and show-all button last in the DOM when inserting labels.
            insert(label, tabContainer, tabLabels.size() - 1, true);
            updateSingleTabStyle();
            reArrangeTabVisibility();
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
        reArrangeTabVisibility();
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

    private class HiddenTabsPopup extends Widget {

        private DialogBox menuWrapper = new DialogBox(true);

        private HiddenTabsMenuBar menubar = new HiddenTabsMenuBar();

        public HiddenTabsPopup() {
            setElement(LIElement.as(DOM.createElement("li")));
            addStyleName("icon-arrow2_e");
            addStyleName("hidden-tabs-popup-button");
            menuWrapper.add(menubar);
            menuWrapper.setStyleName("context-menu-wrapper");

            // Initially hide the component
            setVisible(false);
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            bindHandlers();
        }

        public void addTabLabel(final MagnoliaTabLabel label) {
            final MenuItem item = menubar.addItem(label.getCaption(), new ScheduledCommand() {
                @Override
                public void execute() {
                    menuWrapper.hide();
                    eventBus.fireEvent(new ActiveTabChangedEvent(label.getTab()));
                }
            });
            item.addStyleName("menu-item");
        }

        private void bindHandlers() {
            addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.preventDefault();
                    event.stopPropagation();
                    menuWrapper.setPopupPosition(hiddenTabsPopup.getAbsoluteLeft() + hiddenTabsPopup.getOffsetWidth(), hiddenTabsPopup.getAbsoluteTop());
                    menuWrapper.show();
                }

            }, ClickEvent.getType());
        }

        public void hide() {
            menuWrapper.hide();
            setVisible(false);
        }

        public void showControlIfNeeded() {
            setVisible(!menubar.isEmpty());
        }

        private class HiddenTabsMenuBar extends MenuBar {

            public HiddenTabsMenuBar() {
                super(true);
                setStyleName("context-menu");
                addStyleName("hidden-tabs-menu");
            }

            public boolean isEmpty() {
                return super.getItems().isEmpty();
            }
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
