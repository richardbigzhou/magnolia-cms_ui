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
package info.magnolia.ui.widget.dialog.gwt.client.dialoglayout;

import info.magnolia.ui.widget.dialog.gwt.client.VDialogHeader;
import info.magnolia.ui.widget.dialog.gwt.client.VDialogHeader.VDialogHeaderCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link VBaseDialogViewImpl}. Implements {@link VBaseDialogView}.
 */
public class VBaseDialogViewImpl extends ComplexPanel implements VBaseDialogView {

    private static final String CLASSNAME = "dialog-panel";

    private static final String CLASSNAME_CONTENT = "dialog-content";

    private static final String CLASSNAME_FOOTER = "dialog-footer";

    private static final String CLASSNAME_BUTTON = "btn-dialog";

    private Presenter presenter;
    
    private Widget content;
    
    private VDialogHeader header = new VDialogHeader(createHeaderCallback());
    
    private Element contentEl = DOM.createDiv();
    
    private final Element footerEl = DOM.createDiv();
    
    public VBaseDialogViewImpl() {
        final Element root = DOM.createDiv();
        root.addClassName("dialog-root");
        setElement(root);
        add(header, root);
        root.appendChild(contentEl);
        root.appendChild(footerEl);
        setStylePrimaryName(CLASSNAME);
        contentEl.addClassName(CLASSNAME_CONTENT);
        footerEl.addClassName(CLASSNAME_FOOTER);
        header.setErrorAmount(0);
    }
    
    @Override
    public void addAction(final String name, String label) {
        final Button button = new Button(label);
        button.setStyleName(CLASSNAME_BUTTON);
        button.addStyleDependentName(name);
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                getPresenter().fireAction(name);
            }

        });
        add(button, footerEl);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
    
    protected VDialogHeaderCallback createHeaderCallback() {
        return new VDialogHeaderCallback() {
            
            @Override
            public void onDescriptionVisibilityChanged(boolean isVisible) {
                
            }
            
            @Override
            public void onCloseFired() {
                
            }
            
            @Override
            public void jumpToNextError() {
                
            }
        };
    }
    
    protected VDialogHeader getHeader() {
        return header;
    }

    @Override
    public void setDescription(String description) {
        header.setDescription(description);
    }

    @Override
    public void setCaption(String caption) {
        header.setDialogCaption(caption);
    }
    
    @Override
    public int getContentWidth() {
        return getOffsetWidth();
    }

    @Override
    public int getContentHeight() {
        return getOffsetHeight() - header.getOffsetHeight() - footerEl.getOffsetHeight();
    }

    @Override
    public void setContent(Widget contentWidget) {
        if (content != null) {
            remove(content);
        }
        this.content = contentWidget;
        add(contentWidget, contentEl);
    }

    @Override
    public Widget getContent() {
        return content;
    }
    
    
    public Element getContentEl() {
        return contentEl;
    }
}
