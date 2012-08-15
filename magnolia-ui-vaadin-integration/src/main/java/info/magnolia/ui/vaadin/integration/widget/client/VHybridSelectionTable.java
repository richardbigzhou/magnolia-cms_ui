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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VScrollTable;
import com.vaadin.terminal.gwt.client.ui.VScrollTable.VScrollTableBody.VScrollTableRow;

/**
 * Client side iimpl of HybridSelection table.
 */
public class VHybridSelectionTable extends VScrollTable {

    final CheckBox selectAllCheckBox = new CheckBox();

    public VHybridSelectionTable() {
        selectAllCheckBox.addStyleName("v-select-all");
        add(selectAllCheckBox, getElement());
        selectAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                client.updateVariable(paintableId, "selectAll", event.getValue(), true);
            }
        });

        addDomHandler(new VHybridSelectionUtils.VHybridSelectionMouseUpHandler(selectAllCheckBox, this), MouseUpEvent.getType());

        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                final NativeEvent nativeEvent = event.getNativeEvent();
                int eventCode = event.getTypeInt();
                if (eventCode == Event.ONMOUSEUP) {
                    final Element target = nativeEvent.getEventTarget().cast();
                    if (target.getTagName().equalsIgnoreCase("input") && target.getClassName().contains("v-selection-cb")) {
                        event.cancel();
                        final Element rowElement = VHybridSelectionUtils.findParentRowElement(target);
                        if (rowElement != null) {
                            boolean isSelected = target.getPropertyBoolean("checked");
                            final VScrollTableRow row = VHybridSelectionUtils.findWidget(rowElement, VScrollTableRow.class);
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
        return new VHybridSelectionTableBody();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                VHybridSelectionUtils.updateCheckBoxesForSelectedRows(VHybridSelectionTable.this);
                VHybridSelectionUtils.updateSelectAllControl(selectAllCheckBox, VHybridSelectionTable.this);
            }
        });
    }

    @Override
    public void deselectAll() {
        super.deselectAll();
        VHybridSelectionUtils.deselectAllCheckBoxes(this);
    }
    
    /**
     * Custom body.
     */
    protected class VHybridSelectionTableBody extends VScrollTableBody {

        @Override
        protected VScrollTableRow createRow(UIDL uidl, char[] aligns2) {
            final VHybridSelectionTableRow row = new VHybridSelectionTableRow(uidl, aligns2);
            row.addCheckBox();
            return row;
        }

        /**
         * Custom row.
         */
        protected class VHybridSelectionTableRow extends VScrollTableBody.VScrollTableRow {

            public VHybridSelectionTableRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
            }

            public void addCheckBox() {
                final Element element = getElement();
                final Element cb = DOM.createInputCheck();
                final Element refDiv = element.getElementsByTagName("div").getItem(0).cast();
                cb.addClassName("v-selection-cb");
                if (element.getClassName().contains("select")) {
                    cb.setPropertyBoolean("checked", true);
                }
                refDiv.appendChild(cb);
            }

            @Override
            protected boolean isRenderHtmlInCells() {
                return true;
            }
        }
    }

}
