/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.autosuggest;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * StickPopPanel.
 */
public class VAutoSuggestPopupEx extends PopupPanel {

    private static final String CSS_SUGGEST_CONTAINER = "suggest-container";
    private static final String CSS_SUGGEST_DRAG_UP = "suggest-drag-up";
    private static final String CSS_SUGGEST_DRAG_DOWN = "suggest-drag-down";
    private static final String CSS_SUGGEST_FULLLINE = "suggest-fullline";
    private static final String CSS_SUGGEST_ITEM = "suggest-item";
    private static final String CSS_SELECTED = "selected";
    private static final String CSS_SELECTED_FOCUS = "selected-focus";

    private static final String COLUMN_RESIZER = "v-table-resizer";
    private static final String MANUAL_RESIZE = "manual-resize";

    private static final int ROW_HEIGHT = 20;
    private static final int DEFAULT_ROWS = 10;

    private VAutoSuggestTextFieldEx refField;

    private Grid grid;
    private ScrollPanel scroll;
    private FlexTable vp;

    private Label dragCornerDown;
    private Element dragRight;
    private Element dragBottom;
    private Element dragEl;

    private int mouseLeft;
    private int mouseTop;

    private int width;
    private int height;

    private int topPosition;

    private int minimumWidth = 32;
    private int minimumHeihgt = 32;

    private List<String> filterList;
    private String selectedText = null;

    /**
     * ClickItemHandler.
     */
    public interface ClickItemHandler {
        void onClickItem(String selectedText);
    }

    /**
     * HoverHandler.
     */
    public interface HoverHandler {
        void onHover();
    }

    private ClickItemHandler clickItemHandler;

    private HoverHandler hoverHandler;

    public void setClickItemHandler(ClickItemHandler clickItemHandler) {
        this.clickItemHandler = clickItemHandler;
    }

    public void setHoverHandler(HoverHandler hoverHandler) {
        this.hoverHandler = hoverHandler;
    }

    public VAutoSuggestPopupEx(VAutoSuggestTextFieldEx refField) {
        super(true, false);
        this.refField = refField;
        initLayout();
        initEvent();
    }

    public void setContentWidth(int width) {
        grid.getWidget(1, 0).setWidth(width + "px");
    }

    private void initLayout() {

        vp = new FlexTable();
        vp.setWidth("100%");
        vp.setCellSpacing(0);
        vp.setCellPadding(0);

        scroll = new ScrollPanel();
        scroll.sinkEvents(Event.ONMOUSEWHEEL);
        scroll.getElement().getStyle().setProperty("maxHeight", ROW_HEIGHT * DEFAULT_ROWS + Unit.PX.getType());
        scroll.setWidget(vp);

        grid = new Grid(3, 2);
        grid.setStyleName(CSS_SUGGEST_CONTAINER);
        grid.setBorderWidth(0);
        grid.setCellSpacing(0);
        grid.setCellPadding(0);
        grid.setWidget(1, 0, scroll);
        grid.getRowFormatter().getElement(0).getStyle().setDisplay(Display.NONE);
        grid.getCellFormatter().getElement(1, 1).getStyle().setFontSize(0D, Unit.PX);

        dragCornerDown = new Label();
        dragCornerDown.getElement().setClassName(CSS_SUGGEST_DRAG_DOWN);
        dragCornerDown.setSize("12px", "12px");
        dragCornerDown.getElement().getStyle().setProperty("cursor", "nwse-resize");
        dragCornerDown.getElement().getStyle().setMarginBottom(2D, Unit.PX);
        grid.setWidget(2, 0, dragCornerDown);
        grid.getCellFormatter().getElement(2, 1).getStyle().setFontSize(0D, Unit.PX);

        grid.getCellFormatter().getElement(2, 0).setAttribute("align", "right");

        dragRight = grid.getCellFormatter().getElement(1, 1);
        dragRight.getStyle().setCursor(Cursor.COL_RESIZE);
        dragRight.getStyle().setPadding(1D, Unit.PX);
        dragBottom = grid.getCellFormatter().getElement(2, 0);
        dragBottom.getStyle().setCursor(Cursor.ROW_RESIZE);

        this.setWidget(grid);
        setVisible(false);
        getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        getElement().getStyle().setPosition(Position.ABSOLUTE);
    }

