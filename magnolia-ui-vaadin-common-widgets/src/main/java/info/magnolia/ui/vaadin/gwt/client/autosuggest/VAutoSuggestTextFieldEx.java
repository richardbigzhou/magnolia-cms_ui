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
import info.magnolia.ui.vaadin.gwt.client.autosuggest.VAutoSuggestPopupEx.ClickItemHandler;

import java.util.List;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.vaadin.client.ui.VTextField;

/**
 * An extension of {@link VTextField} that displays a drop-down of suggestions for possible values.
 */
public class VAutoSuggestTextFieldEx extends VTextField {

    private AutoSuggestTextFieldState state = null;

    private VAutoSuggestPopupEx popup;

    private InputTimer inputTimer;

    public VAutoSuggestTextFieldEx() {
        super();
        inputTimer = new InputTimer();
        this.sinkEvents(Event.ONKEYDOWN | Event.ONCLICK | Event.ONDBLCLICK | Event.MOUSEEVENTS);
    }

    public VAutoSuggestTextFieldEx(Element node) {
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

    @Override
    public void onFocus(FocusEvent event) {
        super.onFocus(event);
        if (state.suggestionsAvailable) {
            popup.filter();
            popup.setContentWidth(this.getOffsetWidth());
            popup.showRelativeTo(VAutoSuggestTextFieldEx.this);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        popup = new VAutoSuggestPopupEx(this);
        popup.addAutoHidePartner(this.getElement());
        popup.setClickItemHandler(new ClickItemHandler() {

            @Override
            public void onClickItem(String selectedText) {
                onClickOnSelectionInPopupHandler(selectedText);
            }
        });

    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (state.suggestionsAvailable) {
            popup.removeFromParent();
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONKEYDOWN) {
            int keyCode = DOM.eventGetKeyCode(event);
            if (keyCode == KeyCodes.KEY_UP) {
                selectedPrevious();
                event.preventDefault();
            } else if (keyCode == KeyCodes.KEY_DOWN) {
                selectedNext();
                event.preventDefault();
            } else if (keyCode == KeyCodes.KEY_ENTER) {
                enterValue();
                event.stopPropagation();
            } else if (keyCode == KeyCodes.KEY_TAB) {
                onTabKeyHandler(event);
            } else {
                onInputKey();
            }

        }
        super.onBrowserEvent(event);
    }

    private void onInputKey() {
        inputTimer.execute(new Executor() {
            @Override
            public void execute() {
                if (state.suggestionsAvailable) {
                    popup.filter();
                }
            }
        });
    }

    private void selectedPrevious() {
        if (state.suggestionsAvailable) {
            popup.selectedPrevious();
            popup.scrollToSelected();
        }
    }

    private void selectedNext() {
        if (state.suggestionsAvailable) {
            popup.selectedNext();
            popup.scrollToSelected();
        }
    }

    private void enterValue() {
        String selectedText = popup.getSelectedText();
        if (state.suggestionsAvailable && selectedText != null) {
            if (selectedText != null) {
                this.setValue(selectedText);
            }

        }
        popup.hide(true);
        blur(this.getElement());
    }

    private void onTabKeyHandler(Event event) {
        popup.tryComplete();
        event.preventDefault();
        event.stopPropagation();
        popup.filter();
    }

    private void onClickOnSelectionInPopupHandler(String selectedText) {
        setValue(selectedText, true);
        valueChange(true);
        popup.hide(true);
    }

    private native void blur(Element el) /*-{
                                          if (el.blur) {
                                          el.blur();
                                          }
                                          }-*/;
}
