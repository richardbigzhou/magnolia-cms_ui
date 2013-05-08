/**
 * This file Copyright (c) 2011 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.AreaListener;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Area bar.
 */
public class AreaBar extends AbstractBar {

    private String name;

    private String type;

    private String dialog;

    private boolean optional;

    private boolean created;

    private boolean editable = true;

    private final AreaListener listener;

    public AreaBar(MgnlArea mgnlElement) {
        super(mgnlElement);
        listener = mgnlElement;


        GWT.log("Area [" + this.name + "] is of type " + this.type);

        this.addStyleName("area");

        setVisible(false);
        createControls();

    }

    @Override
    protected String createLabel(String label) {
        return label + ((optional) ? " (optional)" : "");
    }

    private void createControls() {
        if (this.optional) {
            if (!this.created) {
                final Label add = new Label();
                add.setStyleName(ICON_CLASSNAME);
                add.addStyleName(ADD_CLASSNAME);
                add.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        listener.createOptionalArea();
                    }
                });
                addButton(add);
            }
        }

        if (this.dialog != null) {
            // do not show edit-icon if the area has not been created
            if (!this.optional || (this.optional && this.created)) {
                final Label edit = new Label();
                edit.setStyleName(ICON_CLASSNAME);
                edit.addStyleName(EDIT_CLASSNAME);
                edit.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        listener.editArea();
                    }
                });
                addButton(edit);
            }
        }

    }

    @Override
    protected void setFields(Map<String, String> attributes) throws IllegalArgumentException {

        this.name = attributes.get("name");
        this.type = attributes.get("type");

        this.dialog = attributes.get("dialog");

        if (attributes.containsKey("editable")) {
            this.editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        boolean showAddButton = Boolean.parseBoolean(attributes.get("showAddButton"));
        this.optional = Boolean.parseBoolean(attributes.get("optional"));
        this.created = Boolean.parseBoolean(attributes.get("created"));
    }

}
