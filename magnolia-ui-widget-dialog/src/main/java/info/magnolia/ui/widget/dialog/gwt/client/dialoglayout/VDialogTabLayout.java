/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.dialog.gwt.client.dialoglayout;

import info.magnolia.ui.widget.dialog.gwt.client.VDialog;
import info.magnolia.ui.widget.dialog.gwt.client.VDialogTab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * Layout for the {@link DialogFieldWrapper} widgets.
 */
public class VDialogTabLayout extends FlowPanel implements Container, HelpAccessibilityEvent.Handler {

    private List<Widget> children = new LinkedList<Widget>();

    private Map<Widget, DialogFieldWrapper> sections = new LinkedHashMap<Widget, DialogFieldWrapper>();

    private List<DialogFieldWrapper> problematicSections = new ArrayList<DialogFieldWrapper>();
    
    private HandlerRegistration helpEventRegistration = null;

    private Element fieldSet = DOM.createElement("fieldset");
    
    private boolean isValidationVisible = false;
    
    public VDialogTabLayout() {
        super();
        getElement().appendChild(fieldSet);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        
        isValidationVisible = uidl.getBooleanAttribute("validationVisible");
        final Iterator<?> it = uidl.getChildIterator();
        while (it.hasNext()) {
            final UIDL childUIdl = (UIDL) it.next();
            final Paintable p = client.getPaintable(childUIdl.getChildUIDL(0));
            final Widget w = (Widget) p;
            if (!hasChildComponent(w)) {
                DialogFieldWrapper fieldSection = new DialogFieldWrapper();
                sections.put(w, fieldSection);
                children.add(w);
                fieldSection.setField(w);
                add(fieldSection, fieldSet);
            }
            if (childUIdl.hasAttribute("helpDescription")) {
                String description = childUIdl.getStringAttribute("helpDescription");
                DialogFieldWrapper fieldSection = sections.get(w);
                fieldSection.setHelpDescription(description);
            }
            p.updateFromUIDL(childUIdl.getChildUIDL(0), client);
            /**
             * TODO: Implement ALL the details of Paintable handling here.
             */
        }

        if (helpEventRegistration == null) {
            final VDialog dialog = Util.findWidget(getElement(), VDialog.class);
            if (dialog != null) {
                helpEventRegistration = dialog.addHelpAccessibilityHandler(this);
            }
        }
    }
    
    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public boolean hasChildComponent(Widget component) {
        return children.contains(component);
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
        DialogFieldWrapper fs = sections.get(component);
        if (fs != null) {
            boolean errorsOccured = uidl.hasAttribute("error");
            if (errorsOccured && isValidationVisible) {
                for (final Iterator<?> it = uidl.getErrors().getChildIterator(); it.hasNext();) {
                    final Object child = it.next();
                    if (child instanceof String) {
                        final String errorMessage = (String) child;
                        fs.showError(errorMessage);
                        if (!problematicSections.contains(fs)) {
                            problematicSections.add(fs);
                        }
                        break;
                    }
                }
            } else {
                problematicSections.remove(fs);
                fs.clearErrors();
            }
            getParent().setHasError(!problematicSections.isEmpty());
            if (uidl.hasAttribute("caption")) {
                fs.setCaption(uidl.getStringAttribute("caption"));
            }
        }
    }
    
    @Override
    public VDialogTab getParent() {
        final Widget parent = super.getParent();
        if (parent == null) {
            return null;
        }
        if (!(super.getParent() instanceof VDialogTab)) {
            throw new RuntimeException("Parent of VDialogTabLayout must be of type VDialogTab, you have used: " + super.getParent().getClass());
        }
        return (VDialogTab)super.getParent();
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        final DialogFieldWrapper fs = sections.get(child);
        if (fs != null) {
            return new RenderSpace(fs.getFieldAreaWidth(), fs.getFieldAreaHeight());
        }
        return new RenderSpace();
    }

    @Override
    public void onHelpAccessibilityChanged(HelpAccessibilityEvent event) {
        for (final DialogFieldWrapper fs : sections.values()) {
            fs.setHelpEnabled(event.isHelpAccesible());
        }
    }
    
    public void setDescriptionVisible(boolean isAccessible) {
        for (final DialogFieldWrapper fs : sections.values()) {
            fs.setHelpEnabled(isAccessible);
        }        
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }
    
    public void setValidationVisible(boolean isVisible) {
        this.isValidationVisible = isVisible;
    }
}
