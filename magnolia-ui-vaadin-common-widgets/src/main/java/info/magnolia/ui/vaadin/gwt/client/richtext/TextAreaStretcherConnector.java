/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
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

    public static final int DELAY_MS = 500;
    private Widget form;
    private Widget dialog;
    private Widget textWidget;

    private Element stretchControl = DOM.createDiv();
    private WindowResizeListener windowResizeListener = new WindowResizeListener();

    private boolean isOverlay = false;
    private boolean isRichTextEditor = false;

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
        this.textWidget = ((ComponentConnector)target).getWidget();
        this.isRichTextEditor = !"textarea".equalsIgnoreCase(textWidget.getElement().getTagName());
        this.stretchControl.setClassName("textarea-stretcher");
        textWidget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                initFormView();
                initDialog();
                checkOverlay();
                if (!isRichTextEditor) {
                    appendStretcher(textWidget.getElement());
                } else {
                    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                        private int repeats = 0;

                        @Override
                        public boolean execute() {
                            repeats++;
                            isRichTextEditor = true;
                            Element iframe = JQueryWrapper.select(textWidget).find("iframe").get(0);
                            if (iframe != null) {
                                appendStretcher(iframe);
                                stretchControl.addClassName("rich-text");
                            }
                            return iframe == null && repeats < 5;
                        }
                    }, DELAY_MS);
                }
            }
        });
    }

    @Override
    public TextAreaStretcherState getState() {
        return (TextAreaStretcherState) super.getState();
    }

    private void appendStretcher(Element rootElement) {
        rootElement.getParentElement().insertAfter(stretchControl, rootElement);
        Widget parent = textWidget.getParent();
        TouchDelegate touchDelegate = new TouchDelegate(parent);
        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                Element target = event.getNativeEvent().getEventTarget().cast();
                if (stretchControl.isOrHasChild(target)) {
                    if (!getState().isCollapsed) {
                        unregisterSizeChangeListeners();
                    }
                    getRpcProxy(TextAreaStretcherServerRpc.class).toggle(textWidget.getOffsetWidth(), textWidget.getOffsetHeight());

                }
            }
        });
    }

    private void registerSizeChangeListeners() {
        final LayoutManager lm = getParent().getLayoutManager();
        final UIConnector ui = getConnection().getUIConnector();
        getParent().addStateChangeHandler(textAreaSizeHandler);
        lm.addElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
    }

    private void toggleCollapseState() {
        boolean isCollapsed = getState().isCollapsed;
        if (!isCollapsed) {
            stretchControl.replaceClassName("icon-open-fullscreen-2", "icon-close-fullscreen-2");
            stretchControl.replaceClassName("collapsed", "stretched");
            form.asWidget().addStyleName("textarea-stretched");

            Style style = textWidget.getElement().getStyle();
            style.setPosition(Style.Position.ABSOLUTE);
            Element header = getDialogHeaderElement();
            ComputedStyle headerCS = new ComputedStyle(header);

            int top = form.getAbsoluteTop() - dialog.getAbsoluteTop();
            top = isOverlay ? top : top + headerCS.getPadding()[0] + headerCS.getPadding()[2];

            int left = isOverlay ? 0 : form.getAbsoluteLeft();

            style.setLeft(left, Style.Unit.PX);
            style.setTop(top, Style.Unit.PX);

            stretchTextArea(style);
            style.setZIndex(3);

            if (!isOverlay && !isRichTextEditor) {
                stretchControl.getStyle().setTop(top + 5, Style.Unit.PX);
                stretchControl.getStyle().setLeft(left + textWidget.getOffsetWidth() - stretchControl.getOffsetWidth() - 5, Style.Unit.PX);

            }

        } else {
            stretchControl.replaceClassName("stretched", "collapsed");
            stretchControl.replaceClassName("icon-close-fullscreen-2", "icon-open-fullscreen-2");
            form.asWidget().removeStyleName("textarea-stretched");
            clearTraces();
        }
    }

    private void clearTraces() {
        Style style = textWidget.getElement().getStyle();
        style.clearLeft();
        style.clearTop();
        style.clearPosition();
        style.clearZIndex();

        stretchControl.getStyle().clearTop();
        stretchControl.getStyle().clearLeft();
    }

    private void stretchTextArea(Style style) {
        style.setWidth(form.getOffsetWidth(), Style.Unit.PX);
        adjustTextAreaHeightToScreen(getConnection().getUIConnector().getWidget().getOffsetHeight());
    }

    private void unregisterSizeChangeListeners() {
        final LayoutManager lm = getParent().getLayoutManager();
        final UIConnector ui = getConnection().getUIConnector();
        if (ui != null) {
            getParent().removeStateChangeHandler(textAreaSizeHandler);
            lm.removeElementResizeListener(ui.getWidget().getElement(), windowResizeListener);
        }
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
