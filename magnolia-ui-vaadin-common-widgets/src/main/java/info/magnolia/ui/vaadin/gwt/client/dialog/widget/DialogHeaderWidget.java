/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.dialog.widget;

import info.magnolia.ui.vaadin.gwt.client.CloseButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * DialogHeaderWidget.
 */
public class DialogHeaderWidget extends FlowPanel {

    private static final String CLASSNAME_HEADER = "dialog-header";
    private static final String ClASSNAME_DESCRIPTION = "dialog-description";
    private static final String CLASSNAME_WIDEBUTTON = "btn-form-wide";
    private static final String CLASSNAME_WIDEBUTTON_ICON = "icon-open-fullscreen-2";
    private static final String CLASSNAME_WIDEBUTTON_ICON_CLOSE = "icon-close-fullscreen-2";
    private static final String CLASSNAME_HELPBUTTON = "btn-form-help";
    private static final String CLASSNAME_HEADER_TOOLBAR = "dialog-header-toolbar";

    protected CloseButton closeButton = new CloseButton();

    protected DialogHeaderCallback callback = null;

    protected final FlowPanel descriptionPanel = new FlowPanel();

    protected final Element headerPanel = DOM.createDiv();

    protected final Element caption = DOM.createSpan();

    protected final Element toolbarEl = DOM.createSpan();

    protected Widget toolbar;

    protected static boolean isDescriptionVisible = false;

    protected boolean hasDescription = false;

    protected boolean isWide = false;

    protected final Button helpButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            isDescriptionVisible = !isDescriptionVisible;
            onDescriptionVisibility();
        }
    });

    protected final Button wideButton = new Button("", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            isWide = !isWide;
            onWideChanged();
        }
    });


    private void onWideChanged(){
        callback.onWideChanged(isWide);
        setWideIcon();
    }

    private void setWideIcon(){
        if (isWide){
            wideButton.removeStyleName(CLASSNAME_WIDEBUTTON_ICON);
            wideButton.addStyleName(CLASSNAME_WIDEBUTTON_ICON_CLOSE);
        }else{
            wideButton.removeStyleName(CLASSNAME_WIDEBUTTON_ICON_CLOSE);
            wideButton.addStyleName(CLASSNAME_WIDEBUTTON_ICON);
        }
    }

    private void onDescriptionVisibility() {
        if (hasDescription) {
            descriptionPanel.setVisible(isDescriptionVisible);
        }
        callback.onDescriptionVisibilityChanged(isDescriptionVisible);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        onDescriptionVisibility();
        if (hasDescription) {
            if (this.getElement().getParentElement() != null) {
                this.getElement().getParentElement().setAttribute("role", "dialogDescriptionHeader");
            }
        }
    }

    public DialogHeaderWidget(DialogHeaderCallback callback) {
        this.callback = callback;
        callback.onDescriptionVisibilityChanged(false);
        construct();
    }

    public void construct() {

        closeButton.addStyleDependentName("dialog");
        closeButton.setVisible(false);
        headerPanel.appendChild(closeButton.getElement());
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Element target = event.getNativeEvent().getEventTarget().cast();
                if (closeButton.getElement().isOrHasChild(target)) {
                    callback.onCloseFired();
                }
            }
        }, ClickEvent.getType());

        headerPanel.addClassName(CLASSNAME_HEADER);
        descriptionPanel.addStyleName(ClASSNAME_DESCRIPTION);
        wideButton.setStyleName(CLASSNAME_WIDEBUTTON);
        setWideIcon();
        helpButton.setStyleName(CLASSNAME_HELPBUTTON);
        toolbarEl.addClassName(CLASSNAME_HEADER_TOOLBAR);

        getElement().appendChild(headerPanel);
        caption.addClassName("title");
        headerPanel.appendChild(caption);
        add(wideButton, headerPanel);
        add(helpButton, headerPanel);
        headerPanel.appendChild(toolbarEl);

        descriptionPanel.setVisible(false);
        add(descriptionPanel);

    }

    public void setDescription(String description) {
        final Label content = new Label();
        content.setText(description);
        descriptionPanel.insert(content, 0);
        hasDescription = !description.isEmpty();
        if (hasDescription) {
            descriptionPanel.setVisible(isDescriptionVisible);
            if (this.getElement().getParentElement() != null) {
                this.getElement().getParentElement().setAttribute("role", "dialogDescriptionHeader");
            }
        }
    }

    public void setWide(boolean isWide) {
        this.isWide = isWide;
        setWideIcon();
    }

    public void setCaption(String caption) {
        this.caption.setInnerText(caption);
    }

    public void setToolbar(Widget toolbarWidget) {
        if (toolbar != null) {
            remove(toolbar);
        }
        toolbar = toolbarWidget;
        add(toolbarWidget, toolbarEl);
    }

    public void showCloseButton() {
        closeButton.setVisible(true);
    }

    /**
     * Callback interface for the EditorLike header.
     */
    public interface DialogHeaderCallback {

        void onDescriptionVisibilityChanged(boolean isVisible);

        void onCloseFired();

        void onWideChanged(boolean isWide);
    }
}
