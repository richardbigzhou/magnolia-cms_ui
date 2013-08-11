/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.richtext;

import info.magnolia.ui.vaadin.gwt.client.dialog.widget.OverlayWidget;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormView;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.richtext.TextAreaStretcher;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector for {@link info.magnolia.ui.vaadin.richtext.TextAreaStretcher}.
 */
@Connect(TextAreaStretcher.class)
public class TextAreaStretcherConnector extends AbstractExtensionConnector {

    private Widget form;
    private Widget dialog;
    private Widget textWidget;
    private ComputedStyle parentCs;
    private Element button;
    private WindowResizeListener windowResizeListener = new WindowResizeListener();
    private boolean isOverlay = false;

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (stateChangeEvent.hasPropertyChanged("isCollapsed")) {
            toggleCollapseState();
        }
    }

    private void toggleCollapseState() {
        boolean isCollapsed = getState().isCollapsed;
        if (isCollapsed) {
            button.replaceClassName("stretched", "collapsed");
        } else {
            button.replaceClassName("collapsed", "stretched");
        }

        if (!isCollapsed) {
            final Element header = getDialogHeaderElement();
            final ComputedStyle cs = new ComputedStyle(header);
            Style style = textWidget.getElement().getStyle();
            style.setPosition(Style.Position.ABSOLUTE);

            style.setLeft(form.getAbsoluteLeft(), Style.Unit.PX);
            int dialogHeaderPadding = cs.getPadding()[0] + cs.getPadding()[2];
            style.setTop(form.getAbsoluteTop() - dialog.getAbsoluteTop() + dialogHeaderPadding, Style.Unit.PX);
            style.setWidth(form.getOffsetWidth(), Style.Unit.PX);
        }

        final LayoutManager lm = getParent().getLayoutManager();
        final UIConnector ui = getConnection().getUIConnector();

        if (isCollapsed) {
            lm.removeElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
        } else {
            lm.addElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
        }
    }

    @Override
    public ComponentConnector getParent() {
        return (ComponentConnector) super.getParent();
    }

    @Override
    protected void extend(ServerConnector target) {
        AbstractComponentConnector cc = (AbstractComponentConnector) target;
        final Widget w = cc.getWidget();
        final Element rootElement = w.getElement();
        this.textWidget = getParent().getWidget();
        this.parentCs = new ComputedStyle(textWidget.getElement());
        if ("textarea".equalsIgnoreCase(rootElement.getTagName())) {
            button = DOM.createButton();
            button.setInnerHTML("T");
            button.setClassName("textarea-expander");
            w.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent attachEvent) {
                    initFormView();
                    initDialog();
                    checkOverlay();
                    rootElement.getParentElement().appendChild(button);
                    Widget parent = w.getParent();
                    parent.addDomHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            Element target = event.getNativeEvent().getEventTarget().cast();
                            if (button.isOrHasChild(target)) {
                                getRpcProxy(TextAreaStretcherServerRpc.class).toggle();
                            }
                        }
                    }, ClickEvent.getType());
                }
            });
        }
    }

    @Override
    public TextAreaStretcherState getState() {
        return (TextAreaStretcherState) super.getState();
    }

    private Element getDialogHeaderElement() {
        return JQueryWrapper.select(dialog.asWidget()).find(".dialog-header").get(0);
    }

    private void checkOverlay() {
        Widget it = this.dialog.asWidget();
        while (it != null && !isOverlay) {
            it = it.getParent();
            this.isOverlay = it instanceof OverlayWidget;
        }
    }

    private void initDialog() {
        this.dialog = form.getParent();
    }

    private void initFormView() {
        Widget it = textWidget;
        while (it != null && !(it instanceof FormView)) {
            it = it.getParent();
        }
        this.form = (it instanceof FormView) ? it : null;
    }

    private class WindowResizeListener implements ElementResizeListener {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            int formTop = form.getAbsoluteTop();
            int textAreaPadding = parentCs.getPadding()[0] + parentCs.getPadding()[2];
            int textAreaBorder = parentCs.getBorder()[0] + parentCs.getBorder()[2];
            textWidget.setHeight((e.getLayoutManager().getOuterHeight(e.getElement())
                    - formTop - textAreaPadding - textAreaBorder - 1) + "px");
        }
    }
}
