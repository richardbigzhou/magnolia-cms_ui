/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom;

import info.magnolia.cms.security.operations.OperationPermissionDefinition;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditAreaEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewAreaEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentPlaceHolder;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.AreaListener;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;

/**
 * Represents an area inside the {@link CmsNode}-tree.
 * An area can have 3 widgets associated with it:
 *
 * <pre>
 *   <ul>
 *     <li>{@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaBar}</li>
 *     <li>{@link AreaEndBar}</li>
 *     <li>{@link ComponentPlaceHolder}</li>
 *   </ul>
 * </pre>
 *
 * Implements a listener interface for the {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaBar} and {@link ComponentPlaceHolder}.
 * Provides wrapper functions used by the {@link info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel}.
 */
public class MgnlArea extends MgnlElement implements AreaListener {

    public static final String EDITOR_INIT_CLASS_NAME = "init";
    private AreaEndBar areaEndBar;
    private ComponentPlaceHolder componentPlaceHolder;
    private Element componentMarkerElement;
    private EventBus eventBus;

    /**
     * MgnlElement. Represents a node in the tree built on cms-tags.
     */
    public MgnlArea(MgnlElement parent, EventBus eventBus) {
        super(parent);
        this.eventBus = eventBus;
    }

    private AreaEndBar getAreaEndBar() {
        return areaEndBar;
    }

    public void setAreaEndBar(AreaEndBar areaEndBar) {
        this.areaEndBar = areaEndBar;
    }

    private ComponentPlaceHolder getComponentPlaceHolder() {
        return componentPlaceHolder;
    }

    public void setComponentPlaceHolder(ComponentPlaceHolder componentPlaceHolder) {
        this.componentPlaceHolder = componentPlaceHolder;
    }

    public void setComponentMarkerElement(Element componentElement) {
        this.componentMarkerElement = componentElement;
    }

    public Element getComponentMarkerElement() {
        return componentMarkerElement;
    }

    @Override
    public AreaElement getTypedElement() {
        String availableComponents = getAttribute("availableComponents");
        AreaElement area = new AreaElement(getAttribute("workspace"), getAttribute("path"), getAttribute("dialog"), availableComponents);

        boolean areaIsTypeSingle = "single".equals(getAttribute("type"));
        boolean areaHasChildComponents = getComponents().size() > 0;
        boolean optional = Boolean.parseBoolean(getAttribute("optional"));
        boolean created = Boolean.parseBoolean(getAttribute("created"));
        boolean hasAvailableComponents = availableComponents != null && !availableComponents.isEmpty();
        boolean addible = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.ADDIBLE)) {
            addible = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.ADDIBLE));
        }

        area.setOptional(optional);
        area.setCreated(created);
        area.setAddible(addible && hasAvailableComponents && !(optional && !created) && !(areaIsTypeSingle && areaHasChildComponents) && !isMaxComponentsReached());

        return area;
    }

    private boolean isMaxComponentsReached() {
        boolean showAddButton = Boolean.parseBoolean(getAttribute("showAddButton"));
        boolean showNewComponentArea = Boolean.parseBoolean(getAttribute("showNewComponentArea"));

        return showNewComponentArea && !showAddButton;
    }

    @Override
    public void createOptionalArea() {
        eventBus.fireEvent(new NewAreaEvent(getTypedElement()));
    }

    @Override
    public void editArea() {
        eventBus.fireEvent(new EditAreaEvent(getTypedElement()));
    }

    @Override
    public void createNewComponent() {
        eventBus.fireEvent(new NewComponentEvent(getTypedElement()));
    }

    @Override
    public boolean hasAddButton() {
        boolean optional = Boolean.parseBoolean(getAttribute("optional"));
        boolean created = Boolean.parseBoolean(getAttribute("created"));

        return optional && !created;
    }

    @Override
    public boolean hasEditButton() {
        boolean optional = Boolean.parseBoolean(getAttribute("optional"));
        boolean created = Boolean.parseBoolean(getAttribute("created"));
        boolean dialog = null != getAttribute("dialog");

        if (dialog) {
            // do not show edit-icon if the area has not been created
            if (!optional || (optional && created)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAddComponentButton() {
        return Boolean.parseBoolean(getAttribute("showAddButton"));
    }

    @Override
    public String getLabel() {
        String label = getAttribute("label");
        boolean optional = Boolean.parseBoolean(getAttribute("optional"));
        return label + ((optional) ? " (optional)" : "");
    }

    @Override
    public boolean isBoxPlaceHolder() {
        Element marker = getComponentMarkerElement();
        boolean onlyBar = (marker != null && marker.getAttribute(AreaDefinition.CMS_ADD).equals("bar"));
        return !onlyBar;
    }

    @Override
    public String getPlaceHolderLabel() {
        String label = getAttribute("label");
        String labelString;

        // if the add new component area should be visible
        if (isMaxComponentsReached()) { // maximum of components is reached - show add new component area with the maximum reached message, but without the ADD button
            labelString = "Maximum of components reached.";
        } else { // maximum of components is NOT reached - show add new component area with ADD button
            labelString = "New Component";
            if (label != null && !label.isEmpty()) {
                labelString = "New" + " " + label + " " + "Component";
            }
        }
        return labelString;
    }

    public void removeFocus() {
        if (getControlBar() != null) {
            getControlBar().removeFocus();
        }

        if (getAreaEndBar() != null) {
            getAreaEndBar().removeFocus();
        }
    }

    public void setFocus(boolean child) {
        if (getControlBar() != null) {
            getControlBar().setFocus(child);
        }
        if (getAreaEndBar() != null) {
            getAreaEndBar().setFocus(child);
        }
    }

    public void setVisible(boolean visible) {
        if (getControlBar() != null) {
            getControlBar().setVisible(visible);
        }
        if (getAreaEndBar() != null) {
            getAreaEndBar().setVisible(visible);
        }
    }

    public void setPlaceHolderVisible(boolean visible) {
        if (getComponentPlaceHolder() != null) {
            getComponentPlaceHolder().setVisible(visible);
        }
    }

    public void toggleInitFocus(boolean visible) {
        if (visible) {
            if (getControlBar() != null) {
                getControlBar().addStyleName(EDITOR_INIT_CLASS_NAME);
            }
            if (getAreaEndBar() != null) {
                getAreaEndBar().addStyleName(EDITOR_INIT_CLASS_NAME);
                getAreaEndBar().addStyleName(EDITOR_INIT_CLASS_NAME);
            }
        } else {
            if (getControlBar() != null) {
                getControlBar().removeStyleName(EDITOR_INIT_CLASS_NAME);
            }
            if (getAreaEndBar() != null) {
                getAreaEndBar().removeStyleName(EDITOR_INIT_CLASS_NAME);
            }
        }
    }

    public void onDragStart(boolean isDrag) {
        if (getComponentPlaceHolder() != null) {
            getComponentPlaceHolder().setStyleName("moveOngoing", isDrag);
        }
    }
}
