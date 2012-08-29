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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.ui.widget.actionbar.ActionbarView;
import info.magnolia.ui.widget.editor.PageEditorView;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


/**
 * PageEditorViewImpl.
 */
@SuppressWarnings("serial")
public class PagesEditorSubAppViewImpl implements PagesEditorSubAppView {

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout container = new VerticalLayout();

    private Listener listener;

    private PageEditorView pageEditor;

    private ActionbarView actionbar;

    public PagesEditorSubAppViewImpl() {

        root.setSizeFull();
        root.setStyleName("workbench");
        root.addComponent(container);
        root.setExpandRatio(container, 1);
        root.setSpacing(true);
        root.setMargin(false);

        container.setSizeFull();
        container.setImmediate(true);

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }



    @Override
    public void setPageEditorView(PageEditorView pageEditor) {
        container.addComponent(pageEditor.asVaadinComponent());
        this.pageEditor = pageEditor;
    }

    @Override
    public void setActionbarView(final ActionbarView actionbar) {
        actionbar.asVaadinComponent().setWidth(Sizeable.SIZE_UNDEFINED, 0);
        root.addComponent(actionbar.asVaadinComponent());
        this.actionbar = actionbar;
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    @Override
    public void hideActionbar(boolean hide) {
        if(actionbar != null) {
            actionbar.asVaadinComponent().setVisible(!hide);
        }
    }
}
