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
package info.magnolia.ui.vaadin.tabsheet;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.VMagnoliaTabSheet;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.util.CollectionUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.KeyMapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;

/**
 * Simple lightweight tabsheet component.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VMagnoliaTabSheet.class, loadStyle = LoadStyle.EAGER)
public class MagnoliaTabSheet extends AbstractComponentContainer implements ServerSideHandler {

    private final KeyMapper mapper = new KeyMapper();

    private final List<MagnoliaTab> tabs = new LinkedList<MagnoliaTab>();

    private MagnoliaTab activeTab = null;

    protected ServerSideProxy proxy = createProxy();

    private boolean isFullscreen = false;
    
    public MagnoliaTabSheet() {
        super();
        setImmediate(true);
    }

    protected ServerSideProxy createProxy() {
        return new ServerSideProxy(this) {
            {
                register("activateTab", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        onActiveTabSet(String.valueOf(params[0]));
                    }
                });

                register("closeTab", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String tabId = String.valueOf(params[0]);
                        closeTab(tabId);
                    }
                });
            }
        };
    }

    @Override
    public void addComponent(final Component c) {
        if (!(c instanceof ComponentContainer)) {
            throw new IllegalArgumentException("Content of the tab must be a ComponentContainer!");
        }
        if (c instanceof MagnoliaTab) {
            doAddTab((MagnoliaTab) c);
        } else {
            addTab("", (ComponentContainer) c);
        }
    }

    public ComponentContainer addTab(String string) {
        final VerticalLayout c = new VerticalLayout();
        addTab(string, c);
        return c;
    }

    public MagnoliaTab addTab(final String caption, final ComponentContainer c) {
        final MagnoliaTab tab = new MagnoliaTab(caption, c);
        doAddTab(tab);
        return tab;
    }

    @Override
    public void callFromClient(String method, Object[] params) {}

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    public void showAllTab(boolean showAll, String label) {
        proxy.call("addShowAllTab", new Boolean(showAll), label);
    }

    public void setFullscreen(boolean isFullscreen) {
        if (this.isFullscreen != isFullscreen) {
            this.isFullscreen = isFullscreen;
            proxy.call("setActiveTabFullscreen", isFullscreen);   
        }
    }
    
    protected void closeTab(final String tabId) {
        final MagnoliaTab tab = (MagnoliaTab) mapper.get(tabId);
        if (tab != null) {
            if (activeTab == tab) {
                final MagnoliaTab nextTab = getNextTab(tab);
                if (nextTab != null && nextTab != tab) {
                    setActiveTab(nextTab);
                }
            }
            removeComponent(tab);
        }
    }

    protected void doAddTab(final MagnoliaTab tab) {
        super.addComponent(tab);
        tab.setTabId(mapper.key(tab));
        tabs.add(tab);
        if (activeTab == null) {
            setActiveTab(tab);
        }
        requestRepaint();
    }

    public MagnoliaTab getActiveTab() {
        return activeTab;
    }

    public void onActiveTabSet(String tabId) {
        final MagnoliaTab shellTab = (MagnoliaTab) mapper.get(tabId);
        if (shellTab != null && shellTab != activeTab) {
            activeTab = shellTab;
        }
    }

    @Override
    public Iterator<Component> getComponentIterator() {
        return new Iterator<Component>() {

            private final Iterator<MagnoliaTab> wrappedIt = tabs.iterator();

            @Override
            public boolean hasNext() {
                return wrappedIt.hasNext();
            }

            @Override
            public Component next() {
                final MagnoliaTab tab = wrappedIt.next();
                return tab.getContent();
            }

            @Override
            public void remove() {
                wrappedIt.remove();
            }
        };
    }

    protected MagnoliaTab getNextTab(final MagnoliaTab tab) {
        return CollectionUtil.getNext(tabs, tab);
    }

    protected MagnoliaTab getTabById(final String tabId) {
        return (MagnoliaTab)mapper.get(tabId);
    }

    @Override
    public Object[] initRequestFromClient() {
        proxy.call("setActiveTab", activeTab.getTabId());
        proxy.call("setActiveTabFullscreen", isFullscreen);
        for (final MagnoliaTab tab : tabs) {
            if (tab.isClosable()) {
                proxy.call("setTabClosable", tab.getTabId(), true);
            }
        }
        return new Object[] {};
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        paintTabs(target);
        proxy.paintContent(target);
    }

    private void paintTabs(final PaintTarget target) throws PaintException {
        target.startTag("tabs");
        final Iterator<MagnoliaTab> it = tabs.iterator();
        while (it.hasNext()) {
            it.next().paint(target);
        }
        target.endTag("tabs");
    }
    
    @Override
    public void removeAllComponents() {
        /**
         * TODO: implement properly.
         */
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void removeComponent(final Component c) {
        if (c instanceof MagnoliaTab) {
            final MagnoliaTab tab = (MagnoliaTab) c;
            super.removeComponent(c);
            tabs.remove(tab);
            mapper.remove(tab);
        }
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {

    }

    public void setActiveTab(final MagnoliaTab tab) {
        if (tabs.contains(tab)) {
            this.activeTab = tab;
            proxy.callOnce("setActiveTab", mapper.key(tab));
            requestRepaint();
        }
    };

    public void setTabClosable(final MagnoliaTab tab, boolean closable) {
        tab.setClosable(closable);
    }

    public void updateTabNotification(final MagnoliaTab tab, final String text) {
        tab.setNotification(text);
    }

    public void hideTabNotification(final MagnoliaTab tab) {
        tab.hideNotification();
    }

}
