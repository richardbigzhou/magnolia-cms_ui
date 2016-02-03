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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * A Widget for adding components to area. Marks where a newly created component will be injected.
 * Is assembled by a {@link PlaceHolderBar} and an empty place holder area.
 */
public class ComponentPlaceHolder extends FlowPanel {

    private final static String PLACEHOLDER_CLASS_NAME = "mgnlPlaceholder";
    private final static String PLACEHOLDER_ELEMENT_BOX_CLASS_NAME = "mgnlPlaceholderBox";

    private final MgnlArea listener;

    private PlaceHolderBar placeHolderBar;

    public ComponentPlaceHolder(MgnlArea area) throws IllegalArgumentException {

        this.listener = area;
        this.placeHolderBar = new PlaceHolderBar(area);
        setStyleName(AbstractBar.EDITOR_CLASS_NAME);
        addStyleName(PLACEHOLDER_CLASS_NAME);

        setVisible(false);
        initLayout();
    }

    protected void initLayout() {
        add(placeHolderBar);
        if (listener.isBoxPlaceHolder()) {

            Element element = DOM.createDiv();
            element.addClassName(AbstractBar.EDITOR_CLASS_NAME);
            element.addClassName(PLACEHOLDER_ELEMENT_BOX_CLASS_NAME);
            getElement().appendChild(element);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        placeHolderBar.setVisible(visible);
    }

    @Override
    public void onAttach() {
        super.onAttach();
    }

    /**
     * A placeholder bar which resembles the {@link ComponentBar}.
     */
    private class PlaceHolderBar extends AbstractBar {

        private final MgnlArea listener;

        public PlaceHolderBar(MgnlArea area) {
            super(area);
            this.listener = area;

            addStyleName(COMPONENT_CLASS_NAME);

            initLayout();
        }

        @Override
        protected String getLabel() {
            return listener.getPlaceHolderLabel();
        }

        @Override
        protected void createControls() {

            if (listener.hasAddComponentButton()) {
                final Label add = new Label();
                add.setStyleName(ICON_CLASS_NAME);
                add.addStyleName(ADD_CLASS_NAME);

                TouchDelegate td = new TouchDelegate(add);
                td.addTouchEndHandler(new TouchEndHandler() {
                    @Override
                    public void onTouchEnd(TouchEndEvent touchEndEvent) {
                        listener.createNewComponent();
                    }
                });

                addButton(add);
            }
        }
    }
}
