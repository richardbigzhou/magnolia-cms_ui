/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.AreaListener;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * A Widget for adding components to area.
 */
public class ComponentPlaceHolder extends AbstractBar {

    protected final static String ICON_CLASSNAME = "editorIcon";
    protected final static String ADD_CLASSNAME = "icon-add-item";

    private final AreaListener listener;

    public ComponentPlaceHolder(MgnlArea area) throws IllegalArgumentException {
        this.listener = area;

        this.addStyleName("component placeholder");

        Element marker = area.getComponentMarkerElement();
        boolean onlyBar = (marker != null && marker.getAttribute(AreaDefinition.CMS_ADD).equals("bar"));

        if (!onlyBar) {
            this.addStyleName("box");
        }

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
            add.setStyleName(ICON_CLASSNAME);
            add.addStyleName(ADD_CLASSNAME);
            add.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    listener.createNewComponent();
                }
            });
            addButton(add);
        }
    }


    @Override
    public void onAttach() {
        super.onAttach();
    }

}
