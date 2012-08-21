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
package info.magnolia.ui.app.pages.editor.preview;

import info.magnolia.ui.app.pages.editor.PagesEditorView;
import info.magnolia.ui.app.pages.editor.PagesEditorView.Listener;
import info.magnolia.ui.widget.actionbar.ActionbarView;
import info.magnolia.ui.widget.editor.PageEditorView;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * Implementation of {@link PagesPreviewView}.
 */
public class PagesPreviewViewImpl implements PagesEditorView {

    private VerticalLayout wrapper;
    private ActionbarView actionbar;

    public PagesPreviewViewImpl() {
        wrapper = new VerticalLayout();
        wrapper.addComponent(new Label("Non fullscreen preview here"));
    }
    @Override
    public Component asVaadinComponent() {
        return wrapper;
    }

    @Override
    public void setUrl(String url) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setActionbarView(ActionbarView actionbar) {
        actionbar.asVaadinComponent().setWidth(Sizeable.SIZE_UNDEFINED, 0);
        if (this.actionbar == null) {
            wrapper.addComponent(actionbar.asVaadinComponent());
        } else {
            wrapper.replaceComponent(this.actionbar.asVaadinComponent(), actionbar.asVaadinComponent());
        }
        this.actionbar = actionbar;
    }
    @Override
    public void setListener(Listener listener) {
        // TODO Auto-generated method stub

    }
    @Override
    public void setPageEditor(PageEditorView pageEditor) {
        // TODO Auto-generated method stub

    }

}
