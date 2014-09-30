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

import info.magnolia.ui.vaadin.gwt.client.autosuggest.InputTimer.Executor;
import info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopup.ClickItemHandler;
import info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopup.HoverHandler;
import info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopup.ResizePopupHandler;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.vaadin.client.ui.VTextField;

/**
 * An extension of {@link VTextField} that displays a drop-down of suggestions for possible values.
 */
public class VAutoSuggestTextField extends VTextField {

    private static final String COLUMN_RESIZER = "v-table-resizer";

    private static final int S1 = 1;
    private static final int S2 = 2;
    private static final int S3 = 3;
    private static final int S4 = 4;

    private int curState;

    private AutoSuggestTextFieldState state = null;

    private Element relativeEl;

    private VAutoSuggestPopup popup;

    private HandlerRegistration registration;

    private HandlerRegistration resizeRegistration;

    private InputTimer inputTimer;

    public VAutoSuggestTextField() {
        super();
        inputTimer = new InputTimer();
        changeToS1();
        this.sinkEvents(Event.ONKEYDOWN | Event.ONCLICK | Event.ONDBLCLICK | Event.MOUSEEVENTS);
    }

    public VAutoSuggestTextField(Element node) {
        super(node);
    }

    public void setAutoSuggestTextFieldState(AutoSuggestTextFieldState state) {
        this.state = state;
    }

    public AutoSuggestTextFieldState getAutoSuggestTextFieldState() {
        return state;
    }

    public List<String> getSuggestion() {
        return state.suggestions;
    }

    public boolean isStartsWith() {
        return state.matchMethod == AutoSuggestTextFieldState.STARTS_WITH;
    }

    private void initLayout() {
        initRelativeEl();
        popup = new VAutoSuggestPopup(relativeEl, this);
    }

    /**
     * Get the element corresponding to the scroll panel for the tree view.
     */
    private void initRelativeEl() {
        Element current = this.getElement();
        // QUESTION is there a better way to find the scroll container for the tree view?
        while (current != null) {
            if ("auto".equals(current.getStyle().getOverflow()) || "scroll".equals(current.getStyle().getOverflow())) {
                relativeEl = current;
                break;
            }
            current = (Element) current.getParentElement();
        }
        assert relativeEl != null : "relative el must exist";
    }

    private native void blur(Element el) /*-{
                                         if (el.blur) {
                                         el.blur();
                                         }
                                         }-*/;

