/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.model.focus;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlPage;
import info.magnolia.ui.vaadin.gwt.client.editor.event.ComponentStopMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;

import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Helper class to keep track of selected items. Welcome to the MindTwister.
 */
public class FocusModelImpl implements FocusModel {

    private final Model model;
    private final EventBus eventBus;

    private boolean rootSelected = false;

    public FocusModelImpl(EventBus eventBus, Model model) {
        super();
        this.eventBus = eventBus;
        this.model = model;
    }

    @Override
    public void selectElement(Element element) {


        MgnlElement mgnlElement = model.getMgnlElement(element);

        if (model.isMoving()) {
            // cancel move if click was outside the moving components relatives or null
            if (mgnlElement == null || !mgnlElement.isRelated(model.getSelectedComponent())) {
                eventBus.fireEvent(new ComponentStopMoveEvent(null, false));
            }
            return;
        }

        MgnlPage page = null;
        MgnlArea area = null;
        MgnlComponent component = null;

        // if there is no mapping, we select the page
        if (mgnlElement == null) {
            page = model.getRootPage();
        } else {
            if (mgnlElement.isComponent()) {
                component = (MgnlComponent) mgnlElement;
                area = mgnlElement.getParentArea();
            } else if (mgnlElement.isArea()) {
                area = (MgnlArea) mgnlElement;
            }
        }
        // first set the component, then set the area. the selected component is used for setting
        // the current area class.
        setComponentSelection(component);
        setAreaSelection(area);
        setPageSelection(page);

        select(mgnlElement);
    }

    @Override
    public void init() {
        for (MgnlArea root : model.getRootAreas()) {
            root.setVisible(true);

            if(root.getComponents().isEmpty()) {
                root.setPlaceHolderVisible(true);
            }
        }

        setPageSelection(model.getRootPage());
        select(model.getRootPage());
    }

    /**
     * Takes care of the selection of components. keeps track of last selected element and toggles
     * the focus. If a null-value is passed it will reset the currently selected component.
     *
     * @param component the MgnlElement component, can be null.
     */
    private void setComponentSelection(MgnlComponent component) {
        MgnlComponent currentComponent = model.getSelectedComponent();
        if (currentComponent == component) {
            return;
        }
        if (currentComponent != null) {
                currentComponent.removeFocus();
        }
        if (component != null) {
            component.setFocus();
        }
        model.setSelectedComponent(component);

    }

    /**
     * This method takes care of selecting and deselecting areas.
     *
     * @param area selected area, can be null.
     */
    private void setAreaSelection(MgnlArea area) {
        MgnlArea selectedArea = model.getSelectedArea();
        MgnlComponent currentComponent = model.getSelectedComponent();

        if (selectedArea != null) {

            selectedArea.removeFocus();

            // always reset current area selection unless area and current area are related
            if (!selectedArea.isRelated(area)) {

                toggleChildComponentVisibility(selectedArea, false);
                toggleAreaVisibility(selectedArea, false);
            }

            // hide child components if area is an ascendant of current area or selectedArea is not
            // a descendant
            else if (selectedArea.getAscendants().contains(area) || (area != null && !area.getDescendants().contains(selectedArea))) {
                toggleChildComponentVisibility(selectedArea, false);
            }
        }

        // set focus on new selected area
        if (area != null) {
            toggleAreaVisibility(area, true);
            toggleChildComponentVisibility(area, true);

            area.setFocus((currentComponent != null));
        }
        model.setSelectedArea(area);
    }

    private void toggleAreaVisibility(MgnlArea area, boolean visible) {

        MgnlArea parentArea = area.getParentArea();
        if (parentArea != null) {
            toggleAreaVisibility(parentArea, visible);
            toggleChildComponentVisibility(parentArea, visible);

        }
        // root areas are always visible
        if (!model.getRootAreas().contains(area)) {
            area.setVisible(visible);
        }

        toggleNestedAreasVisibility(area, visible);
    }

    private void toggleChildComponentVisibility(MgnlArea area, boolean visible) {

        // do not hide empty root areas placeholder
        if (!model.getRootAreas().contains(area) || !area.getComponents().isEmpty()) {
            area.setPlaceHolderVisible(visible);
        }

        // hide
        if (!visible && !area.getComponents().isEmpty()) {
            area.setPlaceHolderVisible(visible);
        }


        for (MgnlComponent component : area.getComponents()) {

            // toggle all child-components editbar visibility - does this case occur?
            component.setVisible(visible);


            // toggle all child-components-area visibility
            for (MgnlArea childArea : component.getAreas()) {
                childArea.setVisible(visible);
                toggleNestedAreasVisibility(childArea, visible);
            }
        }
    }

    private void toggleNestedAreasVisibility(MgnlArea area, boolean visible) {
        for (MgnlArea childArea : area.getAreas()) {
            childArea.setVisible(visible);
            toggleNestedAreasVisibility(childArea, visible);
        }
    }

    private void setPageSelection(MgnlPage page) {
        boolean visible = true;
        if (page == null) {
            visible = false;
        }

        this.rootSelected = !this.rootSelected;
        for (MgnlArea root : model.getRootAreas()) {
            root.toggleInitFocus(visible);
        }
    }

    @Override
    public void select(MgnlElement mgnlElement) {
        mgnlElement = (mgnlElement != null) ? mgnlElement : model.getRootPage();
        eventBus.fireEvent(new SelectElementEvent(mgnlElement.getTypedElement()));
    }

}
