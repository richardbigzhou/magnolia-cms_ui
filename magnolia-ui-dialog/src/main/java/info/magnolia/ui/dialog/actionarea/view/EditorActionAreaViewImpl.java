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
package info.magnolia.ui.dialog.actionarea.view;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import info.magnolia.ui.api.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link EditorActionAreaView}. Composes dialog/editor footer with Vaadin components.
 */
public class EditorActionAreaViewImpl implements EditorActionAreaView {

    private HorizontalLayout footer = new HorizontalLayout();

    private CssLayout primaryActionsContainer = new CssLayout();

    private CssLayout secondaryActionsContainer = new CssLayout();

    private CssLayout toolbarContainer = new CssLayout();

    private Map<String, View> actionNameToView = new HashMap<String, View>();

    public EditorActionAreaViewImpl() {
        footer.addStyleName("footer");
        footer.addComponent(toolbarContainer);
        footer.addComponent(secondaryActionsContainer);
        footer.addComponent(primaryActionsContainer);
        footer.setExpandRatio(primaryActionsContainer, 1f);
        footer.setExpandRatio(secondaryActionsContainer, 1f);


        footer.setWidth("100%");
        secondaryActionsContainer.addStyleName("secondary-actions");
        primaryActionsContainer.addStyleName("primary-actions");
        secondaryActionsContainer.setWidth("100%");
        primaryActionsContainer.setWidth("100%");
    }

    @Override
    public Component asVaadinComponent() {
        return footer;
    }

    @Override
    public void addPrimaryAction(View actionView, String actionName) {
        actionNameToView.put(actionName, actionView);
        primaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void addSecondaryAction(View actionView, String actionName) {
        actionNameToView.put(actionName, actionView);
        secondaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void removeAllActions() {
        primaryActionsContainer.removeAllComponents();
        secondaryActionsContainer.removeAllComponents();
        actionNameToView.clear();
    }

    @Override
    public void setToolbarComponent(Component toolbar) {
        toolbarContainer.removeAllComponents();
        toolbarContainer.addComponent(toolbar);
    }

    @Override
    public View getViewForAction(String actionName) {
        return actionNameToView.get(actionName);
    }
}
