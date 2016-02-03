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
package info.magnolia.ui.vaadin.gwt.client.dialog.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Basic implementation for {@link BaseDialogView}.
 */
public class BaseDialogViewImpl extends ComplexPanel implements BaseDialogView {


    protected static final String CLASSNAME_CONTENT = "dialog-content";

    protected static final String CLASSNAME_FOOTER = "dialog-footer";

    protected static final String CLASSNAME_FOOTER_TOOLBAR = "dialog-footer-toolbar";

    protected final DialogHeaderWidget header = createHeader();

    protected final Element contentEl = DOM.createDiv();

    protected final Element footerEl = DOM.createDiv();

    protected final Element footerToolbarEl = DOM.createDiv();

    private Presenter presenter;

    private Widget content;
    private Widget footerToolbar;

    public BaseDialogViewImpl() {
        final Element root = DOM.createDiv();
        root.addClassName("dialog-root");
        setElement(root);
        add(header, root);
        root.appendChild(contentEl);
        contentEl.addClassName(CLASSNAME_CONTENT);

        root.appendChild(footerEl);
        footerEl.addClassName(CLASSNAME_FOOTER);
        footerEl.appendChild(footerToolbarEl);
        footerToolbarEl.addClassName(CLASSNAME_FOOTER_TOOLBAR);
    }

    protected DialogHeaderWidget createHeader() {
        return new DialogHeaderWidget(createHeaderCallback());
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    protected DialogHeaderWidget.DialogHeaderCallback createHeaderCallback() {
        return new DialogHeaderWidget.DialogHeaderCallback() {
            @Override
            public void onDescriptionVisibilityChanged(boolean isVisible) {
                if (presenter != null) {
                    presenter.setDescriptionVisibility(isVisible);
                }
            }

            @Override
            public void onCloseFired() {
                presenter.closeDialog();
            }
        };
    }

    protected DialogHeaderWidget getHeader() {
        return header;
    }

    @Override
    public void setDescription(String description) {
        header.setDescription(description);
    }

    @Override
    public void setCaption(String caption) {
        header.setCaption(caption);
    }

    @Override
    public void showCloseButton() {
        header.showCloseButton();
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (content != null) {
            remove(content);
        }
        content = contentWidget;
        add(contentWidget, contentEl);
    }

    @Override
    public void setHeaderToolbar(Widget headerToolbarWidget) {
        header.setToolbar(headerToolbarWidget);
    }

    @Override
    public void setFooterToolbar(Widget footerToolbarWidget) {
        if (footerToolbar != null) {
            remove(footerToolbar);
        }
        footerToolbar = footerToolbarWidget;
        add(footerToolbarWidget, footerToolbarEl);
    }
}
