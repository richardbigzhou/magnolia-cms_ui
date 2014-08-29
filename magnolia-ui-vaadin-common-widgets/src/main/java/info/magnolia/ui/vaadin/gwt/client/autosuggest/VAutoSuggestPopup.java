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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.vaadin.client.BrowserInfo;

/**
 * StickPopPanel.
 */
public class VAutoSuggestPopup extends Composite {

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

    private VAutoSuggestToolTip selectedToolTip;
    private VAutoSuggestTextField refField;
    private Element relativeEl;

    private Grid grid;
    private ScrollPanel scroll;
    private FlexTable vp;

    private Element dragTop;
    private Element dragRight;
    private Element dragBottom;
    private Label dragCornerDown;
    private Label dragCornerUp;
    private Element dragEl;

    private int mouseLeft;
    private int mouseTop;

    private int topPosition;
    private int width;
    private int height;

    private int minimumWidth = 32;
    private int minimumHeihgt = 32;

    private List<String> filterList;
    private String selectedText = null;
    private int maxWidth = 0;

    private boolean focus;

    private boolean dropDown = true;

    private boolean resizing;

    private Element container = DOM.createDiv();
    private WrapSimplePanel wrapSimplePanel;
    private HandlerRegistration registration;

    /**
     * ClickItemHandler.
     */
    public interface ClickItemHandler {
        void onClickItem(String selectedText);
    }

    /**
     * ResizePopupHandler.
     */
    public interface ResizePopupHandler {

        void onResized();
    }

    /**
     * HoverHandler.
     */
    public interface HoverHandler {
        void onHover();
    }

    private ClickItemHandler clickItemHandler;

    private ResizePopupHandler resizePopupHandler;

    private HoverHandler hoverHandler;

    public void setClickItemHandler(ClickItemHandler clickItemHandler) {
        this.clickItemHandler = clickItemHandler;
    }

    public void setResizePopupHandler(ResizePopupHandler resizePopupHandler) {
        this.resizePopupHandler = resizePopupHandler;
    }

    public void setHoverHandler(HoverHandler hoverHandler) {
        this.hoverHandler = hoverHandler;
    }

    public VAutoSuggestPopup(Element relativeEl, VAutoSuggestTextField refField) {
        this.relativeEl = relativeEl;
        this.refField = refField;
        initLayout();
        initEvent();
    }

