/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.vaadin.gwt.client.widget.placeholder.ComponentPlaceHolder;

import com.google.gwt.dom.client.Element;

/**
 * MgnlArea.
 */
public class MgnlArea extends MgnlElement {
    private AreaEndBar areaEndBar;
    private ComponentPlaceHolder componentPlaceHolder;
    private Element componentMarkerElement;

    /**
     * MgnlElement. Represents a node in the tree built on cms-tags.
     */
    public MgnlArea(MgnlElement parent) {
        super(parent);
    }

    public AreaEndBar getAreaEndBar() {
        return areaEndBar;
    }

    public void setAreaEndBar(AreaEndBar areaEndBar) {
        this.areaEndBar = areaEndBar;
    }

    public ComponentPlaceHolder getComponentPlaceHolder() {
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
    public AbstractElement getTypedElement() {
        return new AreaElement(getAttribute("workspace"), getAttribute("path"), getAttribute("dialog"), getAttribute("availableComponents"));
    }

    @Override
    public boolean isPage() {
        return false;
    }

    @Override
    public boolean isArea() {
        return true;
    }

    @Override
    public boolean isComponent() {
        return false;
    }

}
