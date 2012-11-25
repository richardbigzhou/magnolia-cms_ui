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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.VMainLauncher.ShellAppType;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;


/**
 * Default view implementation for Pulse.
 */
public class PulseViewImpl implements PulseView {


    private String id= ShellAppType.PULSE.getClassId();

    @Override
    public String getId(){
        return id;
    }

    private final MagnoliaTabSheet tabsheet = new MagnoliaTabSheet() {

        @Override
        public void onActiveTabSet(String tabId) {
            super.onActiveTabSet(tabId);
            presenter.onPulseTabChanged(m.getKey(getTabById(tabId)).toString().toLowerCase());
        }
    };

    private enum PulseTabType {
        DASHBOARD, STATISTIC, MESSAGES;

        public static PulseTabType getDefault() {
            return MESSAGES;
        }
    }

    private Presenter presenter;

    private final BidiMap m = new DualHashBidiMap();

    private final PulseMessagesView messagesView;

    @Inject
    public PulseViewImpl(final PulseMessagesView messagesView, final PulseDashboardView dashboardView) {

        this.messagesView = messagesView;
        tabsheet.addStyleName("v-shell-tabsheet-light");
        final MagnoliaTab dashboard = tabsheet.addTab("Dashboard", (ComponentContainer) dashboardView.asVaadinComponent());
        final MagnoliaTab messages = tabsheet.addTab("Messages", (ComponentContainer) messagesView.asVaadinComponent());

        tabsheet.addStyleName("v-pulse");
        tabsheet.setSizeFull();
        tabsheet.setWidth("900px");

        tabsheet.setDebugId(id);

        m.put(PulseTabType.DASHBOARD, dashboard);
        m.put(PulseTabType.MESSAGES, messages);
    }

    @Override
    public Component asVaadinComponent() {
        return tabsheet;
    }

    @Override
    public String setCurrentPulseTab(final String tabId, final List<String> params) {
        PulseTabType type;
        String finalDisplayedTabId = tabId;
        try {
            type = PulseTabType.valueOf(String.valueOf(tabId).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = PulseTabType.getDefault();
            finalDisplayedTabId = type.name().toLowerCase();
        }
        final MagnoliaTab tab = (MagnoliaTab) m.get(type);
        if (tab != null) {
            tabsheet.setActiveTab(tab);
        }

        switch (type) {
            case MESSAGES :
                messagesView.update(params);
                break;
            default :
                break;
        }
        return finalDisplayedTabId;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
