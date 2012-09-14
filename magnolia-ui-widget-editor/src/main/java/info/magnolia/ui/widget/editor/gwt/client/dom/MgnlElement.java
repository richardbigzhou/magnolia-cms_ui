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
package info.magnolia.ui.widget.editor.gwt.client.dom;

import com.google.gwt.dom.client.Element;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AbstractBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.placeholder.ComponentPlaceHolder;

import java.util.Map;

/**
* MgnlElement Constructor.
*
*/
public class MgnlElement extends CmsNode {

    private CMSComment comment;

    private Element firstElement;

    private Element lastElement;
    private Element componentElement;
    private Element areaElement;
    private Element editElement;

    private AbstractBar controlBar;

    // only used in areas
    ComponentPlaceHolder componentPlaceHolder;
    AreaEndBar areaEndBar;

    private CMSComment endComment;

    private Map<String, String> attributes;

    /**
 * MgnlElement. Represents a node in the tree built on cms-tags.
 */
    public MgnlElement(CMSComment comment, MgnlElement parent) {
        super(parent);

        this.comment = comment;

        this.attributes = comment.getAttributes();

        if (this.parent != null) {
            for (String inheritedAttribute : INHERITED_ATTRIBUTES) {
                if (getParent().asMgnlElement().containsAttribute(inheritedAttribute)) {
                    attributes.put(inheritedAttribute, getParent().asMgnlElement().getAttribute(inheritedAttribute));
                }
            }
        }
    }

    private static final String[] INHERITED_ATTRIBUTES = {"editable"};

    public AbstractBar getControlBar() {
        return controlBar;
    }

    public void setControlBar(AbstractBar controlBar) {
        this.controlBar = controlBar;
    }
    public ComponentPlaceHolder getComponentPlaceHolder() {
        return componentPlaceHolder;
    }

    public void setComponentPlaceHolder(ComponentPlaceHolder componentPlaceHolder) {
        this.componentPlaceHolder = componentPlaceHolder;
    }

    public AreaEndBar getAreaEndBar() {
        return areaEndBar;
    }

    public void setAreaEndBar(AreaEndBar areaEndBar) {
        this.areaEndBar = areaEndBar;
    }


    /*    public MgnlElement getRootArea() {
        MgnlElement rootArea = null;
        for (MgnlElement parent = this; parent != null; parent = parent.getParent()) {
            if (parent.isArea()) {
                rootArea = parent;
            }
        }
        return rootArea;
    }*/

    @Deprecated
    public CMSComment getComment() {
        return comment;
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

    public void setComponentElement(Element componentElement) {
        this.componentElement = componentElement;
    }

    public void setAreaElement(Element areaElement) {
        this.areaElement = areaElement;
    }

    public void setEditElement(Element editElement) {
        this.editElement = editElement;
    }

    public Element getComponentElement() {
        return componentElement;
    }

    public Element getAreaElement() {
        return areaElement;
    }

    public Element getEditElement() {
        return editElement;
    }

    public void setEndComment(CMSComment endComment) {
        this.endComment = endComment;
    }

    public CMSComment getEndComment() {
        return this.endComment;
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public boolean containsAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }
    @Override
    public String toString() {
        return comment.toString();
    }

}
