/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.dialog.gwt.client;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * VTabbedDialogHeader.
 */
public class VDialogHeader extends FlowPanel {

    private static final String ClASSNAME_ERROR = "dialog-error";
    private static final String CLASSNAME_HEADER = "dialog-header";
    private static final String ClASSNAME_DESCRIPTION = "dialog-description";
    private static final String CLASSNAME_HELPBUTTON = "btn-dialog-help";
    private static final String CLASSNAME_CLOSEBUTTON = "btn-dialog-close";
    
    private final VDialogHeaderCallback callback;
    
    private FlowPanel errorPanel = new FlowPanel();
    
    private FlowPanel descriptionPanel = new FlowPanel();
    
    private Element captionContainer = DOM.createDiv();
    
    private Element caption = DOM.createSpan();
    
    private boolean isDescriptionVisible = false;
    
    private final Button helpButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            isDescriptionVisible = !isDescriptionVisible;
            descriptionPanel.setVisible(isDescriptionVisible);
            callback.onDescriptionVisibilityChanged(isDescriptionVisible);
        }
    });
    
    private final Button closeButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            callback.onCloseFired();
        }
    });
    
    
    public VDialogHeader(final VDialogHeaderCallback callback) {
        this.callback = callback;
        callback.onDescriptionVisibilityChanged(false);
        construct();
    }


    private void construct() {
        captionContainer.addClassName(CLASSNAME_HEADER);
        errorPanel.addStyleName(ClASSNAME_ERROR);
        descriptionPanel.addStyleName(ClASSNAME_DESCRIPTION);    
        helpButton.setStyleName(CLASSNAME_HELPBUTTON);
        closeButton.setStyleName(CLASSNAME_CLOSEBUTTON);
        closeButton.addStyleName("green");
        
        getElement().appendChild(captionContainer);
        captionContainer.appendChild(caption);
        
        descriptionPanel.setVisible(false);
        add(closeButton, captionContainer);
        add(helpButton, captionContainer);
        add(descriptionPanel);
        add(errorPanel);
    }
    
    public void setDialogCaption(final String caption) {
        this.caption.setInnerText(caption);
    }
    
    public void setDescription(final String dialogDescription) {
        final Label content = new Label();
        content.setText(dialogDescription);
        descriptionPanel.insert(content, 0);
    }
    
    
    /**
     * Callback interface for the Dialog header.
     */
    interface VDialogHeaderCallback {
        
        void onCloseFired();
        
        void onDescriptionVisibilityChanged(boolean isVisible);

        void jumpToNextError();
    }


    public void setErrorAmount(int totalProblematicFields) {
        errorPanel.setVisible(totalProblematicFields > 0);
        if (totalProblematicFields > 0) {
            errorPanel.getElement().setInnerHTML("<span>Please correct the <b>" + totalProblematicFields + 
                    " errors </b> in this form </span>");

            
            final HTML errorButton = new HTML("[Jump to next error]");
            errorButton.setStyleName("action-jump-to-next-error");
            DOM.sinkEvents(errorButton.getElement(), Event.MOUSEEVENTS);
            errorButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    callback.jumpToNextError();
                }
            }, ClickEvent.getType());
            errorPanel.add(errorButton);
        }
    }
    
}
