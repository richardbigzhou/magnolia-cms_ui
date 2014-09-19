/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.vaadin.editor.pagebar;

import info.magnolia.ui.api.view.View;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Implements {@link PageBarView}.
 *
 * @param <L> listener interface.
 */
public class PageBarViewImpl<L extends PageBarView.Listener> extends CustomComponent implements PageBarView<L> {

    private CssLayout layout = new CssLayout();

    private Label pageNameLabel = new Label();

    protected L listener;

    public PageBarViewImpl() {
        super();
        setCompositionRoot(layout);
        construct();
    }

    private void construct() {
        layout.addStyleName("pagebar");

        this.pageNameLabel.setSizeUndefined();
        this.pageNameLabel.addStyleName("title");

        layout.addComponent(pageNameLabel);
    }

    @Override
    public void setPageName(String PageName) {
        this.pageNameLabel.setValue(PageName);
    }

    @Override
    public void setListener(L listener) {
        this.listener = listener;
    }

    @Override
    public void togglePreviewMode(boolean isPreview) {
        if (isPreview) {
            layout.addStyleName("preview");
        } else {
            layout.removeStyleName("preview");
        }
    }

    @Override
    public void addPageBarComponent(View component) {
        layout.addComponent(component.asVaadinComponent());
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

}
