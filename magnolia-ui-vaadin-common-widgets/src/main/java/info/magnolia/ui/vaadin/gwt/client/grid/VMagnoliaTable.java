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
package info.magnolia.ui.vaadin.gwt.client.grid;

import java.util.Iterator;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapRecognizer;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ui.VScrollTablePatched;
import com.vaadin.terminal.gwt.client.ui.dd.VDragAndDropManager;
import com.vaadin.terminal.gwt.client.ui.dd.VDragEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VTransferable;

/**
 * TODO dlipp: this type is to be streamlined. See SCRUM-1776 for additional info.
 */
public class VMagnoliaTable extends VScrollTablePatched {
    static int checkboxWidth = -1;
    
    public VMagnoliaTable() {
        super();
    }
    
    @Override
    protected TableHead createTableHead() {
        return new MagnoliaTableHead();
    }
    
    @Override
    protected VScrollTableBody createScrollBody() {
        return new MagnoliaTableBody();
    }
    
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        
        if (BrowserInfo.get().isTouchDevice()) {
            addStyleName("mobile");
        }
    }

    @Override
    protected void setMultiSelectMode(int multiselectmode) {
        this.multiselectmode = multiselectmode;
    }
    
    public class MagnoliaTableBody extends VScrollTableBody {
        
        @Override
        protected VScrollTableRow createScrollTableRow(UIDL uidl, char[] aligns) {
            return new MagnoliaTableRow(uidl, aligns);
        }
        
        @Override
        protected VScrollTableRow createScrollTableRow() {
            return new MagnoliaTableRow();
        }
        
        @Override
        public int getColWidth(int columnIndex) {
            return super.getColWidth(columnIndex+1);
        }
        
        public class MagnoliaTableRow extends VScrollTableRow {
            private CheckBox selectionCheckBox = null;
            private ValueChangeHandler<Boolean> selectionCheckBoxValueChangeHandler = null;
            
            public MagnoliaTableRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
                privateConstruction();
            }

            public MagnoliaTableRow() {
                super();
                privateConstruction();
            }
            
            private void privateConstruction() {
                selectionCheckBox = new CheckBox();
                selectionCheckBox.setValue(selected, false);
                selectionCheckBoxValueChangeHandler = new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if (event.getSource() instanceof Widget) {
                            final Widget source = (Widget) event.getSource();
                            final Element targetTd = source.getElement().getParentElement().cast();
                            VScrollTableRow row = Util.findWidget(targetTd, null);
                            if (row != null) {
                                boolean wasSelected = row.isSelected();
                                row.toggleSelection();
                                setRowFocus(row);
                                /*
                                 * next possible range select must start on this row
                                 */
                                selectionRangeStart = row;
                                if (wasSelected) {
                                    removeRowFromUnsentSelectionRanges(row);
                                }
                                
                                sendSelectedRows(true);
                            }
                        }
                    }
                };
                final Element td = DOM.createTD();
                td.addClassName("v-table-cell-content");
                td.appendChild(selectionCheckBox.getElement());
                selectionCheckBox.addValueChangeHandler(selectionCheckBoxValueChangeHandler);
                selectionCheckBox.addStyleName("v-selection-cb");
                
//                rowElement.appendChild(td);
                rowElement.insertFirst(td);
                getChildren().add(selectionCheckBox);