    @Override
    protected void onLoad() {
        super.onLoad();
        initLayout();
        popup.setClickItemHandler(new ClickItemHandler() {

            @Override
            public void onClickItem(String selectedText) {
                onClickOnSelectionInPopupHandler(selectedText);
            }
        });
        popup.setResizePopupHandler(new ResizePopupHandler() {

            @Override
            public void onResized() {
                onDragMouseToResizePopup();
            }
        });
        popup.setHoverHandler(new HoverHandler() {

            @Override
            public void onHover() {
                onHoverMouseOverSuggestions();
            }
        });
        resizeRegistration = Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                onDragMouseToResizeWindow();
            }
        });
        registration = Event.addNativePreviewHandler(new NativePreviewHandler() {

            boolean resizer;

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                Event nativeEvent = Event.as(event.getNativeEvent());
                EventTarget target = nativeEvent.getEventTarget();
                if (Element.is(target)) {
                    switch (nativeEvent.getTypeInt()) {
                    case Event.ONMOUSEDOWN:
                        // If clicked outside of both text field and popup
                        resizer = Element.as(target).getClassName().contains(COLUMN_RESIZER);
                        if (!Element.as(target).equals(VAutoSuggestTextField.this.getElement())
                                && !popup.getElement().isOrHasChild(Element.as(target))
                                && !Element.as(target).getClassName().contains(COLUMN_RESIZER)) {
                            onClickOutsideFieldAndPopupHandler();
                            registration.removeHandler();
                        }
                        break;
                    case Event.ONMOUSEMOVE:
                        if (resizer) {
                            onResizeColumnHandler();
                        }
                        break;
                    case Event.ONMOUSEUP:
                        if (resizer) {
                            onResizeColumnHandler();
                            resizer = false;
                        }
                        break;
                    }
                }
            }
        });
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                s1OnInit();
            }
        });
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (popup.isAttached()) {
            popup.removeFromParent();
        }
        resizeRegistration.removeHandler();
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONBLUR) {
            if (popup.isVisible()) {
                event.preventDefault();
                return;
            }
        }
        if (DOM.eventGetType(event) == Event.ONKEYDOWN) {
            int keyCode = DOM.eventGetKeyCode(event);
            if (keyCode == KeyCodes.KEY_UP) {
                onUpKeyHandler(event);
            } else if (keyCode == KeyCodes.KEY_DOWN) {
                onDownKeyHandler(event);
            } else if (keyCode == KeyCodes.KEY_TAB) {
                onTabKeyHandler(event);
            } else if (keyCode == KeyCodes.KEY_ENTER) {
                onReturnKeyHandler();
            } else if (keyCode == KeyCodes.KEY_ESCAPE) {
                onEscKeyHandler();
            } else if (keyCode == KeyCodes.KEY_LEFT || keyCode == KeyCodes.KEY_RIGHT) {
                onLeftRightKeyHandler();
            } else {
                onInputKeyHandler();
            }
        }
        if (DOM.eventGetType(event) == Event.ONCLICK) {
            onClickHandler();
        }
        if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
            onDoubleClickHandler();
        }
        if (DOM.eventGetType(event) == Event.ONMOUSEUP) {
            if (this.getSelectionLength() > 0) {
                onDragMouseToSelectTextInTextField();
            }
        }
        super.onBrowserEvent(event);
    }

    private void onLeftRightKeyHandler() {
        switch (curState) {
        case S3:
            s3OnLeftRightKey();
            break;
        case S4:
            s4OnLeftRightKey();
            break;
        }
    }

    private void onInputKeyHandler() {
        switch (curState) {
        case S2:
            s2OnInputKey();
            break;
        case S3:
            s3OnInputKey();
            break;
        case S4:
            s4OnInputKey();
            break;
        }
    }

    private void onEscKeyHandler() {
        switch (curState) {
        case S2:
            s2OnEscKey();
            break;
        case S3:
            s3OnEscKey();
            break;
        case S4:
            s4OnEskKey();
            break;

        }
    }

    private void onReturnKeyHandler() {
        switch (curState) {
        case S2:
            s2OnReturnKey();
            break;
        case S3:
            s3OnReturnKey();
            break;
        case S4:
            s4OnReturnKey();
            break;
        }
    }

    private void onTabKeyHandler(Event event) {
        switch (curState) {
        case S2:
            s2OnTabKey();
            break;
        case S3:
            s3OnTabKey(event);
            break;
        case S4:
            s4OnTabKey(event);
            break;
        }
    }

    private void onDownKeyHandler(Event event) {
        switch (curState) {
        case S2:
            s2OnDownKey();
            break;
        case S3:
            s3OnDownKey(event);
            break;
        case S4:
            s4OnDownKey(event);
            break;
        }
    }

    private void onUpKeyHandler(Event event) {
        switch (curState) {
        case S2:
            s2OnUpKey();
            break;
        case S3:
            s3OnUpKey(event);
            break;
        case S4:
            s4OnUpKey(event);
            break;
        }
    }

    private void onDoubleClickHandler() {
        switch (curState) {
        case S2:
            s2OnDoubleClick();
            break;
        case S3:
            s3OnDoubleClick();
            break;
        case S4:
            s4OnDoubleClick();
            break;
        }
    }

    private void onClickOnSelectionInPopupHandler(String selectedText) {
        switch (curState) {
        case S3:
            s3OnClickOnSelectionInPopup(selectedText);
            break;
        case S4:
            s4OnClickOnSelectionInPopup(selectedText);
            break;
        }
    }

    private void onClickHandler() {
        switch (curState) {
        case S2:
            s2OnClick();
            break;
        case S3:
            s3OnClick();
            break;
        case S4:
            s4OnClick();
            break;

        }
    }

    private void onClickOutsideFieldAndPopupHandler() {
        switch (curState) {
        case S2:
            s2OnClickOutSideFieldAndPopup();
            break;
        case S3:
            s3OnClickOutsideFieldAndPopup();
            break;
        case S4:
            s4OnClickOutsideFieldAndPopup();
            break;
        }
    }

    private void onHoverMouseOverSuggestions() {
        switch (curState) {
        case S3:
            s3OnHoverMouseOverSuggestions();
            break;
        case S4:
            s4OnHoverMouseOverSuggestions();
            break;
        }
    }

    private void onDragMouseToResizePopup() {
        switch (curState) {
        case S3:
            s3OnDragMouseToResizePopup();
            break;
        case S4:
            s4OnDragMouseToResizePopup();
            break;
        }
    }

    private void onDragMouseToResizeWindow() {
        switch (curState) {
        case S3:
            s3OnDragMouseToResizeWindow();
            break;
        case S4:
            s4OnDragMouseToResizeWindow();
            break;
        }
    }

    private void onDragMouseToSelectTextInTextFieldHandler() {
        switch (curState) {
        case S2:
            s2OnDragMouseToSelectTextInTextField();
            break;
        case S3:
            s3OnDragMouseToSelectTextInTextField();
            break;
        case S4:
            s4OnDragMouseToSelectTextInTextField();
            break;
        }
    }

    private void onDragMouseToResizeWindowHandler() {
        switch (curState) {
        case S3:
            s3OnDragMouseToResizeWindow();
            break;
        case S4:
            s4OnDragMouseToResizeWindow();
            break;
        }
    }

    private void onDragMouseToResizePopupHandler() {
        switch (curState) {
        case S3:
            s3OnDragMouseToResizePopup();
            break;
        case S4:
            s4OnDragMouseToResizePopup();
            break;
        }
    }

    private void onDragMouseToSelectTextInTextField() {
        switch (curState) {
        case S2:
            s2OnDragMouseToSelectTextInTextField();
            break;
        case S3:
            s3OnDragMouseToSelectTextInTextField();
            break;
        case S4:
            s4OnDragMouseToSelectTextInTextField();
            break;
        }
    }

    private void onResizeColumnHandler() {
        switch (curState) {
        case S3:
            s3OnResizeColumn();
            break;
        case S4:
            s4OnResizeColumn();
            break;
        }
    }

    private void s1OnInit() {
        if (state.suggestionsAvailable) {
            this.selectAll();
            popup.filter();
            changeToS3();
        } else {
            this.selectAll();
            changeToS2();
        }
    }

    private void s2OnInputKey() {
        changeToS2();
    }

    private void s2OnEscKey() {
        // default behavior is right;
        changeToS1();
    }

    private void s2OnReturnKey() {
        // default behavior is right;
        changeToS1();
    }

    private void s2OnUpKey() {
        // default behavior is right;
        changeToS2();
    }

    private void s2OnDownKey() {
        // default behavior is right;
        changeToS2();
    }

    private void s2OnTabKey() {
        // default behavior is right;
        changeToS1();
    }

    private void s2OnDoubleClick() {
        // default behavior is right;
        changeToS2();
    }

    private void s2OnClick() {
        // default behavior is right;
        changeToS2();
    }

    private void s2OnClickOutSideFieldAndPopup() {
        // default behavior is right;
        changeToS1();
    }

    private void s2OnDragMouseToSelectTextInTextField() {
        // default behavior is right;
        changeToS2();
    }

    private void s4OnLeftRightKey() {
        changeToS3();
    }

    private void s4OnInputKey() {
        inputTimer.execute(new Executor() {

            @Override
            public void execute() {
                popup.filter();
                changeToS3();
            }
        });
    }

    private void s4OnEskKey() {
        // default behavior is right;
        changeToS1();
    }

    private void s4OnReturnKey() {
        String selectedText = popup.getSelectedText();
        if (selectedText != null) {
            this.setText(selectedText);
        }
        changeToS1();
    }

    private void s4OnUpKey(Event event) {
        changeToS4();
        popup.selectedPrevious();
        popup.scrollToSelected();
        popup.tryShowingSelectedToolTip();
        event.preventDefault();
    }

    private void s4OnDownKey(Event event) {
        changeToS4();
        popup.selectedNext();
        popup.scrollToSelected();
        popup.tryShowingSelectedToolTip();
        event.preventDefault();
    }

    private void s4OnTabKey(Event event) {
        popup.tryComplete();
        event.preventDefault();
        event.stopPropagation();
        popup.filter();
        changeToS3();
    }

    private void s4OnDoubleClick() {
        changeToS3();
    }

    private void s4OnClickOnSelectionInPopup(String selectedText) {
        setText(selectedText);
        popup.setVisible(false);
        blur(this.getElement());
        changeToS1();
    }

    private void s4OnClick() {
        changeToS3();
    }

    private void s4OnClickOutsideFieldAndPopup() {
        // default behavior is right;
        changeToS1();
    }

    private void s4OnHoverMouseOverSuggestions() {
        changeToS4();
    }

    private void s4OnDragMouseToResizePopup() {

        changeToS4();
    }

    private void s4OnDragMouseToSelectTextInTextField() {

        changeToS3();
    }

    private void s4OnDragMouseToResizeWindow() {
        changeToS4();
    }

    private void s4OnResizeColumn() {
        changeToS4();
    }

    private void s3OnLeftRightKey() {
        changeToS3();
    }

    private void s3OnInputKey() {
        inputTimer.execute(new Executor() {

            @Override
            public void execute() {
                popup.filter();
                changeToS3();
            }
        });
    }

    private void s3OnEscKey() {
        // default behavior is right;
        changeToS1();
    }

    private void s3OnReturnKey() {
        // Default behavior
        changeToS1();
    }

    private void s3OnUpKey(Event event) {
        changeToS4();
        popup.selectedPrevious();
        popup.scrollToSelected();
        popup.tryShowingSelectedToolTip();
        event.preventDefault();
    }

    private void s3OnDownKey(Event event) {
        changeToS4();
        popup.selectedNext();
        popup.scrollToSelected();
        popup.tryShowingSelectedToolTip();
        event.preventDefault();
    }

    private void s3OnTabKey(Event event) {
        popup.tryComplete();
        event.preventDefault();
        event.stopPropagation();
        popup.filter();
        changeToS3();
    }

    private void s3OnDoubleClick() {
        changeToS3();
    }

    private void s3OnClickOnSelectionInPopup(String selectedText) {
        setText(selectedText);
        popup.setVisible(false);
        blur(this.getElement());
        changeToS1();
    }

    private void s3OnClick() {
        changeToS3();
    }

    private void s3OnClickOutsideFieldAndPopup() {
        // default behavior is right;
        changeToS1();
    }

    private void s3OnHoverMouseOverSuggestions() {
        changeToS3();
    }

    private void s3OnDragMouseToResizePopup() {
        changeToS3();
    }

    private void s3OnDragMouseToSelectTextInTextField() {
        // default behavior is right;
        changeToS3();
    }

    private void s3OnDragMouseToResizeWindow() {
        changeToS3();
    }

    private void s3OnResizeColumn() {
        changeToS3();
    }

    private void changeToS1() {
        curState = S1;
    }

    private void changeToS2() {
        curState = S2;
    }

    private void changeToS3() {
        curState = S3;
        popup.blur();
        popup.setVisible(true);
        popup.adjustPositionAndSize();
        popup.tryShowingSelectedToolTip();
    }

    private void changeToS4() {
        curState = S4;
        popup.focus();
        popup.setVisible(true);
        popup.adjustPositionAndSize();
        popup.tryShowingSelectedToolTip();
    }

}
