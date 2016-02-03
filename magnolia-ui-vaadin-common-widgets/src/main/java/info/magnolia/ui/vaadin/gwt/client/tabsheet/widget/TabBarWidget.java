/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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

    private final List<MagnoliaTabLabel> tabLabels = new LinkedList<>();

    private final Element tabContainer = DOM.createElement("ul");

    private final EventBus eventBus;

    private VShellShowAllTabLabel showAllTab;

    private final HiddenTabsPopup hiddenTabsPopup;

    private MagnoliaTabLabel activeTab;

    public TabBarWidget(EventBus eventBus) {
        this.eventBus = eventBus;
        setElement(tabContainer);
        setStyleName("nav");
        addStyleDependentName("tabs");

        hiddenTabsPopup = new HiddenTabsPopup(eventBus);
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

        // Reset hidden-tabs popup
        hiddenTabsPopup.setVisible(true);
        int toggleWidth = hiddenTabsPopup.getOffsetWidth();
        int availableWidth = tabContainer.getOffsetWidth();

        // 1. Active tab is always visible, count its width first
        activeTab.setVisible(true);
        availableWidth -= activeTab.getOffsetWidth();

        // 2. Account for show-all tab if needed
        if (showAllTab != null) {
            availableWidth -= showAllTab.getOffsetWidth();
        }

        // Collect every tab's width (we'll have to know anyway)
        Map<MagnoliaTabLabel, Integer> tabWidths = new HashMap<>();
        for (MagnoliaTabLabel tab : tabLabels) {
            if (tab == activeTab) {
                continue;
            }
            tab.setVisible(true);
            tabWidths.put(tab, tab.getOffsetWidth());
        }

        // 3. Squeeze all other tabs from start to end, as many as width allows
        Iterator<MagnoliaTabLabel> it = tabLabels.iterator();
        boolean outOfSpace = false;
        while (it.hasNext()) {
            MagnoliaTabLabel tab = it.next();
            // Active tab is already accounted for, don't hide it
            if (tab == activeTab) {
                continue;
            }
            if (!outOfSpace) {
                int width = tabWidths.get(tab);
                int maxWidth = Collections.max(tabWidths.values());
                // Either I have enough space for the next tab and it's the last one
                if ((!it.hasNext() && availableWidth >= width)
                        // Either I need enough space for the widest of the next tabs, plus the toggle
                        // Why widest? The tab bar may have to accommodate it, not necessarily now, but maybe when switching.
                        || (maxWidth + toggleWidth <= availableWidth)) {
                    tabWidths.remove(tab);
                    availableWidth -= width;
                    continue;
                } else {
                    outOfSpace = true;
                }
            }
            // If we're there we've run out of space
            tab.setVisible(false);
        }

        // 4. Compute content of the hidden tabs popup
        hiddenTabsPopup.hide();
        hiddenTabsPopup.menubar.clearItems();
        it = tabLabels.iterator();
        while (it.hasNext()) {
            MagnoliaTabLabel tab = it.next();
            if (!tab.isVisible()) {
                hiddenTabsPopup.addTabLabel(tab);
            }
        }
        hiddenTabsPopup.showControlIfNeeded();
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

    /**
     * The toggle display the hidden-tabs popup upon click.
     */
    static class HiddenTabsPopup extends Widget {

        private final DialogBox menuWrapper = new DialogBox(true);
        private final HiddenTabsMenuBar menubar = new HiddenTabsMenuBar();
        private final EventBus eventBus;

        public HiddenTabsPopup(EventBus eventBus) {
            this.eventBus = eventBus;

            setElement(DOM.createElement("li"));
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
                    menuWrapper.setPopupPosition(getAbsoluteLeft() + getOffsetWidth(), getAbsoluteTop());
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
