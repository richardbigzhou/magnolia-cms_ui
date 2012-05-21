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
package info.magnolia.m5vaadin.tabsheet.client;

import info.magnolia.m5vaadin.tabsheet.client.util.CollectionUtil;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * A bar that contains the tab labels and controls the switching between tabs.
 * @author apchelintcev
 *
 */
public class VShellTabNavigator extends ComplexPanel {

    private Element tabList = DOM.createElement("ul");
    
    private List<VShellTabLabel> tabLabels = new LinkedList<VShellTabLabel>();
    
    private Map<VShellTab, VShellTabLabel> labelMap = new LinkedHashMap<VShellTab, VShellTabLabel>();
    
    public VShellTabNavigator() {
        setElement(tabList);
        setStyleName("nav");
        addStyleDependentName("tabs");
        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
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
                final Element target = (Element)event.getNativeEvent().getEventTarget().cast(); 
                final VShellTabLabel label = Util.findWidget(target, VShellTabLabel.class);
                
                if (label != null) {
                    getParent().getEventBus().fireEvent(label.isClosing(target) ? 
                            new TabCloseEvent(label.getTab()):
                                new ActiveTabChangedEvent(label.getTab()));
                }
            }
        }, ClickEvent.getType());
        
        getParent().getEventBus().addHandler(ActiveTabChangedEvent.TYPE, new ActiveTabChangedHandler() {
            @Override
            public void onActiveTabChanged(final ActiveTabChangedEvent event) {
                final VShellTab tab = event.getTab();
                final VShellTabLabel label = labelMap.get(tab);
                if (label != null) {
                    for (final VShellTabLabel tabLabel : tabLabels) {
                        tabLabel.removeStyleName("active");
                    }
                    label.addStyleName("active");                    
                }
            }
        });
        
        getParent().getEventBus().addHandler(TabCloseEvent.TYPE, new TabCloseEventHandler() {
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
            }
        });
    }

    protected VShellTabLabel getNextLabel(final VShellTabLabel label) {
        return CollectionUtil.getNext(tabLabels, label);
    }

    @Override
    public VShellTabSheet getParent() {
        return (VShellTabSheet)super.getParent();
    }
    
    public void updateTab(final VShellTab component, final UIDL uidl) {
        VShellTabLabel label = labelMap.get(component);
        if (label == null) {
            label = new VShellTabLabel();
            labelMap.put(component, label);
            tabLabels.add(label);
            
            label.setTab(component);
            label.updateCaption(uidl);
            label.setClosable(component.isClosable());
            
            add(label, getElement());
        }
        label.updateCaption(uidl);
    }
    
    private static class VShellTabLabel extends SimplePanel {
        
        private final Element notificationBox = DOM.createDiv();
        
        private final Element closeElement = DOM.createDiv();
        
        private final Element text = DOM.createSpan();
        
        private VShellTab tab;
        
        public VShellTabLabel() {
            super(DOM.createElement("li"));
            closeElement.setClassName("v-shell-tab-close");
            notificationBox.setClassName("v-shell-tab-notification");
            getElement().appendChild(text);
        }

        public boolean isClosing(Element target) {
            return closeElement.isOrHasChild(target);
        }

        public void setTab(final VShellTab tab) {
            this.tab = tab;
        }

        public VShellTab getTab() {
            return tab;
        }
        
        public void updateCaption(final UIDL uidl) {
            if (uidl.hasAttribute("caption")) {
                final String caption = uidl.getStringAttribute("caption");
                text.setInnerHTML(caption);
            }
        }

        public void setClosable(boolean isClosable) {
            if (!isClosable) {
                if (getElement().isOrHasChild(closeElement)) {
                    getElement().removeChild(closeElement);   
                }
            } else {
                getElement().insertBefore(closeElement, text);
            }
        }

        public void updateNotification(final String text) {
            if (!getElement().isOrHasChild(notificationBox)) {
                getElement().appendChild(notificationBox);
                notificationBox.appendChild(DOM.createSpan());
            }
            ((Element)notificationBox.getChild(0)).setInnerText(text);
        }

        public void hideNotification() {
            if (getElement().isOrHasChild(notificationBox)) {
                getElement().removeChild(notificationBox);
            }
        }
    }

    public void setTabClosable(final VShellTab tab, boolean isClosable) {
        final VShellTabLabel label = labelMap.get(tab);
        if (label != null) {
            label.setClosable(isClosable);
        }
        
    }

    public void updateTabNotification(final VShellTab tab, final String text) {
        final VShellTabLabel label = labelMap.get(tab);
        if (label != null) {
            label.updateNotification(text);
        }
    }

    public void hideTabNotification(final VShellTab tab) {
        final VShellTabLabel label = labelMap.get(tab);
        if (label != null) {
            label.hideNotification();
        }
    }
}