//                getChildren().insert(selectionCheckBox, 0);
                
                VMagnoliaTable.this.adopt(selectionCheckBox);
                
                                 
                 final TouchDelegate delegate = new TouchDelegate(this);
                 delegate.addTouchHandler(new MultiTapRecognizer(delegate, 1, 2));
                 addHandler(new MultiTapHandler() {
                     @Override
                     public void onMultiTap(MultiTapEvent event) {
                         if (BrowserInfo.get().isTouchDevice()) {
                             final NativeEvent doubleClickEvent = 
                                     Document.get().createDblClickEvent(
                                             0, 
                                             event.getTouchStarts().get(0).get(0).getPageX(), 
                                             event.getTouchStarts().get(0).get(0).getPageY(), 
                                             event.getTouchStarts().get(0).get(0).getPageX(), 
                                             event.getTouchStarts().get(0).get(0).getPageY(), 
                                             false, 
                                             false, 
                                             false, 
                                             false);
                             getElement().dispatchEvent(doubleClickEvent);
                         }
                     }
                 }, MultiTapEvent.getType());                 
            }
            
            @Override
            protected void setCellWidth(int cellIx, int width) {
                final Element cell = DOM.getChild(getElement(), cellIx + 1);
                if (checkboxWidth < 0) {
                    checkboxWidth = ((Element) getElement().getChild(0)).getOffsetWidth();
                }
                cell.getFirstChildElement().getStyle().setPropertyPx("width", cellIx == 0 ? width - checkboxWidth : width);
                cell.getStyle().setPropertyPx("width", cellIx == 0 ? width - checkboxWidth : width);
            }
            
            @Override
            public void toggleSelection() {
                selected = !selected;
                selectionChanged = true;
                if (selected) {
                    selectedRowKeys.add(String.valueOf(rowKey));
                    addStyleName("v-selected");
                } else {
                    removeStyleName("v-selected");
                    selectedRowKeys.remove(String.valueOf(rowKey));
                }
                
                if(selectionCheckBox != null) {
                    selectionCheckBox.setValue(selected, false);
                }
                MagnoliaTableHead head = (MagnoliaTableHead)tHead;
                head.getSelectAllCB().setValue(selectedRowKeys.size() == scrollBody.renderedRows.size(), false);
            }
            
            @Override
            protected boolean handleClickEvent(Event event, Element targetTdOrTr,
                    boolean immediate) {
                if (!client.hasEventListeners(VMagnoliaTable.this,
                        ITEM_CLICK_EVENT_ID)) {
                    // Don't send an event if nobody is listening
                    return false;
                }

                // This row was clicked
                client.updateVariable(paintableId, "clickedKey", "" + rowKey,
                        false);

                if (getElement() == targetTdOrTr.getParentElement()) {
                    // A specific column was clicked
                    int childIndex = DOM.getChildIndex(getElement(),
                            targetTdOrTr);
                    String colKey = null;
                    colKey = tHead.getHeaderCell(childIndex).getColKey();
                    client.updateVariable(paintableId, "clickedColKey", colKey,
                            false);
                }

                MouseEventDetails details = new MouseEventDetails(event);

                client.updateVariable(paintableId, "clickEvent",
                        details.toString(), immediate);

                return true;
            }
            
            @Override
            protected void startRowDrag(Event event, final int type,
                    Element targetTdOrTr) {
                VTransferable transferable = new VTransferable();
                transferable.setDragSource(VMagnoliaTable.this);
                transferable.setData("itemId", "" + rowKey);
                NodeList<TableCellElement> cells = rowElement.getCells();
                for (int i = 0; i < cells.getLength(); i++) {
                    if (cells.getItem(i).isOrHasChild(targetTdOrTr)) {
                        HeaderCell headerCell = tHead.getHeaderCell(i);
                        transferable.setData("propertyId", headerCell.cid);
                        break;
                    }
                }

                VDragEvent ev = VDragAndDropManager.get().startDrag(
                        transferable, event, true);
                if (dragmode == DRAGMODE_MULTIROW && isMultiSelectModeAny()
                        && selectedRowKeys.contains("" + rowKey)) {
                    ev.createDragImage(
                            (Element) scrollBody.tBodyElement.cast(), true);
                    Element dragImage = ev.getDragImage();
                    int i = 0;
                    for (Iterator<Widget> iterator = scrollBody.iterator(); iterator
                            .hasNext();) {
                        VScrollTableRow next = (VScrollTableRow) iterator
                                .next();
                        Element child = (Element) dragImage.getChild(i++);
                        if (!selectedRowKeys.contains("" + next.rowKey)) {
                            child.getStyle().setVisibility(Visibility.HIDDEN);
                        }
                    }
                } else {
                    ev.createDragImage(getElement(), true);
                }
                if (type == Event.ONMOUSEDOWN) {
                    event.preventDefault();
                }
                event.stopPropagation();
            }
        }
        
        @Override
        protected void detectExtrawidth() {
            NodeList<TableRowElement> rows = tBodyElement.getRows();
            if (rows.getLength() == 0) {
                /* need to temporary add empty row and detect */
                VScrollTableRow scrollTableRow = createScrollTableRow();
                tBodyElement.appendChild(scrollTableRow.getElement());
                detectExtrawidth();
                tBodyElement.removeChild(scrollTableRow.getElement());
            } else {
                boolean noCells = false;
                TableRowElement item = rows.getItem(0);
                TableCellElement firstTD = item.getCells().getItem(1);
                if (firstTD == null) {
                    // content is currently empty, we need to add a fake cell
                    // for measuring
                    noCells = true;
                    VScrollTableRow next = (VScrollTableRow) iterator().next();
                    boolean sorted = tHead.getHeaderCell(0) != null ? tHead
                            .getHeaderCell(0).isSorted() : false;
                    next.addCell(null, "", ALIGN_LEFT, "", true, sorted);
                    firstTD = item.getCells().getItem(0);
                }
                com.google.gwt.dom.client.Element wrapper = firstTD
                        .getFirstChildElement();
                cellExtraWidth = firstTD.getOffsetWidth()
                        - wrapper.getOffsetWidth();
                if (noCells) {
                    firstTD.getParentElement().removeChild(firstTD);
                }
            }
        }
    }
    
    public class MagnoliaTableHead extends TableHead {
        private CheckBox selectAll = null;
        
        public MagnoliaTableHead() {
            super();
            selectAll = new CheckBox();
            div.appendChild(selectAll.getElement());
            getChildren().add(selectAll);
            adopt(selectAll);
            selectAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    client.updateVariable(paintableId, "selectAll", event.getValue(), true);
                }
            });
            selectAll.addStyleName("v-select-all"); 
        }
        
        public CheckBox getSelectAllCB() {
            return selectAll;
        }
    }
}
