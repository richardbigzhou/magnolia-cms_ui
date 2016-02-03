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
package info.magnolia.ui.vaadin.gwt.client.tabsheet.event;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Event fired when the active tab in the tabsheet is changed.
 */
public class ActiveTabChangedEvent extends GwtEvent<ActiveTabChangedEvent.Handler> {

    public final static Type<ActiveTabChangedEvent.Handler> TYPE = new Type<ActiveTabChangedEvent.Handler>();

    /**
     * Handler.
     */
    public interface Handler extends EventHandler {
        void onActiveTabChanged(ActiveTabChangedEvent event);
    }

    /**
     * HasActiveTabChangeHandlers.
     */
    public interface HasActiveTabChangeHandlers {
        HandlerRegistration addActiveTabChangedHandler(Handler handler);
    }

    private final MagnoliaTabWidget tab;

    private boolean isShowingAllTabs = false;

    private boolean notifyServer = true;

    public ActiveTabChangedEvent(final MagnoliaTabWidget tab) {
        this.tab = tab;
    }

    public ActiveTabChangedEvent(final MagnoliaTabWidget tab, boolean notifyServer) {
        this.tab = tab;
        this.notifyServer = notifyServer;
    }

    public ActiveTabChangedEvent(boolean isShowingAll, boolean notifyServer) {
        this.tab = null;
        this.isShowingAllTabs = isShowingAll;
        this.notifyServer = notifyServer;
    }

    public boolean isShowingAllTabs() {
        return isShowingAllTabs;
    }

    public boolean isNotifyServer() {
        return notifyServer;
    }

    public MagnoliaTabWidget getTab() {
        return tab;
    }

    @Override
    protected void dispatch(ActiveTabChangedEvent.Handler handler) {
        handler.onActiveTabChanged(this);
    }

    @Override
    public GwtEvent.Type<ActiveTabChangedEvent.Handler> getAssociatedType() {
        return TYPE;
    }
}
