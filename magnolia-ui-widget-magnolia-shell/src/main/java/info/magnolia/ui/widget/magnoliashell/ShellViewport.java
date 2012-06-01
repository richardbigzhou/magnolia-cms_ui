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
package info.magnolia.ui.widget.magnoliashell;

import info.magnolia.ui.framework.app.ShellView;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VShellViewport;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;

import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

/**
 * The server side implementation of the shell viewport. MagnoliaShell is capable of holding of such for 
 * the shell apps, one - for the regular apps.
 * @author apchelintcev
 *
 */
@SuppressWarnings("serial")
@ClientWidget(value=VShellViewport.class, loadStyle = LoadStyle.EAGER)
public class ShellViewport extends DeckLayout implements ViewPort {
    
    private String currentShellFragment = "";
    
    private BaseMagnoliaShell parentShell;
    
    private ShellView view;
    
    public ShellViewport(final BaseMagnoliaShell shell) {
        super();
        setSizeFull();
        display(null);
        this.parentShell = shell;
    }
    
    
    public void setCurrentShellFragment(String currentShellFragment) {
        this.currentShellFragment = currentShellFragment;
    }
    
    public String getCurrentShellFragment() {
        return currentShellFragment;
    }

    public String getCurrentAppName() {
        String result = "";
        if (view != null) {
            result = view.getAppName();
        }
        return result;
    }
    
    public ShellView getView() {
        return view;
    }
    
    @Override
    public void setView(final View view) {
        if (view != null && !(view instanceof ShellView)) {
            throw new RuntimeException("Wrong type of view");
        }
        if (view != null){
            this.view = (ShellView)view;
            if (view instanceof IsVaadinComponent) {
                final Component c = ((IsVaadinComponent)view).asVaadinComponent();
                if (c instanceof ComponentContainer) {
                    display((ComponentContainer)c);
                    parentShell.setActiveViewport(this);
                }
            } else {
                throw new RuntimeException("The view passed can't be interpreted as Vaadin ComponentContainer!");   
            }
        }
    }
}
