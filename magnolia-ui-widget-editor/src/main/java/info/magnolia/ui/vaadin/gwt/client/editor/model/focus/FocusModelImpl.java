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
package info.magnolia.ui.vaadin.gwt.client.editor.model.focus;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.CmsNode;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;

import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Helper class to keep track of selected items. Welcome to the MindTwister.
 */
public class FocusModelImpl implements FocusModel {

    private final Model model;

    private boolean rootSelected = false;

    private final EventBus eventBus;

    public FocusModelImpl(EventBus eventBus, Model model) {
        super();
        this.eventBus = eventBus;
        this.model = model;
    }

    @Override
    public void selectElement(Element element) {

        MgnlElement mgnlElement = model.getMgnlElement(element);

        MgnlElement area = null;
        MgnlElement component = null;

        // if there is no mapping, we select the page
        if (mgnlElement == null) {
            mgnlElement = model.getRootPage();
            setPageSelection(true);
        } else {
            setPageSelection(false);
            if (mgnlElement.isComponent()) {
                component = mgnlElement;
                area = mgnlElement.getParentArea().asMgnlElement();
            } else if (mgnlElement.isArea()) {
                area = mgnlElement;
            }
        }
        // first set the component, then set the area. the selected component is used for setting
        // the corrent area class.
        setComponentSelection(component);
        setAreaSelection(area);

        select(mgnlElement);

    }

    @Override
    public void init() {
        toggleRootAreaBar(true);
        setPageSelection(true);
        select(model.getRootPage());
    }

    @Override
    public void setPageSelection(boolean select) {
        MgnlElement page = model.getRootPage();
        if (page.getControlBar() != null) {
            if (select) {
                page.getControlBar().setFocus(false);
            } else {
                page.getControlBar().removeFocus();
            }
        }
    }

    /**
     * Takes care of the selection of components. keeps track of last selected element and toggles
     * the focus. If a null-value is passed it will reset the currently selected component.
     *
     * @param component the MgnlElement component, can be null.
     */
    private void setComponentSelection(MgnlElement component) {
        MgnlElement currentComponent = model.getSelectedMgnlComponentElement();
        if (currentComponent == component) {
            return;
        }
        if (currentComponent != null) {
            if (currentComponent.getControlBar() != null) {
                currentComponent.getControlBar().removeFocus();
            }
        }
        if (component != null && component.getControlBar() != null) {
            component.getControlBar().setFocus(false);
        }
        model.setSelectedMgnlComponentElement(component);

    }

    /**
     * This method takes care of selecting and deselecting areas.
     *
     * @param area selected area, can be null.
     */
    private void setAreaSelection(MgnlElement area) {
        MgnlElement selectedArea = model.getSelectedMgnlAreaElement();
        MgnlElement currentComponent = model.getSelectedMgnlComponentElement();

        if (selectedArea != null) {

            if (selectedArea.getControlBar() != null) {
                selectedArea.getControlBar().removeFocus();
            }

            if (selectedArea.getAreaEndBar() != null) {
                selectedArea.getAreaEndBar().removeFocus();
            }

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

            if (area.getControlBar() != null) {
                area.getControlBar().setFocus((currentComponent != null));
            }
            if (area.getAreaEndBar() != null) {
                area.getAreaEndBar().setFocus((currentComponent != null));
            }
        }
        model.setSelectedMgnlAreaElement(area);
    }

    private void toggleAreaVisibility(MgnlElement area, boolean visible) {

        CmsNode parentArea = area.getParentArea();
        if (parentArea != null) {
            toggleAreaVisibility(parentArea.asMgnlElement(), visible);
            toggleChildComponentVisibility(parentArea.asMgnlElement(), visible);

        }
        // root areas are always visible
        if (!model.getRootAreas().contains(area)) {
            if (area.getControlBar() != null) {
                area.getControlBar().setVisible(visible);
            }
            if (area.getAreaEndBar() != null) {
                area.getAreaEndBar().setVisible(visible);
            }
        }

        if (!area.isRelated(model.getSelectedMgnlAreaElement())) {

            /*
             * if (model.getAreaPlaceHolder(area) != null) {
             * model.getAreaPlaceHolder(area).setActive(visible);
             * }
             */

            // toggle all direct child-areas placeholders visibility
            for (CmsNode childArea : area.getAreas()) {

                /*
                 * if (model.getAreaPlaceHolder(childArea) != null) {
                 * model.getAreaPlaceHolder(childArea).setVisible(visible);
                 * }
                 */
                if (childArea.asMgnlElement().getControlBar() != null) {
                    childArea.asMgnlElement().getControlBar().setVisible(visible);
                }
                if (childArea.asMgnlElement().getAreaEndBar() != null) {
                    childArea.asMgnlElement().getAreaEndBar().setVisible(visible);
                }
            }
        }

    }

    private void toggleChildComponentVisibility(MgnlElement area, boolean visible) {
        for (CmsNode component : area.getComponents()) {

            // toggle all child-components editbar visibility - does this case occur?
            if (component.asMgnlElement().getControlBar() != null) {
                component.asMgnlElement().getControlBar().setVisible(visible);
            }
            if (area.getComponentPlaceHolder() != null) {
                area.getComponentPlaceHolder().setVisible(visible);
            }
            // toggle all child-components-area placeholder visibility
            for (CmsNode childArea : component.getAreas()) {

                /*
                 * if (model.getAreaPlaceHolder(childArea) != null) {
                 * model.getAreaPlaceHolder(childArea).setVisible(visible);
                 * }
                 */
                if (childArea.asMgnlElement().getControlBar() != null) {
                    childArea.asMgnlElement().getControlBar().setVisible(visible);
                }
                if (childArea.asMgnlElement().getAreaEndBar() != null) {
                    childArea.asMgnlElement().getAreaEndBar().setVisible(visible);
                }
            }
        }
    }

    @Override
    public void toggleRootAreaBar(boolean visible) {

        this.rootSelected = !this.rootSelected;
        for (CmsNode root : model.getRootAreas()) {
            if (root.asMgnlElement().getControlBar() != null) {
                root.asMgnlElement().getControlBar().setVisible(visible);
            }
            if (root.asMgnlElement().getAreaEndBar() != null) {
                root.asMgnlElement().getAreaEndBar().setVisible(visible);
            }
            if (root.asMgnlElement().getComponentPlaceHolder() != null) {
                root.asMgnlElement().getComponentPlaceHolder().setVisible(visible);
            }
        }
    }

    @Override
    public void select(MgnlElement mgnlElement) {
        eventBus.fireEvent(new SelectElementEvent(mgnlElement.getTypedElement()));
    }

}