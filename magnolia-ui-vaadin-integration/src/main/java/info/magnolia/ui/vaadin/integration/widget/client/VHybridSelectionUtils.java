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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ui.VScrollTable.VScrollTableBody.VScrollTableRow;

/**
 * Utility class for Hybrid selection widgets.
 */
public class VHybridSelectionUtils {

    /**
     * Common handler for mouse up events.
     */
    public static class VHybridSelectionMouseUpHandler implements MouseUpHandler {
        
        private final CheckBox selectAllCheckBox;
        
        public VHybridSelectionMouseUpHandler(final CheckBox selectAllCheckBox) {
            super();
            this.selectAllCheckBox = selectAllCheckBox;
        }
        
        @Override
        public void onMouseUp(final MouseUpEvent event) {       
            if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
                if (!event.getNativeEvent().getShiftKey()) {
                    final Element target = event.getNativeEvent().getEventTarget().cast();
                    final Element rowEl = VHybridSelectionUtils.findParentRowElement(target);
                    if (rowEl != null) {
                        toggleRowCheckBox(rowEl);
                    }   
                    updateSelectAllControl(selectAllCheckBox);
                } else {
                    JsArray<Element> selectedRows = JQueryWrapper.select(".v-selected").get();
                    if (selectedRows != null) {
                        for (int i = 0; i < selectedRows.length(); ++i) {
                            toggleRowCheckBox(selectedRows.get(i));
                        }
                    }
                    updateSelectAllControl(selectAllCheckBox);
                }
            }
        }
    };
    
    public static void toggleRowCheckBox(final Element rowEl) {
        final NodeList<?> cbList = rowEl.getElementsByTagName("input");
        if (cbList != null && cbList.getLength() > 0) {
            final Element cb = (Element) cbList.getItem(0);
            final String className = rowEl.getClassName();
            cb.setPropertyBoolean("checked", className.contains("select"));
        }
    }
    
    protected static Element findParentRowElement(final Element el) {
        Element itEl = el;
        while (!itEl.getTagName().equalsIgnoreCase("tr") && itEl.hasParentElement()) {
            itEl = itEl.getParentElement().cast();
        }
        if (!itEl.getTagName().equalsIgnoreCase("tr")) {
            itEl = null;
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
    
    public static void updateSelectAllControl(final CheckBox selectAllCheckBox) {
        final JsArray<Element> selectedRows = JQueryWrapper.select(".v-selected").get();
        final JsArray<Element> totalRows = JQueryWrapper.select(".v-table-row, .v-table-row-odd").get();
        selectAllCheckBox.setValue(selectedRows.length() == totalRows.length());
    }

    public static void deselectAllCheckBoxes() {
        final JsArray<Element> cbs = JQueryWrapper.select(".v-selection-cb").get();
        for (int i = 0; i < cbs.length(); ++i) {
            cbs.get(i).setPropertyBoolean("checked", false);
        }
    }

    public static void updateCheckBoxesForSelectedRows() {
        final JsArray<Element> rows = JQueryWrapper.select(".v-table-row, .v-table-row-odd").get();
        if (rows != null && rows.length() != 0) {
            for (int i = 0; i < rows.length(); ++i) {
                final VScrollTableRow row = VHybridSelectionUtils.findWidget(rows.get(i), VScrollTableRow.class);
                final Node cb = row.getElement().getElementsByTagName("input").getItem(0);
                if (cb != null) {
                    ((Element) cb.cast()).setPropertyBoolean("checked", row.getStyleName().contains("select"));
                }
            }
        }
    }
}