    private void initEvent() {
        sinkEvents(Event.MOUSEEVENTS);

        vp.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Cell cell = vp.getCellForEvent(event);
                if (cell != null) {
                    setSelected(cell.getRowIndex());
                    if (clickItemHandler != null) {
                        clickItemHandler.onClickItem(selectedText);
                    }
                }
            }
        });
    }

    public void filter() {
        filterList = new ArrayList<String>();
        vp.setVisible(false);
        vp.removeAllRows();
        int row = 0;
        selectedText = null;
        if (refField.getText() != null) {
            RowFormatter rowFormatter = vp.getRowFormatter();
            String fieldText = refField.getText();
            String lowerCaseFieldText = fieldText.toLowerCase();
            boolean isLowerCase = fieldText.equals(lowerCaseFieldText);
            boolean isStartWith = refField.isStartsWith();

            List<String> residueResults = new ArrayList<String>();
            boolean showMismatchedSuggestions = refField.getAutoSuggestTextFieldState().showMismatchedSuggestions;

            for (String s : refField.getSuggestion()) {
                if ((isStartWith ?
                        (isLowerCase ? s.toLowerCase().startsWith(lowerCaseFieldText) : s.startsWith(fieldText)) :
                        (isLowerCase ? s.toLowerCase().contains(lowerCaseFieldText) : s.contains(fieldText)))) {
                    SpanLabel item = SpanLabel.getAvailableInstance();
                    item.setWordWrap(false);
                    item.setText(s, fieldText, isStartWith);
                    vp.setWidget(row, 0, item);
                    rowFormatter.addStyleName(row, CSS_SUGGEST_ITEM);
                    vp.getRowFormatter().getElement(row).getStyle().setHeight(ROW_HEIGHT, Unit.PX);
                    mouseOn(rowFormatter.getElement(row));
                    row++;
                    filterList.add(s);
                } else if (showMismatchedSuggestions) {
                    residueResults.add(s);
                }
            }

            for (String suggestItem : residueResults) {
                SpanLabel item = SpanLabel.getAvailableInstance();
                item.setWordWrap(false);
                item.setText(suggestItem);
                vp.setWidget(row, 0, item);
                rowFormatter.addStyleName(row, CSS_SUGGEST_ITEM);
                vp.getRowFormatter().getElement(row).getStyle().setHeight(ROW_HEIGHT, Unit.PX);
                mouseOn(rowFormatter.getElement(row));
                row++;
            }
        }
        vp.setVisible(true);
    }

    public void scrollToSelected() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
            int scrollPosition = scroll.getVerticalScrollPosition();
            int endPosition = scrollPosition + scroll.getOffsetHeight();
            int selectedPosition = ROW_HEIGHT * selectedIndex;
            if (selectedPosition < scrollPosition) {
                scroll.setVerticalScrollPosition(selectedPosition);
            }
            if (selectedPosition + ROW_HEIGHT >= endPosition - SuggestionUtil.getScrollBarWidth(scroll.getElement(), true)) {
                scroll.setVerticalScrollPosition(scrollPosition + (selectedPosition + ROW_HEIGHT - endPosition) + SuggestionUtil.getScrollBarWidth(scroll.getElement(), true));
            }
        }
    }

    private int getSelectedIndex() {
        for (int row = 0; row < vp.getRowCount(); row++) {
            SpanLabel item = (SpanLabel) vp.getWidget(row, 0);
            if (item.getText().equals(selectedText)) {
                return row;
            }
        }
        return -1;
    }

    public void selectedNext() {
        int selectedIndex = getSelectedIndex();
        int nextIndex = (selectedIndex + 1) % vp.getRowCount();
        setSelected(nextIndex);
    }

    public void selectedPrevious() {
        int selectedIndex = getSelectedIndex();
        int previousIndex = selectedIndex - 1;
        previousIndex = previousIndex < 0 ? vp.getRowCount() - 1 : previousIndex;
        setSelected(previousIndex);
    }

    private void setSelected(int index) {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
            vp.getRowFormatter().removeStyleName(selectedIndex, CSS_SELECTED);
            vp.getRowFormatter().removeStyleName(selectedIndex, CSS_SELECTED_FOCUS);
        }
        if (index > -1 && index < vp.getRowCount()) {
            selectedText = ((SpanLabel) vp.getWidget(index, 0)).getText();
            vp.getRowFormatter().addStyleName(index, CSS_SELECTED);
            vp.getRowFormatter().addStyleName(index, CSS_SELECTED_FOCUS);
        } else {
            selectedText = null;
        }
    }

    public void tryComplete() {
        String prefix = SuggestionUtil.getSamePrefix(filterList);
        boolean isLowerCase = refField.getText().equals(refField.getText().toLowerCase());
        if (prefix != null && prefix.length() > 0 && (isLowerCase ? prefix.toLowerCase().contains(refField.getText()) : prefix.contains(refField.getText()))) {
            refField.setText(prefix);
        }
    }

    public String getSelectedText() {
        return selectedText;
    }

    void addClassName(Element el, String className) {
        el.addClassName(className);
        if (hoverHandler != null) {
            hoverHandler.onHover();
        }
    }

    void removeClassName(Element el, String className) {
        el.removeClassName(className);
    }

    @Override
    public void onBrowserEvent(Event event) {
        int eventType = DOM.eventGetType(event);

        switch (eventType) {
        case Event.ONMOUSEDOWN:
            Element el = (Element) Element.as(event.getEventTarget());
            if (el == dragRight || el == dragBottom || el == dragCornerDown.getElement()) {
                DOM.setCapture(this.getElement());
                dragEl = el;
                mouseLeft = event.getClientX();
                mouseTop = event.getClientY();
                width = grid.getWidget(1, 0).getOffsetWidth();
                height = grid.getWidget(1, 0).getOffsetHeight();
                grid.getWidget(1, 0).setHeight(height + Unit.PX.getType());
                grid.getWidget(1, 0).getElement().getStyle().clearProperty("maxHeight");
                DOM.eventPreventDefault(event);
            }
            break;
        case Event.ONMOUSEMOVE:

            if (dragEl == dragRight) {
                int dx = event.getClientX() - mouseLeft;
                grid.getWidget(1, 0).setWidth(getAvailableWidth(width + dx) + Unit.PX.getType());
                grid.getWidget(1, 0).setHeight(getAvailableHeight(height) + Unit.PX.getType());
            }
            if (dragEl == dragBottom) {
                int dy = event.getClientY() - mouseTop;
                grid.getWidget(1, 0).setHeight(getAvailableHeight(height + dy) + Unit.PX.getType());
            }
            if (dragEl == dragCornerDown.getElement()) {
                int dx = event.getClientX() - mouseLeft;
                int dy = event.getClientY() - mouseTop;
                grid.getWidget(1, 0).setWidth(getAvailableWidth(width + dx) + Unit.PX.getType());
                grid.getWidget(1, 0).setHeight(getAvailableHeight(height + dy) + Unit.PX.getType());
            }
            break;
        case Event.ONMOUSEUP:
            if (DOM.getCaptureElement() == this.getElement()) {
                DOM.releaseCapture(this.getElement());
                grid.getWidget(1, 0).getElement().setPropertyBoolean(MANUAL_RESIZE, true);
                dragEl = null;
            }
            break;
        }
        super.onBrowserEvent(event);
    }

    private int getAvailableWidth(int w) {
        return w > minimumWidth ? w : minimumWidth;
    }

    private int getAvailableHeight(int h) {
        return h > minimumHeihgt ? h : minimumHeihgt;
    }

    private native void mouseOn(Element el) /*-{
                                            var instance = this;
                                            el.onmouseenter = function(){
                                            instance.@info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopupEx::addClassName(Lcom/google/gwt/user/client/Element;Ljava/lang/String;)(this, "mouse-over");
                                            };
                                            el.onmouseleave = function(){
                                            instance.@info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopupEx::removeClassName(Lcom/google/gwt/user/client/Element;Ljava/lang/String;)(this, "mouse-over");
                                            };
                                            }-*/;

    private native void focus(Element el) /*-{
                                          if (el.focus) {
                                          el.focus();
                                          }
                                          }-*/;
}
