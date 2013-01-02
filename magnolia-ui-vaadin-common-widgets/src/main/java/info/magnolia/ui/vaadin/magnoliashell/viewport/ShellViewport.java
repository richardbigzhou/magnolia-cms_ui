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
package info.magnolia.ui.vaadin.magnoliashell.viewport;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportState;
import info.magnolia.ui.vaadin.magnoliashell.DeckLayout;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShellBase;

import com.vaadin.ui.Component;

/**
 * The server side implementation of the shell viewport. MagnoliaShell is capable of holding of such for the shell apps,
 * one - for the regular apps.
 */
public class ShellViewport extends DeckLayout implements ViewPort {

    private MagnoliaShellBase parentShell;

    private View view;

    public ShellViewport(MagnoliaShellBase shell) {
        super();
        display(null);
        this.parentShell = shell;
    }

    public void setCurrentShellFragment(String currentShellFragment) {
        getState().currentFragment = currentShellFragment;
    }

    public String getCurrentShellFragment() {
        return getState(false).currentFragment;
    }

    @Override
    public void setView(final View view) {
        if (view != null) {
            this.view = view;
            display(this.view.asVaadinComponent());
            parentShell.setActiveViewport(this);
        }
    }

    @Override
    public void display(Component content) {
        getState().formerActive = getState().activeComponent;
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
}
