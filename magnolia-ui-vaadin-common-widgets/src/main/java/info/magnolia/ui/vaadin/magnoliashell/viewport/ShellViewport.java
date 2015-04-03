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
package info.magnolia.ui.vaadin.magnoliashell.viewport;

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportState;
import info.magnolia.ui.vaadin.magnoliashell.DeckLayout;

import com.vaadin.ui.Component;

/**
 * The server side implementation of the shell viewport. MagnoliaShell is capable of holding of such for the shell apps,
 * one - for the regular apps.
 */
public class ShellViewport extends DeckLayout implements Viewport {

    private View view;

    public ShellViewport() {
        setSizeFull();
    }

    @Override
    public void setView(final View view) {
        if (view != null) {
            this.view = view;
            display(this.view.asVaadinComponent());
        } else {
            super.pop();
            getState().activeComponent = null;
        }
    }

    @Override
    public void display(Component content) {
        getState().activeComponent = content;
        super.display(content);
    }

    @Override
    protected ViewportState getState(boolean markAsDirty) {
        return (ViewportState) super.getState(markAsDirty);
    }

    @Override
    protected ViewportState getState() {
        return (ViewportState) super.getState();
    }
    
    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        if (initial && getState().activeComponent != null) {
            display((Component)getState().activeComponent);
        }
    }
}
