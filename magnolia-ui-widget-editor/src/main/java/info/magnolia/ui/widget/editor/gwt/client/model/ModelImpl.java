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
package info.magnolia.ui.widget.editor.gwt.client.model;

import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AbstractBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.overlay.AbstractOverlay;
import info.magnolia.ui.widget.editor.gwt.client.widget.placeholder.AreaPlaceHolder;
import info.magnolia.ui.widget.editor.gwt.client.widget.placeholder.ComponentPlaceHolder;

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

    private final Map<MgnlElement, AbstractBar> editBars = new HashMap<MgnlElement, AbstractBar>();

    private final Map<MgnlElement, AbstractOverlay> overlays = new HashMap<MgnlElement, AbstractOverlay>();

    private final Map<MgnlElement, List<Element>> elements = new HashMap<MgnlElement, List<Element>>();

    private final Map<Element, MgnlElement> mgnlElements = new HashMap<Element, MgnlElement>();

    private final Map<MgnlElement, AreaPlaceHolder> areaPlaceHolders = new HashMap<MgnlElement, AreaPlaceHolder>();

    private final Map<MgnlElement, ComponentPlaceHolder> componentPlaceHolders = new HashMap<MgnlElement, ComponentPlaceHolder>();

    private final Map<MgnlElement, AreaEndBar> areaEndBars = new HashMap<MgnlElement, AreaEndBar>();

    public List<MgnlElement> rootElements = new LinkedList<MgnlElement>();

    private MgnlElement selectedMgnlAreaElement = null;

    private MgnlElement selectedMgnlComponentElement = null;

    @Override
    public AbstractBar getPageBar() {
        return pageBar;
    }
    @Override
    public void setPageBar(AbstractBar pageBar) {
        this.pageBar = pageBar;
    }

    private AbstractBar pageBar = null;

    @Override
    public void addOverlay(MgnlElement mgnlElement, AbstractOverlay overlayWidget) {
        overlays.put(mgnlElement, overlayWidget);
    }

    @Override
    public AbstractOverlay getOverlay(MgnlElement mgnlElement) {
        return overlays.get(mgnlElement);
    }

    @Override
    public void addEditBar(MgnlElement mgnlElement, AbstractBar editBar) {
        editBars.put(mgnlElement, editBar);
    }

    @Override
    public AbstractBar getEditBar(MgnlElement mgnlElement) {
        return editBars.get(mgnlElement);
    }

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
        MgnlElement mgnlElement = mgnlElements.get(element);
        while (mgnlElement == null && element.hasParentElement()) {
            element = element.getParentElement();
            mgnlElement = mgnlElements.get(element);
        }
        return mgnlElement;
    }

    @Override
    public List<Element> getElements(MgnlElement mgnlElement) {
        return elements.get(mgnlElement);
    }

    @Override
    public void addRoot(MgnlElement boundary) {
        this.rootElements.add(boundary);
    }

    @Override
    public List<MgnlElement> getRootElements() {
        return rootElements;
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
    public void addAreaPlaceHolder(MgnlElement mgnlElement, AreaPlaceHolder placeHolder) {
        areaPlaceHolders.put(mgnlElement, placeHolder);
    }

    @Override
    public AreaPlaceHolder getAreaPlaceHolder(MgnlElement mgnlElement) {
        return areaPlaceHolders.get(mgnlElement);
    }

    @Override
    public void addComponentPlaceHolder(MgnlElement mgnlElement, ComponentPlaceHolder placeHolder) {
        componentPlaceHolders.put(mgnlElement, placeHolder);
    }

    @Override
    public ComponentPlaceHolder getComponentPlaceHolder(MgnlElement mgnlElement) {
        return componentPlaceHolders.get(mgnlElement);
    }

    @Override
    public void addAreaEndBar(MgnlElement mgnlElement, AreaEndBar areaEndBar) {
        areaEndBars.put(mgnlElement, areaEndBar);
    }

    @Override
    public AreaEndBar getAreaEndBar(MgnlElement mgnlElement) {
        return areaEndBars.get(mgnlElement);
    }

    @Override
    public void removeMgnlElement(MgnlElement mgnlElement) {

        // remove all occurrences of the element
        if (mgnlElements.containsValue(mgnlElement)) {
            while (mgnlElements.values().remove(mgnlElement))
                ;
        }
        elements.remove(mgnlElement);

        // if the element is a root node, add all children to root list
        if (rootElements.contains(mgnlElement)) {
            rootElements.remove(mgnlElement);
            rootElements.addAll(mgnlElement.getChildren());
        }
    }

    @Override
    public MgnlElement findMgnlElementByContentId(String contentId) {
        for (MgnlElement element : elements.keySet()) {
            if (contentId.equals(element.getAttribute("content"))) {
                return element;
            }
        }
        return null;
    }

    @Override
    public MgnlElement getSelectedMgnlComponentElement() {
        return selectedMgnlComponentElement;
    }

    @Override
    public void setSelectedMgnlComponentElement(MgnlElement selectedMgnlComponentElement) {
        this.selectedMgnlComponentElement = selectedMgnlComponentElement;
    }
}
