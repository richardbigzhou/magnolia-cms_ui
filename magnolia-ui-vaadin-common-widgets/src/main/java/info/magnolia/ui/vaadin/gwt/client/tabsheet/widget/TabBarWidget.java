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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ui.VButton;

/**
 * A bar that contains the tab labels and controls the switching between tabs.
 */
public class TabBarWidget extends ComplexPanel {

    private static final String SINGLE_TAB_CLASS_NAME = "single-tab";

    private final List<MagnoliaTabLabel> tabLabels = new LinkedList<MagnoliaTabLabel>();

    private final Element tabContainer = DOM.createElement("ul");

    private EventBus eventBus;

    private VShellShowAllTabLabel showAllTab;

    public TabBarWidget(EventBus eventBus) {
        this.eventBus = eventBus;
        setElement(tabContainer);
        setStyleName("nav");
        addStyleDependentName("tabs");

        bindHandlers();
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
                    }
                }
                tabLabels.remove(tabLabel);
                remove(tabLabel);
                updateSingleTabStyle();
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
            add(label, getElement());
            updateSingleTabStyle();
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

            textWrapper.addClickHandler(new ClickHandler(){
                @Override
                public void onClick(ClickEvent event) {
                    textWrapper.setFocus(false);
                    onClickGeneric(event.getNativeEvent());
                }
            });
        }

        private void onClickGeneric(NativeEvent nativeEvent){
            eventBus.fireEvent(new ShowAllTabsEvent());
            nativeEvent.stopPropagation();
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
