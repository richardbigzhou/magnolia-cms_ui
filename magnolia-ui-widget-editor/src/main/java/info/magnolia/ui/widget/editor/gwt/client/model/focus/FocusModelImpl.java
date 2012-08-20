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

import com.google.gwt.dom.client.Element;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;

/**
 * Helper class to keep track of selected items. Welcome to the MindTwister.
 */
public class FocusModelImpl implements FocusModel {

    private Model model;
    private boolean rootSelected = false;

    public FocusModelImpl(Model model) {
        super();
        this.model = model;
    }

    @Override
    public void onMouseUp(Element element) {

        MgnlElement mgnlElement = model.getMgnlElement(element);

        if (mgnlElement == null) {
            reset();
            return;
        }

        MgnlElement area;

        if (mgnlElement.isComponent()) {
            area = mgnlElement.getParentArea();
        }
        else {
            area = mgnlElement;
        }

        MgnlElement currentArea = model.getSelectedMgnlAreaElement();

        if (currentArea != area) {

            if (!area.isRelated(currentArea)) {
                reset();
            }
            else if(!currentArea.getDescendants().contains(area)) {
                toggleChildComponentSelection(currentArea, false);
            }

            toggleAreaSelection(area, true);
            toggleChildComponentSelection(area, true);

        }
        if (mgnlElement.isComponent()) {
            toggleComponentSelection(mgnlElement);
        }
    }

    @Override
    public void onLoadSelect(MgnlElement selectedMgnlElement) {
        model.setSelectedMgnlAreaElement(selectedMgnlElement);
        toggleRootAreaBar(false);
        //showRootPlaceHolder();
        toggleAreaSelection(selectedMgnlElement, true);
    }

    @Override
    public void reset() {
        MgnlElement currentArea = model.getSelectedMgnlAreaElement();

        if (currentArea != null) {

            toggleAreaSelection(currentArea, false);
            toggleChildComponentSelection(currentArea, false);
        }

        toggleComponentSelection(null);
    }

    /**
     * Takes care of the selection of components. keeps track of last selected element and toggles the focus.
     * If a null-value is passed it will reset the currently selected component.
     * @param component the MgnlElement component
     */
    private void toggleComponentSelection(MgnlElement component) {
        MgnlElement currentComponent = model.getSelectedMgnlComponentElement();
        if (currentComponent == component) {
            return;
        }
        if (currentComponent != null) {
            if(model.getEditBar(currentComponent) != null) {
                model.getEditBar(currentComponent).setFocus(false, false);
            }
        }
        if(model.getEditBar(component) != null) {
            model.getEditBar(component).setFocus(true, false);
        }
        model.setSelectedMgnlComponentElement(component);

    }

    private void toggleAreaSelection(MgnlElement area, boolean visible) {

        MgnlElement parentArea = area.getParentArea();
        if (parentArea != null) {
            toggleAreaSelection(parentArea, visible);
            toggleChildComponentSelection(parentArea, visible);

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

            if (model.getAreaPlaceHolder(area)!= null) {
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


        if (visible) {
            model.setSelectedMgnlAreaElement(area);
        }
        else {
            //model.setSelectedMgnlAreaElement(null);
        }
    }

    private void toggleChildComponentSelection(MgnlElement area, boolean visible) {
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
        reset();

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