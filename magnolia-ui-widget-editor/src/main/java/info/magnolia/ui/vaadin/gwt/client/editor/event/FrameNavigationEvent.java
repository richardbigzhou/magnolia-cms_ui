/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * FrameNavigationEvent is used to notify the {@link info.magnolia.ui.vaadin.gwt.client.connector.PageEditorConnector} when the url inside the iFrame is changed.
 * Used to update the serverside implementation when navigating inside the iFrame and to overcome a limitation in frame handling on webkit browser.
 * This event is fired by the onClick method defined in {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.ElementProcessor#registerOnclick(com.google.gwt.dom.client.Element)}.
 */
public class FrameNavigationEvent extends GwtEvent<FrameNavigationEventHandler> {

    public static Type<FrameNavigationEventHandler> TYPE = new Type<FrameNavigationEventHandler>();

    private final String path;

    public FrameNavigationEvent(String path) {
        this.path = path;
    }

    @Override
    public Type<FrameNavigationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FrameNavigationEventHandler handler) {
        handler.onFrameUrlChanged(this);

    }

    public String getPath() {
        return path;
    }
}
