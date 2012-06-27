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
package info.magnolia.ui.vaadin.integration.widget.client;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VScrollTable.VScrollTableBody.VScrollTableRow;
import com.vaadin.terminal.gwt.client.ui.VTreeTable;

/**
 * Client side implementation of the Hybrid selection Tree Table.
 * 
 * @author apchelintcev
 * 
 */
public class VHybridSelectionTreeTable extends VTreeTable {

    final CheckBox cb = new CheckBox();

    public VHybridSelectionTreeTable() {
        super();
        cb.addStyleName("v-select-all");
        add(cb, getElement());
        cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                client.updateVariable(paintableId, "selectAll", event.getValue(), true);
            }
        });

        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                final Element rowEl = findParentRowElement(target);
                if (rowEl != null) {
                    final NodeList<?> cbList = target.getElementsByTagName("input");
                    if (cbList != null && cbList.getLength() > 0) {
                        final Element cb = (Element) cbList.getItem(0);
                        cb.setPropertyBoolean("checked", !target.getClassName().contains("select"));
                    }
                }
            }
        }, MouseUpEvent.getType());

        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                final NativeEvent nativeEvent = event.getNativeEvent();
                int eventCode = event.getTypeInt();
                if (eventCode == Event.ONMOUSEUP) {
                    final Element target = nativeEvent.getEventTarget().cast();
                    if (target.getTagName().equalsIgnoreCase("input") && target.getClassName().contains("v-selection-cb")) {
                        event.cancel();
                        final Element rowElement = findParentRowElement(target);
                        if (rowElement != null) {
                            boolean isSelected = target.getPropertyBoolean("checked");
                            final VScrollTableRow row = findWidget(rowElement, VScrollTableRow.class);
                            client.updateVariable(paintableId, "toggleSelection", !isSelected, false);
                            client.updateVariable(paintableId, "toggledRowId", row.getKey(), false);
                            client.sendPendingVariableChanges();
                        }

                    }
                }
            }
        });
    }

    @Override
    protected VScrollTableBody createScrollBody() {
        VScrollTableBody body = super.createScrollBody();
        addDOMCallbacks(body.getElement());
        return body;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        final JsArray<Element> rows = JQueryWrapper.select(".v-table-row, .v-table-row-odd").get();
        if (rows != null && rows.length() != 0) {
            for (int i = 0; i < rows.length(); ++i) {
                final VScrollTableRow row = findWidget(rows.get(i), VScrollTableRow.class);
                final Node cb = row.getElement().getElementsByTagName("input").getItem(0);
                if (cb != null) {
                    ((Element) cb.cast()).setPropertyBoolean("checked", row.getStyleName().contains("select"));
                }
            }
        }
    }

    private native void addDOMCallbacks(Element body) /*-{
        var table = body.getElementsByTagName("tbody")[0];
        if (table != 'undefined') {
            table._appendChild = table.appendChild;
            table._insertBefore = table.insertBefore;
            var ref = this;
            table.appendChild = function(element) {
                this._appendChild(element);
                ref.@info.magnolia.ui.vaadin.integration.widget.client.VHybridSelectionTreeTable::addCheckBox(Lcom/google/gwt/user/client/Element;)(element);
            }
            table.insertBefore = function(newElement, refElement) {
                this._insertBefore(newElement, refElement);
                ref.@info.magnolia.ui.vaadin.integration.widget.client.VHybridSelectionTreeTable::addCheckBox(Lcom/google/gwt/user/client/Element;)(newElement);
            }
       }
    }-*/;

    public void addCheckBox(final Element element) {
        if (element.getTagName().equalsIgnoreCase("tr")) {
            final Element cb = DOM.createInputCheck();
            final Element refDiv = element.getElementsByTagName("div").getItem(0).cast();
            cb.addClassName("v-selection-cb");
            if (element.getClassName().contains("select")) {
                cb.setPropertyBoolean("checked", true);
            }
            refDiv.appendChild(cb);
            DOM.sinkEvents(element, Event.MOUSEEVENTS);
        }
    }

    @Override
    public void deselectAll() {
        super.deselectAll();
        final JsArray<Element> cbs = JQueryWrapper.select(".v-selection-cb").get();
        for (int i = 0; i < cbs.length(); ++i) {
            cbs.get(i).setPropertyBoolean("checked", false);
        }
    }

    protected static Element findParentRowElement(final Element el) {
        Element itEl = el;
        while (!itEl.getTagName().equalsIgnoreCase("tr") && itEl.hasParentElement()) {
            itEl = itEl.getParentElement().cast();
        }
        return itEl;
    }

    @SuppressWarnings("unchecked")
    public static <T> T findWidget(Element element, Class<? extends Widget> class1) {
        if (element != null) {
            EventListener eventListener = null;
            while (eventListener == null && element != null) {
                eventListener = Event.getEventListener(element);
                if (eventListener == null) {
                    element = (Element) element.getParentElement();
                }
            }
            if (eventListener != null) {
                return (T) eventListener;
            }
        }
        return null;
    }

}
