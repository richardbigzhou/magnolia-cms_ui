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

import com.google.gwt.core.client.Scheduler;
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

    private Element button = DOM.createButton();
    private WindowResizeListener windowResizeListener = new WindowResizeListener();

    private boolean isOverlay = false;

    private StateChangeEvent.StateChangeHandler textAreaSizeHandler = new StateChangeEvent.StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            stretchTextArea(textWidget.getElement().getStyle());
        }
    };

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (stateChangeEvent.hasPropertyChanged("isCollapsed")) {
            toggleCollapseState();
            if (!getState().isCollapsed) {
                registerSizeChangeListeners();
            }
        }
    }

    @Override
    public ComponentConnector getParent() {
        return (ComponentConnector) super.getParent();
    }

    @Override
    protected void extend(ServerConnector target) {
        this.textWidget = getParent().getWidget();
        button.setInnerHTML("T");
        button.setClassName("textarea-expander");
        textWidget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                initFormView();
                initDialog();
                checkOverlay();
                if ("textarea".equalsIgnoreCase(textWidget.getElement().getTagName())) {
                    appendStretcher(textWidget.getElement());
                } else {
                    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                        @Override
                        public boolean execute() {
                            appendStretcher(JQueryWrapper.select(textWidget).find("iframe").get(0));
                            return false;
                        }
                    }, 500);
                }
            }
        });
    }

    @Override
    public TextAreaStretcherState getState() {
        return (TextAreaStretcherState) super.getState();
    }

    private void appendStretcher(Element rootElement) {
        rootElement.getParentElement().appendChild(button);
        Widget parent = textWidget.getParent();
        parent.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Element target = event.getNativeEvent().getEventTarget().cast();
                if (button.isOrHasChild(target)) {
                    if (!getState().isCollapsed) {
                        unregisterSizeChangeListeners();
                    }
                    getRpcProxy(TextAreaStretcherServerRpc.class).toggle(textWidget.getOffsetWidth(), textWidget.getOffsetHeight());

                }
            }
        }, ClickEvent.getType());
    }

    private void registerSizeChangeListeners() {
        final LayoutManager lm = getParent().getLayoutManager();
        final UIConnector ui = getConnection().getUIConnector();
        getParent().addStateChangeHandler(textAreaSizeHandler);
        lm.addElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
    }

    private void toggleCollapseState() {
        boolean isCollapsed = getState().isCollapsed;
        if (isCollapsed) {
            button.replaceClassName("stretched", "collapsed");
        } else {
            button.replaceClassName("collapsed", "stretched");
        }

        if (!isCollapsed) {
            Style style = textWidget.getElement().getStyle();
            style.setPosition(Style.Position.ABSOLUTE);
            final Element header = getDialogHeaderElement();
            final ComputedStyle cs = new ComputedStyle(header);

            int dialogHeaderPadding = cs.getPadding()[0] + cs.getPadding()[2];

            if (!isOverlay) {
                style.setLeft(form.getAbsoluteLeft(), Style.Unit.PX);
                style.setTop(form.getAbsoluteTop() - dialog.getAbsoluteTop() + dialogHeaderPadding, Style.Unit.PX);
            } else {
                style.setLeft(0, Style.Unit.PX);
                style.setTop(form.getAbsoluteTop() - dialog.getAbsoluteTop(), Style.Unit.PX);
            }

            stretchTextArea(style);
            style.setZIndex(3);
        } else {
            clearTraces();
        }
    }

    private void clearTraces() {
        Style style = textWidget.getElement().getStyle();
        style.clearLeft();
        style.clearTop();
        style.clearPosition();
        style.clearZIndex();
    }

    private void stretchTextArea(Style style) {
        style.setWidth(form.getOffsetWidth(), Style.Unit.PX);
        adjustTextAreaHeightToScreen(getConnection().getUIConnector().getWidget().getOffsetHeight());
    }

    private void unregisterSizeChangeListeners() {
        final LayoutManager lm = getParent().getLayoutManager();
        final UIConnector ui = getConnection().getUIConnector();
        getParent().removeStateChangeHandler(textAreaSizeHandler);
        lm.removeElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
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

    private void adjustTextAreaHeightToScreen(int uiHeight) {
        int formTop = form.getAbsoluteTop();
        textWidget.setHeight((uiHeight - formTop) + "px");
    }

    private class WindowResizeListener implements ElementResizeListener {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            adjustTextAreaHeightToScreen(e.getLayoutManager().getOuterHeight(e.getElement()));
        }
    }

}
