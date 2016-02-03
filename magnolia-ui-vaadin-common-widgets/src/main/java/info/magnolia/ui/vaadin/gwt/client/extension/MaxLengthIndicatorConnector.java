/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.extension;

import info.magnolia.ui.vaadin.extension.MaxLengthIndicator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector for {@link MaxLengthIndicator}.
 */
@Connect(MaxLengthIndicator.class)
public class MaxLengthIndicatorConnector extends AbstractExtensionConnector {

    public static final String MAXLENGTH_INDICATOR_STYLE_NAME = "maxlength-indicator";

    private TextFieldConnector textConnector;
    private Widget textWidget;
    private Element indicatorElem = DOM.createDiv();
    private int maxLength = 0;
    private HandlerRegistration keyUpHandlerRegistration;
    private com.google.web.bindery.event.shared.HandlerRegistration parentStateChangeHandlerRegistration;


    @Override
    protected void extend(ServerConnector target) {
        textConnector = (TextFieldConnector) target;
        textWidget = textConnector.getWidget();

        addHandlers();
        updateIndicatorFromParentState();

        textWidget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    assembleAndAttach();
                }
            }
        });
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        cleanUp();
    }

    private void cleanUp() {
        if (keyUpHandlerRegistration != null) {
            keyUpHandlerRegistration.removeHandler();
        }
        if (parentStateChangeHandlerRegistration != null) {
            parentStateChangeHandlerRegistration.removeHandler();
        }
        if (indicatorElem.hasParentElement()) {
            indicatorElem.removeFromParent();
        }
    }

    private void assembleAndAttach() {
        // We append indicator element to the text widget's parent element
        indicatorElem.addClassName(MAXLENGTH_INDICATOR_STYLE_NAME);
        textWidget.getElement().getParentElement().appendChild(indicatorElem);
    }

    private void addHandlers() {
        // One might think KeyPressEvent should be the "better" choice here.
        // However, when using KeyPressEvent, text field's value is not updated
        // and hence the displayed number of entered characters is always 1 too small.
        keyUpHandlerRegistration = textWidget.addDomHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateIndicatorValue(getInputValueLength());
            }
        }, KeyUpEvent.getType());

        // make sure to update maxLength when the property gets changed
        parentStateChangeHandlerRegistration = textConnector.addStateChangeHandler(new StateChangeEvent.StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                if (stateChangeEvent.hasPropertyChanged("maxLength") || stateChangeEvent.hasPropertyChanged("text")) {
                    updateIndicatorFromParentState();
                }
            }
        });
    }

    private void updateIndicatorFromParentState() {
        maxLength = textConnector.getState().maxLength;
        String text = textConnector.getState().text;
        updateIndicatorValue(text == null ? 0 : text.length());
        setIndicatorVisible(maxLength >= 0);
    }

    private void setIndicatorVisible(boolean isVisible) {
        if (isVisible) {
            indicatorElem.getStyle().clearDisplay();
        } else {
            indicatorElem.getStyle().setDisplay(Style.Display.NONE);
        }
    }

    private void updateIndicatorValue(int currentlyDisplayedCharactersAmount) {
        indicatorElem.setInnerHTML(currentlyDisplayedCharactersAmount + "/" + maxLength);
    }

    private int getInputValueLength() {
        return ((String)((HasValue)textWidget).getValue()).length();
    }


}
