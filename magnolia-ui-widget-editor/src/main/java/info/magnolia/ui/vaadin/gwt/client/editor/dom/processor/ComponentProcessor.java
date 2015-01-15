/**
 * This file Copyright (c) 2011-2015 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar;

import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * Processor for {@link MgnlComponent}s.
 *
 * @see ComponentBar
 */
public class ComponentProcessor extends AbstractMgnlElementProcessor {

    public ComponentProcessor(Model model, MgnlComponent component) {
        super(model, component);
    }

    @Override
    public void process() {
        if (hasControlBar(getMgnlElement().getAttributes())) {
            GWT.log("Component has edit bar. Injecting it..");
            ComponentBar editBarWidget = new ComponentBar(getMgnlElement());
            setEditBar(editBarWidget);
            attachWidget();
        } else {
            GWT.log("Component is inherited or not editable. Skipping..");
        }
    }

    private boolean hasControlBar(Map<String, String> attributes) {

        boolean isInherited = getMgnlElement().isInherited();
        boolean editable = true;

        if (attributes.containsKey("editable")) {
            editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        if (isInherited || !editable) {
            return false;
        }
        
        return true;
    }

    @Override
    public MgnlComponent getMgnlElement() {
        return (MgnlComponent) super.getMgnlElement();
    }
}
