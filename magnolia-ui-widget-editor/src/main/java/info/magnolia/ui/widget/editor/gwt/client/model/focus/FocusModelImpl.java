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
package info.magnolia.ui.widget.editor.gwt.client.model.focus;

import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.event.SelectElementEvent;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AbstractBar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;


/**
 * Helper class to keep track of selected items. Welcome to the MindTwister.
 */
public class FocusModelImpl implements FocusModel {

    private final Model model;

    private boolean rootSelected = false;

    private final MgnlElement focusedElement = null;

    private final EventBus eventBus;

    public FocusModelImpl(EventBus eventBus, Model model) {
        super();
        this.eventBus = eventBus;
        this.model = model;
    }

    @Override
    public void onMouseUp(Element element) {

        MgnlElement mgnlElement = model.getMgnlElement(element);

        MgnlElement area = null;
        MgnlElement component = null;

        if (mgnlElement != null) {
            if (mgnlElement.isComponent()) {
                component = mgnlElement;
                area = mgnlElement.getParentArea();
            }
            else {
                area = mgnlElement;
            }

            AbstractBar editBar = model.getEditBar(mgnlElement);
            eventBus.fireEvent(new SelectElementEvent(editBar.getPath(), editBar.getWorkspace(), editBar.getDialog()));
        }
        else {
            // send page as selected element
        }

        // first set the component, then set the area. the selected component is used for setting
        // the corrent area class.
        setComponentSelection(component);
        setAreaSelection(area);

    }

    /**
     * Takes care of the selection of components. keeps track of last selected element and toggles
     * the focus. If a null-value is passed it will reset the currently selected component.
     * @param component the MgnlElement component, can be null.
     */
    private void setComponentSelection(MgnlElement component) {
        MgnlElement currentComponent = model.getSelectedMgnlComponentElement();
        if (currentComponent == component) {
            return;
        }
        if (currentComponent != null) {
            if (model.getEditBar(currentComponent) != null) {
                model.getEditBar(currentComponent).removeFocus();
            }
        }
        if (model.getEditBar(component) != null) {
            model.getEditBar(component).setFocus(false);
        }
        model.setSelectedMgnlComponentElement(component);

    }

    /**
     * This method takes care of selecting and deselecting areas.
     * @param area selected area, can be null.
     */
    private void setAreaSelection(MgnlElement area) {
        MgnlElement selectedArea = model.getSelectedMgnlAreaElement();
        MgnlElement currentComponent = model.getSelectedMgnlComponentElement();

        if (selectedArea != null) {

            if (model.getEditBar(selectedArea) != null) {
                model.getEditBar(selectedArea).removeFocus();
            }

            if (model.getAreaEndBar(selectedArea) != null) {
                model.getAreaEndBar(selectedArea).removeFocus();
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

            if (model.getEditBar(area) != null) {
                model.getEditBar(area).setFocus((currentComponent != null));
            }
            if (model.getAreaEndBar(area) != null) {
                model.getAreaEndBar(area).setFocus((currentComponent != null));
            }
        }
        model.setSelectedMgnlAreaElement(area);
    }

    private void toggleAreaVisibility(MgnlElement area, boolean visible) {

        MgnlElement parentArea = area.getParentArea();
        if (parentArea != null) {
            toggleAreaVisibility(parentArea, visible);
            toggleChildComponentVisibility(parentArea, visible);

        }
        // root areas are always visible
        if (!model.getRootElements().contains(area)) {
            if (model.getEditBar(area) != null) {
                model.getEditBar(area).setVisible(visible);
            }
            if (model.getAreaEndBar(area) != null) {
                model.getAreaEndBar(area).setVisible(visible);
            }
        }

        if (!area.isRelated(model.getSelectedMgnlAreaElement())) {

            if (model.getAreaPlaceHolder(area) != null) {
                model.getAreaPlaceHolder(area).setActive(visible);
            }

            // toggle all direct child-areas placeholders visibility
            for (MgnlElement childArea : area.getAreas()) {

                if (model.getAreaPlaceHolder(childArea) != null) {
                    model.getAreaPlaceHolder(childArea).setVisible(visible);
                }
                if (model.getEditBar(childArea) != null) {
                    model.getEditBar(childArea).setVisible(visible);
                }
                if (model.getAreaEndBar(childArea) != null) {
                    model.getAreaEndBar(childArea).setVisible(visible);
                }
            }
        }

    }

    private void toggleChildComponentVisibility(MgnlElement area, boolean visible) {
        for (MgnlElement component : area.getComponents()) {

            // toggle all child-components editbar visibility - does this case occur?
            if (model.getEditBar(component) != null) {
                model.getEditBar(component).setVisible(visible);
            }
            if (model.getComponentPlaceHolder(area) != null) {
                model.getComponentPlaceHolder(area).setVisible(visible);
            }
            // toggle all child-components-area placeholder visibility
            for (MgnlElement childArea : component.getAreas()) {

                if (model.getAreaPlaceHolder(childArea) != null) {
                    model.getAreaPlaceHolder(childArea).setVisible(visible);
                }
                if (model.getEditBar(childArea) != null) {
                    model.getEditBar(childArea).setVisible(visible);
                }
                if (model.getAreaEndBar(childArea) != null) {
                    model.getAreaEndBar(childArea).setVisible(visible);
                }
            }
        }
    }

    @Override
    public void toggleRootAreaBar(boolean visible) {

        this.rootSelected = !this.rootSelected;
        for (MgnlElement root : model.getRootElements()) {
            if (model.getEditBar(root) != null) {
                model.getEditBar(root).setVisible(visible);
            }
            if (model.getAreaEndBar(root) != null) {
                model.getAreaEndBar(root).setVisible(visible);
            }
            if (model.getAreaPlaceHolder(root) != null) {
                model.getAreaPlaceHolder(root).setVisible(visible);
            }
        }
    }

}