    private void initLayout() {
        selectedToolTip = new VAutoSuggestToolTip();
        selectedToolTip.setStyleName(CSS_SUGGEST_FULLLINE);
        selectedToolTip.getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        selectedToolTip.getElement().getStyle().setHeight(ROW_HEIGHT, Unit.PX);

        vp = new FlexTable();
        vp.setWidth("100%");
        vp.setCellSpacing(0);
        vp.setCellPadding(0);

        scroll = new ScrollPanel() {
            @Override
            public void onBrowserEvent(Event event) {
                super.onBrowserEvent(event);
                switch (DOM.eventGetType(event)) {
                case Event.ONMOUSEWHEEL:
                    if (event.getMouseWheelVelocityY() < 0) {
                        if (scroll.getVerticalScrollPosition() < Math.abs(event.getMouseWheelVelocityY())) {
                            scroll.setVerticalScrollPosition(0);
                            event.preventDefault();
                        }
                    } else if (event.getMouseWheelVelocityY() > 0) {
                        if (scroll.getMaximumVerticalScrollPosition() - scroll.getVerticalScrollPosition() < Math.abs(event.getMouseWheelVelocityY())) {
                            scroll.setVerticalScrollPosition(scroll.getMaximumVerticalScrollPosition());
                            event.preventDefault();
                        }
                    }
                    break;
                }
            }
        };
        scroll.sinkEvents(Event.ONMOUSEWHEEL);
        scroll.getElement().getStyle().setProperty("maxHeight", ROW_HEIGHT * DEFAULT_ROWS + Unit.PX.getType());
        scroll.setWidget(vp);
        getScrollElement(scroll).getStyle().setOverflow(Overflow.AUTO);

        grid = new Grid(3, 2);
        grid.setStyleName(CSS_SUGGEST_CONTAINER);
        grid.setBorderWidth(0);
        grid.setCellSpacing(0);
        grid.setCellPadding(0);

        grid.setWidget(1, 0, scroll);
        grid.getCellFormatter().getElement(1, 1).getStyle().setFontSize(0D, Unit.PX);

        dragCornerUp = new Label();
        dragCornerUp.getElement().setClassName(CSS_SUGGEST_DRAG_UP);
        dragCornerUp.setSize("12px", "12px");
        dragCornerUp.getElement().getStyle().setProperty("cursor", "nesw-resize");
        dragCornerUp.getElement().getStyle().setMarginTop(2D, Unit.PX);
        grid.setWidget(0, 0, dragCornerUp);
        grid.getCellFormatter().getElement(0, 1).getStyle().setFontSize(0D, Unit.PX);

        dragCornerDown = new Label();
        dragCornerDown.getElement().setClassName(CSS_SUGGEST_DRAG_DOWN);
        dragCornerDown.setSize("12px", "12px");
        dragCornerDown.getElement().getStyle().setProperty("cursor", "nwse-resize");
        dragCornerDown.getElement().getStyle().setMarginBottom(2D, Unit.PX);
        grid.setWidget(2, 0, dragCornerDown);
        grid.getCellFormatter().getElement(2, 1).getStyle().setFontSize(0D, Unit.PX);

        grid.getCellFormatter().getElement(0, 0).setAttribute("align", "right");
        grid.getCellFormatter().getElement(2, 0).setAttribute("align", "right");

        dragTop = grid.getCellFormatter().getElement(0, 0);
        dragTop.getStyle().setCursor(Cursor.ROW_RESIZE);
        dragRight = grid.getCellFormatter().getElement(1, 1);
        dragRight.getStyle().setCursor(Cursor.COL_RESIZE);
        dragRight.getStyle().setPadding(1D, Unit.PX);
        dragBottom = grid.getCellFormatter().getElement(2, 0);
        dragBottom.getStyle().setCursor(Cursor.ROW_RESIZE);
        this.initWidget(grid);
        setVisible(false);
        getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        getElement().getStyle().setPosition(Position.ABSOLUTE);
    }

