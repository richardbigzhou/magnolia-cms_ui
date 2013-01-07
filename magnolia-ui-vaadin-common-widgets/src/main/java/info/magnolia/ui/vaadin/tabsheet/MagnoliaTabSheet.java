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

import info.magnolia.ui.vaadin.common.ComponentIterator;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.connector.MagnoliaTabSheetState;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.rpc.MagnoliaTabSheetClientRpc;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.rpc.MagnoliaTabSheetServerRpc;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.util.CollectionUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.VerticalLayout;

/**
 * Simple lightweight tabsheet component.
 */
public class MagnoliaTabSheet extends AbstractComponentContainer {

    private final List<MagnoliaTab> tabs = new LinkedList<MagnoliaTab>();

    public MagnoliaTabSheet() {
        super();
        setImmediate(true);
        registerRpc(new MagnoliaTabSheetServerRpc() {
            @Override
            public void setActiveTab(Connector tabConnector) {
                MagnoliaTabSheet.this.setActiveTab((MagnoliaTab) tabConnector);
            }

            @Override
            public void closeTab(Connector tabConnector) {
                MagnoliaTabSheet.this.closeTab((MagnoliaTab) tabConnector);
            }
        });
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

    public ComponentContainer addTabStub(String string) {
        final VerticalLayout c = new VerticalLayout();
        addTab(string, c);
        return c;
    }

    public MagnoliaTab addTab(final String caption, final HasComponents c) {
        final MagnoliaTab tab = new MagnoliaTab(caption, c);
        doAddTab(tab);
        return tab;
    }

    @Override
    protected MagnoliaTabSheetState getState() {
        return (MagnoliaTabSheetState) super.getState();
    }

    @Override
    protected MagnoliaTabSheetState getState(boolean markDirty) {
        return (MagnoliaTabSheetState) super.getState(markDirty);
    }

    public void showAllTab(boolean showAll, String label) {
        getState().showAllEnabled = showAll;
        getState().showAllLabel = label;
    }

    public void setFullscreen(boolean isFullscreen) {
        getRpcProxy(MagnoliaTabSheetClientRpc.class).toggleFullScreenMode(isFullscreen);
    }

    protected void closeTab(MagnoliaTab tab) {
        if (getState().activeTab == tab) {
            final MagnoliaTab nextTab = getNextTab(tab);
            if (nextTab != null && nextTab != tab) {
                setActiveTab(nextTab);
            }
        }
        removeComponent(tab);
    }

    protected void doAddTab(final MagnoliaTab tab) {
        super.addComponent(tab);
        tabs.add(tab);
        if (getState().activeTab == null) {
            setActiveTab(tab);
        }
    }

    public MagnoliaTab getActiveTab() {
        return (MagnoliaTab) getState(false).activeTab;
    }

    protected MagnoliaTab getNextTab(final MagnoliaTab tab) {
        return CollectionUtil.getNext(tabs, tab);
    }

    @Override
    public void removeComponent(final Component c) {
        if (c instanceof MagnoliaTab) {
            final MagnoliaTab tab = (MagnoliaTab) c;
            super.removeComponent(c);
            tabs.remove(tab);
            markAsDirty();
        }
    }

    public void setActiveTab(final MagnoliaTab tab) {
        Iterator<Component> it = iterator();
        while (it.hasNext()) {
            MagnoliaTab tabIt = (MagnoliaTab)it.next();
            if (tabIt.getContent() != null) {
                tabIt.getContent().setVisible(tabIt == tab);   
            }
        }
        getState().activeTab = tab;
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {}

    @Override
    public int getComponentCount() {
        return tabs.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return new ComponentIterator<MagnoliaTab>(tabs.iterator());
    }
}
