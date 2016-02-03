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

import info.magnolia.ui.vaadin.gwt.client.tabsheet.widget.MagnoliaTabSheetView;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Fired when the set of TabSheet's tabs is changed.
 */
public class TabSetChangedEvent extends GwtEvent<TabSetChangedEvent.Handler> {

    public static final Type<TabSetChangedEvent.Handler> TYPE = new Type<TabSetChangedEvent.Handler>();

    private final MagnoliaTabSheetView tabSheet;

    /**
     * HasTabSetChangedHandlers.
     */
    public interface HasTabSetChangedHandlers {
        HandlerRegistration addTabSetChangedHandler(Handler handler);
    }

    /**
     * Handler.
     */
    public interface Handler extends EventHandler {
        void onTabSetChanged(final TabSetChangedEvent event);
    }

    public TabSetChangedEvent(final MagnoliaTabSheetView tabSheet) {
        this.tabSheet = tabSheet;
    }

    public MagnoliaTabSheetView getTabSheet() {
        return tabSheet;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onTabSetChanged(this);
    }
}
