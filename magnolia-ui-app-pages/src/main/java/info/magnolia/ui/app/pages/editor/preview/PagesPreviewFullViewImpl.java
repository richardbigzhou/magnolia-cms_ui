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

import info.magnolia.ui.widget.actionbar.ActionbarView;
import info.magnolia.ui.widget.editor.PageEditorView;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link PagesPreviewFullViewView}.
 */
public class PagesPreviewFullViewImpl implements PagesPreviewFullView {

    private Panel wrapper = new Panel();

    private Embedded iframe;

    protected Listener listener;

    public PagesPreviewFullViewImpl() {
        this.iframe = new Embedded();
        iframe.setType(Embedded.TYPE_BROWSER);
        iframe.setSizeFull();
        wrapper.setSizeFull();
        wrapper.getContent().setSizeFull();
        final Button closePreviewButton = new Button("Close Preview", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listener.closePreview();
            }
        });
        closePreviewButton.setWidth("100%");
        wrapper.addComponent(closePreviewButton);
        wrapper.addComponent(iframe);
        ((VerticalLayout) wrapper.getContent()).setExpandRatio(iframe, 1f);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return wrapper;
    }

    @Override
    public void setUrl(String url) {
        iframe.setSource(new ExternalResource(url));
    }

    @Override
    public void setActionbarView(final ActionbarView actionbar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPageEditor(PageEditorView pageEditor) {
        // TODO Auto-generated method stub

    }
}
