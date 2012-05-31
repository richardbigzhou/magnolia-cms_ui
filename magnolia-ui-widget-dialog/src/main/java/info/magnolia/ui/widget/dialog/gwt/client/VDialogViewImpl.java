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
package info.magnolia.ui.widget.dialog.gwt.client;


import info.magnolia.ui.widget.tabsheet.gwt.client.VShellTabSheet;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 * @author apchelintcev
 *
 */
public class VDialogViewImpl extends FlowPanel implements VDialogView {

    private Element header = DOM.createDiv();
    private Element content = DOM.createDiv();
    private Element footer = DOM.createDiv();

    private Element root;
    private VShellTabSheet tabsheet;
    private Presenter presenter;
    private EventBus eventBus;
    private String CLASSNAME = "dialog-panel";
    private String CLASSNAME_HEADER = "dialog-header";
    private String CLASSNAME_CONTENT = "dialog-content";
    private String CLASSNAME_FOOTER = "dialog-footer";

    private String CLASSNAME_BUTTON = "btn-dialog";

    public VDialogViewImpl(final EventBus eventBus) {
        super();
        setStylePrimaryName(CLASSNAME);
        header.addClassName(CLASSNAME_HEADER);
        content.addClassName(CLASSNAME_CONTENT);
        footer.addClassName(CLASSNAME_FOOTER);

        this.eventBus = eventBus;
        this.root = getElement();
        root.appendChild(header);
        root.appendChild(content);
        root.appendChild(footer);
        setCaption("Edit page properties");
    }

    @Override
    public void setPresenter(Presenter vDialog) {
        this.presenter = presenter;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public VShellTabSheet getTabSheet() {
        return tabsheet;
    }


    @Override
    public void addTabSheet(VShellTabSheet tabsheet) {
        this.tabsheet = tabsheet;
        add(tabsheet, content);
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        boolean isChild = false;
        for (Widget widget : getChildren()) {
            if (component == widget) {
                isChild = true;
            }
        }
        return isChild;
    }

    @Override
    public void addAction(String label, String action) {
        Button button = new Button(label);
        button.setStyleName(CLASSNAME_BUTTON);
        button.addStyleName(CLASSNAME_BUTTON + "-" +label);
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
                VDialogViewImpl.this.getPresenter();
            }

        });
        add(button, footer);
    }

    void setCaption(String caption) {
        Label label = new Label(caption);
        add(label, header);
    }

}