/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.event.FrameNavigationEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;

import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Processor for {@link com.google.gwt.user.client.DOM} {@link Element}s. This is a crucial part of the page editor as it:
 * <pre>
 *  <ul>
 *      <li>Creates a mapping between the {@link Element} of the DOM and {@link MgnlElement}. Used e.g. in {@link info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel} to map elements to the right area or component.</li>
 *      <li>Looks for certain markers in the attributes which will help inject the control bars at the right spot.</li>
 *      <li>
 *          Modifies links based on their function inside the page:
 *          <ul>
 *              <li>A link inside the navigation will be overwritten by an onclick method.</li>
 *              <li>Normal links will simply be disabled.</li>
 *          </ul>
 *      </li>
 *  </ul>
 * </pre>
 */
public class ElementProcessor {

    private static final String NAVIGATION_ROLE = "navigation";
    private static final String ATTRIBUTE_ROLE = "role";
    private final EventBus eventBus;
    private final Model model;

    public ElementProcessor(EventBus eventBus, Model model) {

        this.eventBus = eventBus;
        this.model = model;
    }

    /**
     * Processes the current {@link Element}.
     *
     * @param element the current node beeing processed
     * @param mgnlElement the associated {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement}
     * @param preview
     */
    public void process(Element element, MgnlElement mgnlElement, boolean preview) {

        if (element.hasTagName("A")) {

            if (preview || isNavigation(element)) {
                registerOnclick(element);
            } else {
                disableLink(element);
                removeHover(element);
            }
        }

        if (mgnlElement == null || mgnlElement.isPage()) {
            return;
        }

        model.addElement(mgnlElement, element);
        if (mgnlElement instanceof MgnlArea) {
            MgnlArea area = (MgnlArea) mgnlElement;

            if (element.hasAttribute(AreaDefinition.CMS_ADD)) {
                area.setComponentMarkerElement(element);
                return;
            }
        }
        if (element.hasAttribute(AreaDefinition.CMS_EDIT)) {
            mgnlElement.setEditElement(element);
        }
        else {
            if (mgnlElement.getFirstElement() == null) {
                mgnlElement.setFirstElement(element);
            }

            if (mgnlElement.getLastElement() == null || !mgnlElement.getLastElement().isOrHasChild(element)) {
                mgnlElement.setLastElement(element);
            }
        }


    }

    /**
     * Fires a {@link FrameNavigationEvent} to the eventBus.
     *
     * @see info.magnolia.ui.vaadin.gwt.client.connector.PageEditorConnector#init()
     */
    private void navigate(String path) {
        eventBus.fireEvent(new FrameNavigationEvent(path));
    }

    /**
     * JSNI method which registers an onclick method which will call {@link #navigate(String)}.
     * Extracts the href attribute from the {@link Element} and passes it as a parameter.
     * By returning false the browser won't follow the link.
     */
    private native void registerOnclick(Element element) /*-{
        var that = this;
        var path = element.href;
        if (element.onclick == null) {
            element.onclick = $entry(function () {
                that.@info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.ElementProcessor::navigate(Ljava/lang/String;)(path);
                return false;
            });
        }
    }-*/;

    private void removeHover(Element element) {
        element.addClassName("disabled");
    }

    private native static void disableLink(Element element) /*-{
        if (element.onclick == null) {
            element.onclick = function() {
                return false;
            };
        }
    }-*/;

    /**
     * Searches for an element with attribute {@link #ATTRIBUTE_ROLE} defined as {@link #NAVIGATION_ROLE} on the element itself and it's ancestors.
     */
    private boolean isNavigation(Element element) {
        if (element == null) {
            return false;
        }
        if (element.getAttribute(ATTRIBUTE_ROLE).equals(NAVIGATION_ROLE)) {
            return true;
        }
        return isNavigation(element.getParentElement());
    }

}