    private void initEvent() {
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                tryShowingSelectedToolTip();
            }
        });

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
        sinkEvents(Event.MOUSEEVENTS);
    }

    private void showRelativeTo(int top, int left, boolean dropDown) {
        if (!this.isAttached()) {
            container.getStyle().setWidth(0D, Unit.PX);
            container.getStyle().setHeight(0D, Unit.PX);
            relativeEl.appendChild(container);
            wrapSimplePanel = WrapSimplePanel.wrap(container);
            wrapSimplePanel.add(this);
        }
        if (top >= 0) {
            this.getElement().getStyle().setTop(top, Unit.PX);
        }
        if (left >= 0) {
            this.getElement().getStyle().setLeft(left, Unit.PX);
        }
        if (dropDown) {
            grid.getRowFormatter().getElement(0).getStyle().setDisplay(Display.NONE);
            grid.getRowFormatter().getElement(2).getStyle().clearDisplay();
        } else {
            grid.getRowFormatter().getElement(2).getStyle().setDisplay(Display.NONE);
            grid.getRowFormatter().getElement(0).getStyle().clearDisplay();
        }
    }

    public void adjustPositionAndSize() {
        int[] topAndLeft = SuggestionUtil.getOffset(refField.getElement(), relativeEl);
        int residualWidth = relativeEl.getOffsetWidth() - topAndLeft[1] - 26;
        if (!scroll.getElement().getPropertyBoolean(MANUAL_RESIZE)) {
            if (isInFirstColumn()) {
                if (maxWidth > refField.getOffsetWidth() - topAndLeft[1]
                        && maxWidth < residualWidth) {
                    scroll.setWidth(maxWidth + 15 + Unit.PX.getType());
                } else if (maxWidth <= refField.getOffsetWidth() - topAndLeft[1]) {
                    scroll.setWidth(refField.getOffsetWidth() - topAndLeft[1] + Unit.PX.getType());
                } else {
                    scroll.setWidth(residualWidth + Unit.PX.getType());
                }
            } else {
                if (maxWidth > refField.getOffsetWidth() && maxWidth < residualWidth) {
                    scroll.setWidth(maxWidth + 15 + Unit.PX.getType());
                } else if (maxWidth <= refField.getOffsetWidth()) {
                    scroll.setWidth(refField.getOffsetWidth() + Unit.PX.getType());
                } else {
                    scroll.setWidth(residualWidth + Unit.PX.getType());
                }
            }
        }
        int popupHeight = (vp.getRowCount() >= DEFAULT_ROWS ? ROW_HEIGHT * DEFAULT_ROWS : ROW_HEIGHT * vp.getRowCount()) + 16;
        int bottomPosition = relativeEl.getScrollTop() + relativeEl.getOffsetHeight();
        int relativeTop;
        if (topAndLeft[0] + refField.getOffsetHeight() + popupHeight > bottomPosition
                && topAndLeft[0] - popupHeight < relativeEl.getScrollTop()) {
            dropDown = true;
            relativeTop = topAndLeft[0] + refField.getOffsetHeight();
        } else {
            if (dropDown) {
                if (topAndLeft[0] + refField.getOffsetHeight() + popupHeight < bottomPosition) {
                    dropDown = true;
                    relativeTop = topAndLeft[0] + refField.getOffsetHeight();
                } else {
                    dropDown = false;
                    relativeTop = topAndLeft[0] - popupHeight - (vp.getRowCount() < DEFAULT_ROWS ? SuggestionUtil.getScrollBarWidth(scroll.getElement(), true) : 0);
                }
            } else {
                if (topAndLeft[0] - popupHeight > relativeEl.getScrollTop()) {
                    dropDown = false;
                    relativeTop = topAndLeft[0] - popupHeight - (vp.getRowCount() < DEFAULT_ROWS ? SuggestionUtil.getScrollBarWidth(scroll.getElement(), true) : 0);
                } else {
                    dropDown = true;
                    relativeTop = topAndLeft[0] + refField.getOffsetHeight();
                }
            }
        }
        if (scroll.getElement().getPropertyBoolean(MANUAL_RESIZE)) {
            showRelativeTo(-1, topAndLeft[1], dropDown);
        } else {
            showRelativeTo(relativeTop, topAndLeft[1], dropDown);
        }
    }

    private boolean isInFirstColumn() {
        Element td = refField.getElement().getParentElement().getParentElement().cast();
        return td.getPreviousSibling() == null;
    }

    public void filter() {
        filterList = new ArrayList<String>();
        selectedToolTip.setVisible(false);
        vp.setVisible(false);
        vp.removeAllRows();
        int row = 0;

        maxWidth = 0;
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
                    int w = SuggestionUtil.getWidthInView(item.getHtml());
                    if (w > maxWidth) {
                        maxWidth = w;
                    }
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
                int w = SuggestionUtil.getWidthInView(item.getHtml());
                if (w > maxWidth) {
                    maxWidth = w;
                }
                rowFormatter.addStyleName(row, CSS_SUGGEST_ITEM);
                vp.getRowFormatter().getElement(row).getStyle().setHeight(ROW_HEIGHT, Unit.PX);
                mouseOn(rowFormatter.getElement(row));
                row++;
            }
            setSelected(getSelectedIndex());
        }
        vp.setVisible(true);
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
            if (focus) {
                vp.getRowFormatter().addStyleName(index, CSS_SELECTED_FOCUS);
            }
        } else {
            selectedText = null;
        }
    }

    public void focus() {
        focus = true;
        int selectedIndex = getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
            vp.getRowFormatter().addStyleName(selectedIndex, CSS_SELECTED_FOCUS);
        }
    }

    public void blur() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
            vp.getRowFormatter().removeStyleName(selectedIndex, CSS_SELECTED_FOCUS);
        }
        focus = false;
    }

    public boolean isResizing() {
        return resizing;
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

    public String getSelectedText() {
        return selectedText;
    }

    public void tryComplete() {
        String prefix = SuggestionUtil.getSamePrefix(filterList);
        boolean isLowerCase = refField.getText().equals(refField.getText().toLowerCase());
        if (prefix != null && prefix.length() > 0 && (isLowerCase ? prefix.toLowerCase().contains(refField.getText()) : prefix.contains(refField.getText()))) {
            refField.setText(prefix);
        }
    }

    public void tryShowingSelectedToolTip() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
            SpanLabel item = (SpanLabel) vp.getWidget(selectedIndex, 0);
            if (item.getOffsetWidth() > scroll.getOffsetWidth() - SuggestionUtil.getScrollBarWidth(scroll.getElement(), false) && selectedInView() && scroll.getHorizontalScrollPosition() == 0) {
                SpanLabel fullItem = SpanLabel.getAvailableInstance();
                fullItem.setHtml(item.getHtml());
                selectedToolTip.setWidget(fullItem);
                Element rowEl = vp.getRowFormatter().getElement(selectedIndex);
                int[] topAndLeft = SuggestionUtil.getOffset(rowEl, relativeEl);
                if (focus) {
                    selectedToolTip.addStyleName(CSS_SELECTED_FOCUS);
                } else {
                    selectedToolTip.removeStyleName(CSS_SELECTED_FOCUS);
                }
                selectedToolTip.showRelativeTo(relativeEl, topAndLeft[0], topAndLeft[1]);
                return;
            }
        }
        selectedToolTip.setVisible(false);
    }

    private boolean selectedInView() {
        int selectedIndex = getSelectedIndex();
        int selectedItemPosition = selectedIndex * ROW_HEIGHT;
        int scrollPosition = scroll.getVerticalScrollPosition();
        return selectedItemPosition >= scrollPosition && (selectedItemPosition + ROW_HEIGHT) <= scrollPosition + scroll.getOffsetHeight() - SuggestionUtil.getScrollBarWidth(scroll.getElement(), true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            int selectedIndex = getSelectedIndex();
            if (selectedIndex > -1 && selectedIndex < vp.getRowCount()) {
                vp.getRowFormatter().removeStyleName(selectedIndex, CSS_SELECTED);
            }
            selectedText = null;
            selectedToolTip.setVisible(false);
        }
    }

    private int getAvailableWidth(int w) {
        return w > minimumWidth ? w : minimumWidth;
    }

    private int getAvailableHeight(int h) {
        return h > minimumHeihgt ? h : minimumHeihgt;
    }

    @Override
    public void onBrowserEvent(Event event) {
        int eventType = DOM.eventGetType(event);

        switch (eventType) {
        case Event.ONMOUSEDOWN:
            Element el = (Element) Element.as(event.getEventTarget());
            if (el == dragTop || el == dragCornerUp.getElement()) {
                DOM.setCapture(this.getElement());
                dragEl = el;
                mouseLeft = event.getClientX();
                mouseTop = event.getClientY();
                topPosition = Integer.parseInt(this.getElement().getStyle().getTop().replaceFirst("(p|P)(x|X)$", ""));
                width = grid.getWidget(1, 0).getOffsetWidth();
                height = grid.getWidget(1, 0).getOffsetHeight();
                grid.getWidget(1, 0).setHeight(height + Unit.PX.getType());
                grid.getWidget(1, 0).getElement().getStyle().clearProperty("maxHeight");
                selectedToolTip.setVisible(false);
                if (BrowserInfo.get().isChrome()) {
                    getScrollElement(scroll).getStyle().setOverflow(Overflow.HIDDEN);
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            getScrollElement(scroll).getStyle().setOverflow(Overflow.AUTO);
                        }
                    });
                }
                DOM.eventPreventDefault(event);
            } else if (el == dragRight || el == dragBottom || el == dragCornerDown.getElement()) {
                DOM.setCapture(this.getElement());
                dragEl = el;
                mouseLeft = event.getClientX();
                mouseTop = event.getClientY();
                width = grid.getWidget(1, 0).getOffsetWidth();
                height = grid.getWidget(1, 0).getOffsetHeight();
                grid.getWidget(1, 0).setHeight(height + Unit.PX.getType());
                grid.getWidget(1, 0).getElement().getStyle().clearProperty("maxHeight");
                selectedToolTip.setVisible(false);
                if (BrowserInfo.get().isChrome()) {
                    getScrollElement(scroll).getStyle().setOverflow(Overflow.HIDDEN);
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            getScrollElement(scroll).getStyle().setOverflow(Overflow.AUTO);
                        }
                    });
                }
                DOM.eventPreventDefault(event);
            }
            break;
        case Event.ONMOUSEMOVE:
            if (dragEl == dragTop) {
                int dy = mouseTop - event.getClientY();
                if (dy > -height) {
                    if (getAvailableHeight(height + dy) > minimumHeihgt) {
                        this.getElement().getStyle().setTop(topPosition - dy, Unit.PX);
                        grid.getWidget(1, 0).setHeight((height + dy) + Unit.PX.getType());
                    }
                }
            }
            if (dragEl == dragCornerUp.getElement()) {
                int dx = event.getClientX() - mouseLeft;
                grid.getWidget(1, 0).setWidth(getAvailableWidth(width + dx) + Unit.PX.getType());

                int dy = mouseTop - event.getClientY();
                if (dy > -height) {
                    if (getAvailableHeight(height + dy) > minimumHeihgt) {
                        this.getElement().getStyle().setTop(topPosition - dy, Unit.PX);
                        grid.getWidget(1, 0).setHeight(getAvailableHeight(height + dy) + Unit.PX.getType());
                    }
                }
            }
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
            if (DOM.getCaptureElement() == this.getElement()) {
                resizing = true;
            }
            break;
        case Event.ONMOUSEUP:
            if (DOM.getCaptureElement() == this.getElement()) {
                DOM.releaseCapture(this.getElement());
                grid.getWidget(1, 0).getElement().setPropertyBoolean(MANUAL_RESIZE, true);
                tryShowingSelectedToolTip();
                dragEl = null;
                resizing = false;
                if (resizePopupHandler != null) {
                    resizePopupHandler.onResized();
                }
            }
            if (refField != null) {
                focus(refField.getElement());
            }
            break;
        }
        super.onBrowserEvent(event);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        registration = Event.addNativePreviewHandler(new NativePreviewHandler() {
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                Event nativeEvent = Event.as(event.getNativeEvent());
                switch (nativeEvent.getTypeInt()) {
                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    EventTarget target = nativeEvent.getEventTarget();
                    if (Element.is(target)) {
                        if (!getElement().isOrHasChild(Element.as(target))
                                && !refField.getElement().equals(Element.as(target))
                                && !Element.as(target).getClassName().contains(COLUMN_RESIZER)) {
                            setVisible(false);
                            return;
                        }
                    }
                    break;
                }
            }
        });
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (registration != null) {
            registration.removeHandler();
        }
        if (wrapSimplePanel != null) {
            wrapSimplePanel.removeFromParent();
        }
        container.removeFromParent();
        selectedToolTip.removeFromParent();
    }

    private native void focus(Element el) /*-{
                                          if (el.focus) {
                                          el.focus();
                                          }
                                          }-*/;

    private native Element getScrollElement(ScrollPanel scroll) /*-{
                                                                return scroll.@com.google.gwt.user.client.ui.ScrollPanel::getScrollableElement()();
                                                                }-*/;

    private native void mouseOn(Element el) /*-{
                                            var instance = this;
                                            el.onmouseenter = function(){
                                            instance.@info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopup::addClassName(Lcom/google/gwt/user/client/Element;Ljava/lang/String;)(this, "mouse-over");
                                            };
                                            el.onmouseleave = function(){
                                            instance.@info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopup::removeClassName(Lcom/google/gwt/user/client/Element;Ljava/lang/String;)(this, "mouse-over");
                                            };
                                            }-*/;

    void addClassName(Element el, String className) {
        el.addClassName(className);
        if (hoverHandler != null) {
            hoverHandler.onHover();
        }
    }

    void removeClassName(Element el, String className) {
        el.removeClassName(className);
    }
}
