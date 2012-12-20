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
package info.magnolia.ui.vaadin.gwt.client.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * VDialogHeader.
 */
public class VDialogHeader extends FlowPanel {

    private static final String CLASSNAME_HEADER = "form-header";
    private static final String ClASSNAME_DESCRIPTION = "form-description";
    private static final String CLASSNAME_HELPBUTTON = "btn-form-help";
    private static final String CLASSNAME_CLOSEBUTTON = "btn-dialog-close";

    protected final VDialogHeaderCallback callback;

    private FlowPanel descriptionPanel = new FlowPanel();

    protected Element captionContainer = DOM.createDiv();

    private Element caption = DOM.createSpan();

    private boolean isDescriptionVisible = false;

    private final Button closeButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ((VDialogHeaderCallback)callback).onCloseFired();
        }
    });


    private final Button helpButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            isDescriptionVisible = !isDescriptionVisible;
            descriptionPanel.setVisible(isDescriptionVisible);
            callback.onDescriptionVisibilityChanged(isDescriptionVisible);
        }
    });


    public void construct() {
        captionContainer.addClassName(CLASSNAME_HEADER);

        closeButton.setStyleName(CLASSNAME_CLOSEBUTTON);
        closeButton.addStyleName("green");
        add(closeButton, captionContainer);

        descriptionPanel.addStyleName(ClASSNAME_DESCRIPTION);
        helpButton.setStyleName(CLASSNAME_HELPBUTTON);


        getElement().appendChild(captionContainer);
        captionContainer.appendChild(caption);

        descriptionPanel.setVisible(false);
        add(helpButton, captionContainer);
        add(descriptionPanel);
    }


    public VDialogHeader(final VDialogHeaderCallback callback) {
        this.callback = callback;
        callback.onDescriptionVisibilityChanged(false);
        construct();
    }

    public void setDescription(String description) {
        final Label content = new Label();
        content.setText(description);
        descriptionPanel.insert(content, 0);}

    public void setDialogCaption(String caption) {
        this.caption.setInnerText(caption);
    }


    /**
     * Callback interface for the Dialog header.
     */
    public interface VDialogHeaderCallback {

        void onCloseFired();

        void onDescriptionVisibilityChanged(boolean isVisible);
    }}
