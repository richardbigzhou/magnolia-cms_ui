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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.view.View;

import java.util.EnumMap;
import java.util.Map;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * Implementation of {@link DetailEditorView}.
 * Holds the {@link ActionbarView} and {@link DetailView} Currently lacking some functionality planned. See MGNLUI-154.
 */
public class DetailEditorViewImpl extends HorizontalLayout implements DetailEditorView {

    private final CssLayout itemViewContainer = new CssLayout();
    private final Map<DetailView.ViewType, DetailView> itemViews = new EnumMap<DetailView.ViewType, DetailView>(DetailView.ViewType.class);

    private ActionbarView actionbar;

    private DetailView.ViewType currentViewType = DetailView.ViewType.VIEW;

    private DetailEditorView.Listener contentWorkbenchViewListener;

    public DetailEditorViewImpl() {
        super();
        setSizeFull();
        setStyleName("workbench");
        setMargin(true);
        setSpacing(true);
        itemViewContainer.setSizeFull();
        itemViewContainer.setStyleName("detail-view");
        addComponent(itemViewContainer);
        setExpandRatio(itemViewContainer, 1);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setListener(DetailEditorView.Listener listener) {
        this.contentWorkbenchViewListener = listener;
    }

    @Override
    public void setViewType(final DetailView.ViewType type) {

        itemViewContainer.removeComponent(itemViews.get(currentViewType).asVaadinComponent());
        final Component c = itemViews.get(type).asVaadinComponent();

        c.setSizeFull();
        itemViewContainer.addComponent(c);

        this.currentViewType = type;
        refresh();
        this.contentWorkbenchViewListener.onViewTypeChanged(currentViewType);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void addItemView(DetailView.ViewType type, DetailView view) {

    }

    @Override
    public void setItemView(final View itemView) {
        itemView.asVaadinComponent().setWidth(null);
        itemView.asVaadinComponent().setHeight("100%");
        itemViewContainer.addComponent(itemView.asVaadinComponent());
    }

    @Override
    public void setActionbarView(final ActionbarView actionbar) {
        actionbar.asVaadinComponent().setWidth(null);
        if (this.actionbar == null) {
            addComponent(actionbar.asVaadinComponent());
        } else {
            replaceComponent(this.actionbar.asVaadinComponent(), actionbar.asVaadinComponent());
        }
        this.actionbar = actionbar;
    }

}
