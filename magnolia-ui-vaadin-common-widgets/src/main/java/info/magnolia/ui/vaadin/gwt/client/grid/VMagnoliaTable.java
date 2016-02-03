/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapRecognizer;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.UIDL;
import com.vaadin.client.Util;
import com.vaadin.client.ui.VScrollTablePatched;

/**
 * Magnolia table extends VScrollTable in a way that out-of-the-box version of it would not allow.
 * Therefore maven build will patch the VScrollTable to reveal it's private members.
 */
public class VMagnoliaTable extends VScrollTablePatched {

    static int checkboxWidth = -1;

    public VMagnoliaTable() {
        super();
        MagnoliaTableHead head = (MagnoliaTableHead) tHead;
        head.addToDom();
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
    protected HeaderCell createHeaderCell(String colId, String headerText) {
        return new MagnoliaHeaderCell(colId, headerText);
    }

    @Override
    protected void setMultiSelectMode(int multiselectmode) {
        this.multiselectmode = multiselectmode;
    }

    /**
     * Extend header cell to contain caption text.
     */
    public class MagnoliaHeaderCell extends HeaderCell {

        private Element caption = null;
        private boolean canBeSorted = false;

        public MagnoliaHeaderCell(String colId, String headerText) {
            super(colId, headerText);
            caption = DOM.createSpan();
            captionContainer.appendChild(caption);
            setText(headerText);
        }

        @Override
        public void setText(String headerText) {
            if (caption != null) {
                caption.setInnerHTML(headerText);
            }
        }

        @Override
        protected void setSorted(boolean sorted) {
            this.canBeSorted = true;
            super.setSorted(sorted);
        }

        @Override
        protected void updateStyleNames(String primaryStyleName) {
            super.updateStyleNames(primaryStyleName);
            if (this.canBeSorted) {
                addStyleName("sortable");
            }
        }
    }

    /**
     * Extend TableHead to contain select all checkbox.
     */
    public class MagnoliaTableHead extends TableHead {

        private CheckBox selectAll = null;

        public MagnoliaTableHead() {
            super();
            selectAll = new CheckBox();
            selectAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    client.updateVariable(paintableId, "selectAll", event.getValue(), true);
                }
            });
            selectAll.addStyleName("v-select-all");
        }

        public void addToDom() {
            div.appendChild(selectAll.getElement());
            getChildren().add(selectAll);
            adopt(selectAll);
        }

        public CheckBox getSelectAllCB() {
            return selectAll;
        }
    }

    /**
     * Extension of VScrollTableBody.
     */
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
            return super.getColWidth(columnIndex);
        }

        /**
         * Extend VScrollTableRow to contain selection checkbox.
         */
        public class MagnoliaTableRow extends VScrollTableRow {

            private CheckBox selectionCheckBox;

            private HTML selectionCheckBoxSpacer;

            private String nodeIcon;

            public MagnoliaTableRow(UIDL uidl, char[] aligns) {
                super(uidl, aligns);
            }

            public MagnoliaTableRow() {
                super();
            }

            @Override
            protected void updateStyleNames(String primaryStyleName) {
                // Minor hack. Use row style for icon definition.
                if (rowStyle != null) {
                    String[] rowStyles = rowStyle.split(" ");
                    for (String style : rowStyles) {
                        if (style.startsWith("icon")) {
                            if (nodeIcon == null) {
                                nodeIcon = style;
                            } else {
                                nodeIcon += " " + style;
                            }
                        }
                    }
                }
                super.updateStyleNames(primaryStyleName);
            }

            /**
             * Minor hack. Construction has to happen during base class construction and this method
             * is called by the base class constructor.
             */
            @Override
            protected void setElement(com.google.gwt.user.client.Element elem) {
                super.setElement(elem);
                privateConstruction();
            }

            @Override
            protected void initCellWithText(String text, char align, String style, boolean textIsHTML, boolean sorted, String description, final TableCellElement td) {
                super.initCellWithText(text, align, style, textIsHTML, sorted, description, td);
                if (td.equals(this.getElement().getFirstChildElement())) {
                    insertNodeIcon(td);
                    if (isSingleSelectMode()) {
                        insertSelectionCheckboxSpacer(td);
                    } else {
                        insertSelectionCheckbox(td);
                    }
                }
            }

            @Override
            protected void initCellWithWidget(Widget w, char align, String style, boolean sorted, TableCellElement td) {
                super.initCellWithWidget(w, align, style, sorted, td);
                if (td.equals(this.getElement().getFirstChildElement())) {
                    insertNodeIcon(td);
                    if (isSingleSelectMode()) {
                        insertSelectionCheckboxSpacer(td);
                    } else {
                        insertSelectionCheckbox(td);
                    }
                }
            }

            /**
             * Inserts the selection checkbox in first column.
             */
            private void insertSelectionCheckbox(final TableCellElement td) {
                com.google.gwt.dom.client.Element container = td.getFirstChildElement();
                container.insertFirst(selectionCheckBox.getElement());
            }

            /**
             * Inserts a spacer element in the first column to ensure that positioning of content is the same when there is or is not a checkbox.
             */
            private void insertSelectionCheckboxSpacer(final TableCellElement td) {
                com.google.gwt.dom.client.Element container = td.getFirstChildElement();
                container.insertFirst(selectionCheckBoxSpacer.getElement());
            }

            protected void insertNodeIcon(TableCellElement td) {
                if (nodeIcon != null) {
                    SpanElement iconElement = Document.get().createSpanElement();
                    iconElement.setClassName(nodeIcon);
                    iconElement.addClassName("v-table-icon-element");
                    td.getFirstChild().insertFirst(iconElement);
                }
            }

            private void privateConstruction() {
                selectionCheckBox = new CheckBox();
                selectionCheckBox.setValue(isSelected(), false);
                ValueChangeHandler<Boolean> selectionChangeHandler = new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if (event.getSource() instanceof Widget) {
                            final Widget source = (Widget) event.getSource();
                            final Element targetTd = source.getElement().getParentElement().cast();
                            VScrollTableRow row = Util.findWidget(targetTd, null);
                            if (row != null) {
                                boolean wasSelected = row.isSelected();

                                if (VMagnoliaTable.this.isSingleSelectMode() && !row.isSelected()) {
                                    deselectAll();
                                }

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

                selectionCheckBox.addValueChangeHandler(selectionChangeHandler);
                selectionCheckBox.addStyleName("v-selection-cb");
                getChildren().add(selectionCheckBox);
                VMagnoliaTable.this.adopt(selectionCheckBox);

                selectionCheckBoxSpacer = new HTML();
                selectionCheckBoxSpacer.addStyleName("v-selection-cb");
                getChildren().add(selectionCheckBoxSpacer);
                VMagnoliaTable.this.adopt(selectionCheckBoxSpacer);

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
            public void toggleSelection() {
                super.toggleSelection();

                if (selectionCheckBox != null) {
                    selectionCheckBox.setValue(isSelected(), false);
                }
                MagnoliaTableHead head = (MagnoliaTableHead) tHead;
                head.getSelectAllCB().setValue(selectedRowKeys.size() == scrollBody.renderedRows.size(), false);
            }

            @Override
            protected boolean isRenderHtmlInCells() {
                return true;
            }
        }

    }
}
