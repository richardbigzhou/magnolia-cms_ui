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
package info.magnolia.ui.vaadin.gwt.client.editor.model;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.CmsNode;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;


/**
 * Singleton keeping the model.
 */
public class ModelImpl implements Model {

    private Map<MgnlElement, List<Element>> elements = new HashMap<MgnlElement, List<Element>>();

    private Map<Element, MgnlElement> mgnlElements = new HashMap<Element, MgnlElement>();

    public MgnlElement rootPage;

    public List<MgnlElement> rootAreas = new LinkedList<MgnlElement>();

    private MgnlElement selectedMgnlAreaElement = null;

    private MgnlElement selectedMgnlComponentElement = null;

    @Override
    public void addElement(MgnlElement mgnlElement, Element element) {

        if (mgnlElement == null || element == null) {
            return;
        }
        mgnlElements.put(element, mgnlElement);

        if (elements.get(mgnlElement) != null) {
            elements.get(mgnlElement).add(element);
        }
        else {
            List<Element> elList = new LinkedList<Element>();
            elList.add(element);
            elements.put(mgnlElement, elList);
        }
    }

    @Override
    public void addElements(MgnlElement mgnlElement, Element element) {

        if (mgnlElement == null || element == null) {
            return;
        }
        addElement(mgnlElement, element);

        for (int i = 0; i < element.getChildCount(); i++) {
            Node childNode = element.getChild(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element child = childNode.cast();
                addElements(mgnlElement, child);
            }
        }
    }

    @Override
    public MgnlElement getMgnlElement(Element element) {
        return mgnlElements.get(element);

    }

    @Override
    public MgnlElement getRootPage() {
        return rootPage;
    }

    @Override
    public void setRootPage(MgnlElement rootPage) {
        this.rootPage = rootPage;
    }

    @Override
    public void addRootArea(MgnlElement area) {
        this.rootAreas.add(area);
    }

    @Override
    public List<MgnlElement> getRootAreas() {
        return rootAreas;
    }

    @Override
    public void setSelectedMgnlAreaElement(MgnlElement selectedMgnlAreaElement) {
        this.selectedMgnlAreaElement = selectedMgnlAreaElement;
    }

    @Override
    public MgnlElement getSelectedMgnlAreaElement() {
        return selectedMgnlAreaElement;
    }

    @Override
    public void removeMgnlElement(MgnlElement mgnlElement) {

        // remove all occurrences of the element
        if (mgnlElements.containsValue(mgnlElement)) {
            while (mgnlElements.values().remove(mgnlElement)) {
                ;
            }
        }
        elements.remove(mgnlElement);

        // if the element is a root node, add all children to root list
        if (rootAreas.contains(mgnlElement)) {
            rootAreas.remove(mgnlElement);
            for (CmsNode childNode : mgnlElement.getChildren()) {
                rootAreas.add(childNode.asMgnlElement());
            }
        }
    }

    @Override
    public MgnlElement getSelectedMgnlComponentElement() {
        return selectedMgnlComponentElement;
    }

    @Override
    public void setSelectedMgnlComponentElement(MgnlElement selectedMgnlComponentElement) {
        this.selectedMgnlComponentElement = selectedMgnlComponentElement;
    }

    /**
     * Reset the tree, e.g. when browsing inside the page editor.
     */
    @Override
    public void reset() {
        this.elements = new HashMap<MgnlElement, List<Element>>();
        this.mgnlElements = new HashMap<Element, MgnlElement>();
        this.rootPage = null;
        this.rootAreas = new LinkedList<MgnlElement>();
        this.selectedMgnlAreaElement = null;
        this.selectedMgnlComponentElement = null;
    }
}
