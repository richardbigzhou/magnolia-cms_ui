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
package info.magnolia.ui.widget.editor.gwt.client.widget.placeholder;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;

import java.util.Map;

/**
 * Widget for area placeholders.
 */
public class AreaPlaceHolder extends AbstractPlaceHolder {

    private Label areaName;
    private Model model;

    public AreaPlaceHolder(Model model, MgnlElement mgnlElement) throws IllegalArgumentException {

        super(model, mgnlElement);
        this.model = model;

        checkMandatories(mgnlElement.getAttributes());

        this.addStyleName("area");
        String label = mgnlElement.getAttribute("label");
        areaName = new Label(label + " Placeholder");
        areaName.setStyleName("label");

        if (mgnlElement.getRootArea() != mgnlElement) {
            setVisible(false);
        }
        add(areaName);

        attach();

    }

    public void attach() {
        Element parent = getMgnlElement().getAreaElement();
        if (parent == null) {
            parent = getModel().getEditBar(getMgnlElement()).getElement().getParentElement();
            parent.insertAfter(getElement(), getModel().getEditBar(getMgnlElement()).getElement());
        }
        else {
            parent.insertFirst(getElement());
        }

        onAttach();
        getModel().addAreaPlaceHolder(getMgnlElement(), this);
    }

    public void setActive(boolean active) {
        setStyleName("active", active);
        areaName.setVisible(!active);
    }

    private void checkMandatories(Map<String, String> attributes) throws IllegalArgumentException {

        boolean noComponent = attributes.get("type").equals(AreaDefinition.TYPE_NO_COMPONENT);

        if (!getMgnlElement().getComponents().isEmpty() || noComponent) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Model getModel() {
        return model;
    }
}
