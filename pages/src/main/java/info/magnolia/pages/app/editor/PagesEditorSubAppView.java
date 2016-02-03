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

import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.workbench.StatusBarView;

/**
 * PagesEditorSubAppView.
 */
public interface PagesEditorSubAppView extends ContentSubAppView {

    /**
     * Listener.
     */
    public interface Listener extends PageBarView.Listener {

        void onEscape();
    }

    void setListener(Listener listener);

    void setPageBarView(PageBarView pageBarView);

    void setPageEditorView(PageEditorView pageEditor);

    void setStatusBarView(StatusBarView statusBarView);

    /**
     * Use this method to add an action bar to this sub app view.
     */
    void setActionbarView(ActionbarView actionbar);

    /**
     * Shows/hides the actionbar. It has no effect if the actionbar hasn't yet been set.
     */
    void hideActionbar(boolean hide);

}
