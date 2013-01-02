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
package info.magnolia.ui.vaadin.gwt.client.editor.dom.processor;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.editor.widget.controlbar.AbstractBar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventBus;

/**
 * Abstract Class for MgnlElement processors.
 */
public abstract class AbstractMgnlElementProcessor {

    private MgnlElement mgnlElement;

    private Model model;
    private EventBus eventBus;

    private AbstractBar editBar;

    public AbstractMgnlElementProcessor(Model model, EventBus eventBus, MgnlElement mgnlElement) {
        this.model = model;
        this.eventBus = eventBus;
        this.setMgnlElement(mgnlElement);
    }

    public abstract void process();

    protected void attachWidget() {
        attach();
        addToModel();
    }


    public void attach() {
        // if there is a marker for the controlBar insert here
        if (getMgnlElement().getEditElement() != null) {
            Element parent = getMgnlElement().getEditElement();
            parent.insertFirst(getEditBar().getElement());
        }
        else if (getMgnlElement().getFirstElement() != null && getMgnlElement().getFirstElement() == getMgnlElement().getLastElement()) {
            attach(getMgnlElement());
        }
        else {
            String nodeData = getMgnlElement().getStartComment().getNodeValue();

            attach(getMgnlElement().getStartComment());
        }
        getEditBar().onAttach();
    }

    public void attach(MgnlElement mgnlElement) {
        Element element = mgnlElement.getFirstElement();
        if (element != null) {
            if(element.hasTagName("DIV")){
                element.insertFirst(getEditBar().getElement());
            }else{
                final Node parentNode = element.getParentNode();
                parentNode.insertBefore(getEditBar().getElement(), element);
            }
        }
    }

    public void attach(Element element) {
        Node parentNode = element.getParentNode();
        parentNode.insertAfter(getEditBar().getElement(), element);
    }

    protected void addToModel() {
        getModel().addElements(getMgnlElement(), getEditBar().getElement());
        getMgnlElement().setControlBar(getEditBar());
    }

    public void setMgnlElement(MgnlElement mgnlElement) {
        this.mgnlElement = mgnlElement;
    }

    public MgnlElement getMgnlElement() {
        return mgnlElement;
    }

    public Model getModel() {
        return model;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    protected void setEditBar(AbstractBar editBar) {
        this.editBar = editBar;
    }

    public AbstractBar getEditBar() {
        return editBar;
    }

}
