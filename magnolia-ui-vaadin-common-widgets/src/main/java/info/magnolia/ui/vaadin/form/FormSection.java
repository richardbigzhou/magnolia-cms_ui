/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.form;

import info.magnolia.ui.vaadin.gwt.client.form.formsection.connector.FormSectionState;
import info.magnolia.ui.vaadin.gwt.client.form.rpc.FormSectionClientRpc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;

/**
 * Form layout server side implementation.
 */
public class FormSection extends AbstractLayout {

    private final List<Component> components = new LinkedList<Component>();

    private String name;

    public FormSection() {
        addStyleName("v-form-layout");
    }

    @Override
    protected FormSectionState getState() {
        return (FormSectionState) super.getState();
    }

    @Override
    protected FormSectionState getState(boolean markAsDirty) {
        return (FormSectionState) super.getState(markAsDirty);
    }

    public void setComponentHelpDescription(Component c, String description) {
        if (components.contains(c)) {
            getState().helpDescriptions.put(c, description);
        } else {
            throw new IllegalArgumentException("Layout doesn't contain this component.");
        }
    }

    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        components.add(c);
        c.addStyleName("v-form-field");
        markAsDirty();
    }

    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        components.remove(c);
        markAsDirty();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
    }

    @Override
    public Iterator<Component> iterator() {
        return components.iterator();
    }

    public void setValidationVisible(boolean isVisible) {
        getState().isValidationVisible = isVisible;
    }

    @Override
    public ErrorMessage getErrorMessage() {
        if (!getState(false).isValidationVisible) {
            return null;
        }
        final Iterator<Component> it = getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            if (c instanceof AbstractComponent) {
                final ErrorMessage errMsg = ((AbstractComponent) c).getErrorMessage();
                if (errMsg != null) {
                    return errMsg;
                }
            }
        }
        return super.getErrorMessage();
    }

    @Override
    public int getComponentCount() {
        return components.size();
    }

    public Component getNextProblematicField(Connector currentFocused) {
        int startIndex = components.indexOf(currentFocused) + 1;
        if (startIndex < components.size() - 1) {
            while (startIndex < components.size()) {
                Component c = components.get(startIndex++);
                if (c instanceof AbstractField && !((AbstractField<?>) c).isValid()) {
                    return c;
                }
            }
        }
        return null;
    }

    public void focusField(Component field) {
        getRpcProxy(FormSectionClientRpc.class).focus(field);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
