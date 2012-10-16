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
package info.magnolia.ui.admincentral.app.simple;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import com.vaadin.ui.ComponentContainer;


/**
 * View used to give all apps a uniform look-and-feel.
 */
@SuppressWarnings("serial")
public class AppFrameView implements View {

    /**
     * Listener.
     */
    public interface Listener {

        void onActiveTabSet(MagnoliaTab tab);

        void onTabClosed(MagnoliaTab tab);
    }

    private final MagnoliaTabSheet tabsheet = new MagnoliaTabSheet() {

        @Override
        public void onActiveTabSet(String tabId) {
            super.onActiveTabSet(tabId);
            listener.onActiveTabSet(super.getTabById(tabId));
        }

        @Override
        protected void closeTab(String tabId) {
            MagnoliaTab tab = super.getTabById(tabId);
            super.closeTab(tabId);
            listener.onTabClosed(tab);
        }
    };

    private Listener listener;

    public AppFrameView() {
        super();
        tabsheet.setSizeFull();
        tabsheet.addStyleName("app");
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public MagnoliaTab addTab(ComponentContainer cc, String caption, boolean closable) {
        final MagnoliaTab tab = new MagnoliaTab(caption, cc);
        tabsheet.addComponent(tab);
        tabsheet.setTabClosable(tab, closable);
        tabsheet.setActiveTab(tab);
        return tab;
    }

    public void closeTab(ComponentContainer cc) {
        tabsheet.removeComponent(cc);
    }

    @Override
    public MagnoliaTabSheet asVaadinComponent() {
        return tabsheet;
    }

    public void setActiveTab(MagnoliaTab tab) {
        tabsheet.setActiveTab(tab);
    }

    public MagnoliaTab getActiveTab() {
        return tabsheet.getActiveTab();
    }
}
