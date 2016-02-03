/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.pages.app.editor;

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;

import javax.inject.Inject;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * PageEditorViewImpl.
 */
public class PagesEditorSubAppViewImpl implements PagesEditorSubAppView {

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout container = new VerticalLayout();

    private Listener listener;

    private PageEditorView pageEditor;

    private ActionbarView actionBar;

    private final CssLayout actionBarWrapper = new CssLayout();

    private PageBarView pageBarView;

    @Inject
    public PagesEditorSubAppViewImpl(PageBarView pageBarView) {
        this.pageBarView = pageBarView;

        root.setSizeFull();
        root.setStyleName("pageeditor");
        root.addComponent(container);
        root.setExpandRatio(container, 1);
        root.setSpacing(true);
        root.setMargin(false);
        root.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                listener.onEscape();
            }
        });

        container.setSizeFull();
        container.addStyleName("editor");

        actionBarWrapper.setHeight(100, Unit.PERCENTAGE);
        actionBarWrapper.addStyleName("actionbar");
        root.addComponent(actionBarWrapper);
        root.setExpandRatio(actionBarWrapper, 0);

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        this.pageBarView.setListener(listener);
    }

    @Override
    public void setPageBarView(PageBarView pageBarView) {
        this.pageBarView = pageBarView;
        container.addComponentAsFirst(pageBarView.asVaadinComponent());
    }

    @Override
    public void setPageEditorView(PageEditorView pageEditor) {
        this.pageEditor = pageEditor;
        container.addComponent(pageEditor.asVaadinComponent(), 1);
        container.setExpandRatio(pageEditor.asVaadinComponent(), 1f);
    }

    @Override
    public void setActionbarView(final ActionbarView actionBar) {
        Component c = actionBar.asVaadinComponent();
        Component old = actionBarWrapper.getComponentCount() != 0 ? actionBarWrapper.getComponent(0) : null;
        if (old == null) {
            actionBarWrapper.addComponent(c);
        } else {
            actionBarWrapper.replaceComponent(old, c);
        }
        this.actionBar = actionBar;
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    @Override
    public void hideActionbar(boolean hide) {
        if (actionBar != null) {
            actionBar.asVaadinComponent().setVisible(!hide);
        }
    }

    @Override
    public void setContentView(View view) {

    }
}
