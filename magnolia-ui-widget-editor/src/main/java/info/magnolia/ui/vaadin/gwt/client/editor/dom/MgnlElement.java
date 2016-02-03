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
package info.magnolia.ui.vaadin.gwt.client.editor.dom;

import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.AbstractBar;

import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * Extends the {@link CmsNode} by objects used for positioning the associated {@link AbstractBar} inside the DOM structure.
 * Has a map with all attributes extracted from the {@link Comment}.
 *
 * @see info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.AbstractMgnlElementProcessor
 */
public abstract class MgnlElement extends CmsNode {

    private Map<String, String> attributes;

    private Element startComment;
    private Element endComment;

    private Element firstElement;
    private Element lastElement;

    private Element editElement;
    private AbstractBar controlBar;

    /**
     * MgnlElement. Represents a node in the tree built on cms-tags.
     */
    public MgnlElement(MgnlElement parent) {
        super(parent);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    protected AbstractBar getControlBar() {
        return controlBar;
    }

    public void setControlBar(AbstractBar controlBar) {
        this.controlBar = controlBar;
    }

    public Element getFirstElement() {
        return firstElement;
    }

    public void setFirstElement(Element firstElement) {
        this.firstElement = firstElement;
    }

    public Element getLastElement() {
        return lastElement;
    }

    public void setLastElement(Element lastElement) {
        this.lastElement = lastElement;
    }

    public void setEditElement(Element editElement) {
        this.editElement = editElement;
    }

    public Element getEditElement() {
        return editElement;
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public boolean containsAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    public boolean isInherited() {
        return Boolean.parseBoolean(getAttribute("inherited")) || (getParent() != null && ((MgnlElement) getParent()).isInherited());
    }

    public Element getStartComment() {
        return startComment;
    }

    public Element getEndComment() {
        return this.endComment;
    }

    public void setStartComment(Element element) {
        this.startComment = element;
    }

    public void setEndComment(Element element) {
        this.endComment = element;
    }

    public abstract AbstractElement getTypedElement();

    public boolean isPage() {
        return this instanceof MgnlPage;
    }

    public boolean isArea() {
        return this instanceof MgnlArea;
    }

    public boolean isComponent(){
        return this instanceof MgnlComponent;
    }

}
