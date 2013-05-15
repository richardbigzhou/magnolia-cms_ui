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
package info.magnolia.ui.vaadin.gwt.client.editor.dom.processor;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaBar;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentPlaceHolder;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

/**
 * Factory Class for MgnlElement processors.
 */
public class AreaProcessor extends AbstractMgnlElementProcessor {

    public AreaProcessor(Model model, MgnlArea mgnlElement) {
        super(model, mgnlElement);
    }

    @Override
    public void process() {

        if (hasControlBar(getMgnlElement().getAttributes())) {
            AreaBar areaBar = new AreaBar(getMgnlElement());
            setEditBar(areaBar);
            attachWidget();

            if (hasComponentPlaceHolder(getMgnlElement().getAttributes())) {
                ComponentPlaceHolder placeHolder = new ComponentPlaceHolder(getMgnlElement());
                attachComponentPlaceHolder(placeHolder);
                addToModel(placeHolder);
            }

            AreaEndBar endBar = new AreaEndBar(getMgnlElement());
            attachAreaEndBar(endBar);
            addToModel(endBar);

        } else {

            GWT.log("Not creating areabar and area endbar for this element. Missing parameters. Will be deleted.");

            // if the area has no controls we, don't want it in the structure.

            // delete the element from the tree
            // set all child parents to parent
            getMgnlElement().delete();

            // remove it from the Model
            getModel().removeMgnlElement(getMgnlElement());
        }
    }

    private void addToModel(ComponentPlaceHolder placeHolder) {
        getModel().addElements(getMgnlElement(), placeHolder.getElement());

    }

    private void addToModel(AreaEndBar endBar) {
        getModel().addElements(getMgnlElement(), endBar.getElement());
        getMgnlElement().setAreaEndBar(endBar);
    }

    private boolean hasComponentPlaceHolder(Map<String, String> attributes) {

        String type = attributes.get("type");
        boolean optional = Boolean.parseBoolean(attributes.get("optional"));
        boolean created = Boolean.parseBoolean(attributes.get("created"));

        if (optional && !created) {
            return false;
        }
        String availableComponents = "";
        if (AreaDefinition.TYPE_NO_COMPONENT.equals(type)) {
            availableComponents = "";
        } else {
            availableComponents = attributes.get("availableComponents");
        }

        if (availableComponents.equals("")) {
            return false;
        }

        if (type.equals(AreaDefinition.TYPE_SINGLE) && !getMgnlElement().getComponents().isEmpty()) {
            return false;
        }
        return true;

    }

    private boolean hasControlBar(Map<String, String> attributes) {

        String type = attributes.get("type");
        String dialog = attributes.get("dialog");

        boolean editable = true;
        if (attributes.containsKey("editable")) {
            editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        boolean showNewComponentArea = Boolean.parseBoolean(attributes.get("showNewComponentArea"));
        boolean optional = Boolean.parseBoolean(attributes.get("optional"));

        // break no matter what follows
        if (!editable) {
            return false;
        }

        // area can be deleted or created
        if (optional) {
            return true;
        } else if (type.equals(AreaDefinition.TYPE_SINGLE)) {
            return true;
        }

        // can show add new components area
        else if (showNewComponentArea) {
            return true;
        }

        // area can be edited
        else if (dialog != null && !dialog.isEmpty()) {
            return true;
        }

        return false;
    }

    private void attachAreaEndBar(AreaEndBar controlBar) {
        if (getMgnlElement().getFirstElement() != null && getMgnlElement().getFirstElement() == getMgnlElement().getLastElement()) {
            Element element = getMgnlElement().getFirstElement();
            if (element != null) {
                element.appendChild(controlBar.getElement());
            }
        } else {
            Element element = getMgnlElement().getEndComment();
            Node parentNode = element.getParentNode();
            parentNode.insertBefore(controlBar.getElement(), element);

            // attach(getMgnlElement().getEndComment());
        }
        controlBar.onAttach();
    }

    private void attachComponentPlaceHolder(ComponentPlaceHolder placeHolder) {
        Element parent = getMgnlElement().getComponentMarkerElement();

        if (parent == null) {
            if (getMgnlElement().getLastElement() != null && getMgnlElement().getFirstElement() == getMgnlElement().getLastElement()) {
                Element element = getMgnlElement().getFirstElement();
                if (element != null) {
                    element.appendChild(placeHolder.getElement());
                }
            } else {
                Element element = getMgnlElement().getEndComment();
                Node parentNode = element.getParentNode();
                parentNode.insertBefore(placeHolder.getElement(), element);
            }
        } else {
            parent.insertFirst(placeHolder.getElement());
        }
        placeHolder.onAttach();
        getMgnlElement().setComponentPlaceHolder(placeHolder);
    }

    @Override
    public MgnlArea getMgnlElement() {
        return (MgnlArea) super.getMgnlElement();
    }
}
