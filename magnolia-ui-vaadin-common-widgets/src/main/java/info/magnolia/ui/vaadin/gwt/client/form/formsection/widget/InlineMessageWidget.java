/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.form.formsection.widget;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.shape.Path;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Error/Help bubble widget.
 */
public abstract class InlineMessageWidget extends FlowPanel {

    protected Widget triangleSVG = createErrorDecoration();

    protected Element icon = DOM.createElement("i");

    protected Element messageWrapper = DOM.createDiv();

    protected Element messageEl = DOM.createSpan();

    protected InlineMessageWidget() {
        construct();
    }

    private void construct() {
        applyStyles();
        add(triangleSVG);
        getElement().appendChild(messageWrapper);
        messageWrapper.appendChild(icon);
        messageWrapper.appendChild(messageEl);
    }

    protected abstract void applyStyles();

    private static Widget createErrorDecoration() {
        final DrawingArea canvas = new DrawingArea(20, 10);
        canvas.clear();
        final Path path = new Path(0, 10);
        path.setStrokeColor(null);
        path.lineTo(10, 0);
        path.lineTo(20, 10);
        path.close();
        canvas.add(path);
        canvas.getElement().getStyle().setProperty("width", "");
        return canvas;
    }

    public void setMessage(final String message) {
        messageEl.setInnerHTML(message);
    }

    public static InlineMessageWidget createErrorMessage() {
        final InlineMessageWidget result = new InlineMessageWidget() {
            @Override
            protected void applyStyles() {
                addStyleName("validation-message-inline");
                icon.addClassName("icon-error-white");
                messageWrapper.addClassName("validation-message");
                triangleSVG.addStyleName("triangle red");
            }
        };
        return result;
    }

    public static InlineMessageWidget createHelpMessage() {
        final InlineMessageWidget result = new InlineMessageWidget() {
            @Override
            protected void applyStyles() {
                addStyleName("help-message-inline");
                icon.addClassName("action-dialog-help");
                messageWrapper.addClassName("help-message");
                triangleSVG.addStyleName("triangle blue");
            }
        };
        return result;
    }

    public void addMessage(String newMessage) {
        final String message = messageEl.getInnerHTML();
        final StringBuilder sb = new StringBuilder(message);
        if (!message.isEmpty()) {
            sb.append("<br/>");
        }
        sb.append(newMessage);
        messageEl.setInnerHTML(sb.toString());
    }
}
