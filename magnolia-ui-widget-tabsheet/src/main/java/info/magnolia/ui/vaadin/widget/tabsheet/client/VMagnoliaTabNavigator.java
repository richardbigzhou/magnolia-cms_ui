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
package info.magnolia.ui.vaadin.widget.tabsheet.client;

import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ActiveTabChangedHandler;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ShowAllTabsEvent;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ShowAllTabsHandler;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.TabCloseEvent;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.TabCloseEventHandler;
import info.magnolia.ui.vaadin.widget.tabsheet.client.util.CollectionUtil;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * A bar that contains the tab labels and controls the switching between tabs.
 */
public class VMagnoliaTabNavigator extends ComplexPanel {

    private static final String SINGLE_TAB_CLASSNAME = "single-tab";

    private final Element tabList = DOM.createElement("ul");

    private final List<VShellTabLabel> tabLabels = new LinkedList<VShellTabLabel>();

    private final Map<VMagnoliaTab, VShellTabLabel> labelMap = new LinkedHashMap<VMagnoliaTab, VShellTabLabel>();

    private VShellShowAllTabLabel showAllTab;

    private EventBus eventBus;

    public VMagnoliaTabNavigator(EventBus eventBus) {
        this.eventBus = eventBus;
        setElement(tabList);
        setStyleName("nav");
        addStyleDependentName("tabs");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        bindHandlers();
    }

    private void bindHandlers() {

        eventBus.addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedHandler() {

            @Override
            public void onActiveTabChanged(final ActiveTabChangedEvent event) {
                final VMagnoliaTab tab = event.getTab();
                final VShellTabLabel label = labelMap.get(tab);
                if (label != null) {
                    for (final VShellTabLabel tabLabel : tabLabels) {
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
                final VShellTabLabel tabLabel = labelMap.get(event.getTab());
                boolean wasActive = tabLabel.getStyleName().contains("active");
                if (wasActive) {
                    final VShellTabLabel nextLabel = getNextLabel(tabLabel);
                    if (nextLabel != null) {
                        nextLabel.addStyleName("active");
                    }
                }
                tabLabels.remove(tabLabel);
                labelMap.remove(event.getTab());
                remove(tabLabel);
                updateSingleTabStyle();
            }
        });

        eventBus.addHandler(ShowAllTabsEvent.TYPE, new ShowAllTabsHandler() {

            @Override
            public void onShowAllTabs(ShowAllTabsEvent event) {
                for (final VShellTabLabel tabLabel : tabLabels) {
                    tabLabel.removeStyleName("active");
                }
                showAll(true);
            }
        });
    }

    protected VShellTabLabel getNextLabel(final VShellTabLabel label) {
        return CollectionUtil.getNext(tabLabels, label);
    }

    public void updateTab(final VMagnoliaTab component, final UIDL uidl) {
        VShellTabLabel label = labelMap.get(component);
        if (label == null) {
            label = new VShellTabLabel();
            labelMap.put(component, label);
            tabLabels.add(label);

            label.setTab(component);
            label.updateCaption(uidl);
            label.setClosable(component.isClosable());

            component.setLabel(label);
            add(label, getElement());
            updateSingleTabStyle();
        }
        label.updateCaption(uidl);
    }

    public void updateSingleTabStyle() {
        if (tabLabels.size() <= 1) {
            if (!tabList.getClassName().contains(SINGLE_TAB_CLASSNAME)) {
                tabList.addClassName(SINGLE_TAB_CLASSNAME);
            }
        } else {
            if (tabList.getClassName().contains(SINGLE_TAB_CLASSNAME)) {
                tabList.removeClassName(SINGLE_TAB_CLASSNAME);
            }
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

    /**
     * Tab label in the tabbar.
     */
    public class VShellTabLabel extends SimplePanel {

        private  Element indicatorsWrapper = DOM.createDiv();
                
        private final Element notificationBox = DOM.createDiv();

        private final Element closeElement = DOM.createDiv();
        
        private final Element errorIndicator = DOM.createDiv();

        private final Element textWrapper = DOM.createSpan();
        
        private VMagnoliaTab tab;

        public VShellTabLabel() {
            super(DOM.createElement("li"));
            
            getElement().addClassName("clearfix");
            
            indicatorsWrapper.addClassName("indicators-wrapper");
            getElement().appendChild(textWrapper);
            
            indicatorsWrapper = getElement();
            
            closeElement.setClassName("v-shell-tab-close");
            notificationBox.setClassName("v-shell-tab-notification");
            errorIndicator.setClassName("v-shell-tab-error");
            
            getElement().appendChild(closeElement);
            getElement().appendChild(notificationBox);
            getElement().appendChild(errorIndicator);
            
            DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
            hideNotification();
            setHasError(false);
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
                    final Element target = (Element) event.getNativeEvent().getEventTarget().cast();
                    if (closeElement.isOrHasChild(target) ) {
                        eventBus.fireEvent(new TabCloseEvent(tab));
                    } else {
                        eventBus.fireEvent(new ActiveTabChangedEvent(tab));    
                    }
                }
            }, ClickEvent.getType());
        }

        public void setTab(final VMagnoliaTab tab) {
            this.tab = tab;
        }

        public VMagnoliaTab getTab() {
            return tab;
        }

        public void updateCaption(final UIDL uidl) {
            if (uidl.hasAttribute("caption")) {
                final String caption = uidl.getStringAttribute("caption");
                textWrapper.setInnerText(caption);
            }
        }

        public void setClosable(boolean isClosable) {
            closeElement.getStyle().setDisplay(isClosable ? Display.INLINE_BLOCK : Display.NONE);
        }

        public void updateNotification(final String text) {
            notificationBox.getStyle().setDisplay(Display.INLINE_BLOCK);
            ((Element) notificationBox.getChild(0)).setInnerText(text);
        }

        public void hideNotification() {
            notificationBox.getStyle().setDisplay(Display.NONE);
        }

        public void setHasError(boolean hasError) {
            errorIndicator.getStyle().setDisplay(hasError ? Display.INLINE_BLOCK : Display.NONE);
        }
    }

    private class VShellShowAllTabLabel extends SimplePanel {

        private final Element text = DOM.createSpan();

        public VShellShowAllTabLabel(String label) {
            super(DOM.createElement("li"));
            addStyleName("show-all");
            text.setInnerHTML(label);
            getElement().appendChild(text);

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
                    eventBus.fireEvent(new ShowAllTabsEvent());
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
