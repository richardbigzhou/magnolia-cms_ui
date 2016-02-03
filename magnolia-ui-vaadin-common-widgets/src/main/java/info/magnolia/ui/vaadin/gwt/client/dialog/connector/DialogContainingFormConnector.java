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
package info.magnolia.ui.vaadin.gwt.client.dialog.connector;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import info.magnolia.ui.vaadin.dialog.FormDialog;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.BaseDialogView;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormViewImpl;

import java.util.Arrays;
import java.util.List;

/**
 * DialogContainingForm assumes that content of dialog is
 * FormView. This connector will set the height of form view
 * based on how much space this dialog can provide to the form.
 */
@Connect(FormDialog.class)
public class DialogContainingFormConnector extends BaseDialogConnector implements ResizeHandler {

    private BaseDialogView view;
    private HandlerRegistration registration;

    @Override
    protected BaseDialogView createView() {
        this.view = super.createView();
        return this.view;
    }

    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getWidget().getElement(), listener);
        registration = Window.addResizeHandler(this);
    }

    private final ElementResizeListener listener = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            doResize();
        }
    };

    @Override
    public void onUnregister() {
        registration.removeHandler();
        getLayoutManager().removeElementResizeListener(getWidget().getElement(), listener);
    }

    /**
     * Calculates and sets the max height of form view.
     */
    @Override
    public void onResize(ResizeEvent event) {
        doResize();
    }

    private void doResize() {
        Widget content = getContent().getWidget();
        if (content instanceof FormViewImpl) {
            FormViewImpl formView = (FormViewImpl) content;
            Element element = view.asWidget().getElement();
            NodeList<Node> childNodes = element.getChildNodes();
            int footerHeight = 0, headerHeight = 0, marginHeight = 0;
            List<String> marginElements = Arrays.asList("dialog-description", "dialog-error", "dialog-content", "dialog-footer");
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.getItem(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) item;
                    if (child.getClassName().equalsIgnoreCase("dialog-footer")) {
                        footerHeight = child.getOffsetHeight();
                    } else if (child.getClassName().isEmpty()) {
                        headerHeight = child.getOffsetHeight();
                    }
                    if (marginElements.contains(child.getClassName())) {
                        marginHeight += 2;
                    }
                }
            }
            formView.setMaxHeight(view.asWidget().getElement().getOffsetHeight() - footerHeight - headerHeight - marginHeight);
        }
    }
}
