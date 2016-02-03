/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.form.formsection.widget;

import info.magnolia.ui.vaadin.gwt.client.form.tab.widget.FormTabWidget;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormFieldWrapper;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Layout for the {@link info.magnolia.ui.vaadin.gwt.client.form.widget.FormFieldWrapper} widgets.
 */
public class FormSectionWidget extends FlowPanel {

    private final Map<Widget, FormFieldWrapper> sections = new LinkedHashMap<Widget, FormFieldWrapper>();

    private final Element legend = DOM.createElement("legend");

    private final Element fieldSet = DOM.createElement("fieldset");

    public FormSectionWidget() {
        super();
        getElement().appendChild(fieldSet);
        fieldSet.appendChild(legend);
        fieldSet.appendChild(DOM.createElement("hr"));
    }

    @Override
    public void add(Widget child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Widget w) {
        FormFieldWrapper fieldSection = (w instanceof FormFieldWrapper) ? (FormFieldWrapper) w : sections.get(w);
        if (w != null) {
            sections.remove(w);
            super.remove(fieldSection);
        }
        return false;
    }

    @Override
    public void insert(Widget w, int beforeIndex) {
        if (sections.containsKey(w)) {
            return;
        }
        FormFieldWrapper fieldSection;
        if (!(w instanceof FormFieldWrapper)) {
            fieldSection = new FormFieldWrapper();
            sections.put(w, fieldSection);
            fieldSection.setField(w);
        } else {
            fieldSection = (FormFieldWrapper) w;
        }

        // Patch ComplexPanel's insert logic to keep 0 index for the legend element (element is appended in the constructor).
        // Although fieldset's legend is usually displayed on top of it regardless of its position in the DOM, this was not the case in older versions of Internet Explorer.
        beforeIndex = adjustIndex(fieldSection, beforeIndex);
        fieldSection.removeFromParent();
        getChildren().insert(fieldSection, beforeIndex); // This widget's child collection remains 0-indexed...
        DOM.insertChild(fieldSet, fieldSection.getElement(), beforeIndex + 1); // ... but DOM insertion starts with 1.
        adopt(fieldSection);
    }

    @Override
    public FormTabWidget getParent() {
        final Widget parent = super.getParent();
        if (parent == null || !(parent instanceof FormTabWidget)) {
            return null;
        }
        return (FormTabWidget) super.getParent();
    }

    public void setDescriptionVisible(boolean isAccessible) {
        if (!hasDialogDescriptionHeader()) {
            boolean hasDisplayedFormFieldHelpSection = false;
            for (final FormFieldWrapper fs : sections.values()) {
                if (fs.isDisplayingHelpSection()) {
                    fs.setHelpEnabled(false);
                    hasDisplayedFormFieldHelpSection = true;
                }
            }
            if (hasDisplayedFormFieldHelpSection) {
                return;
            } else if (!isAccessible) {
                isAccessible = true;
            }
        }
        for (final FormFieldWrapper fs : sections.values()) {
            fs.setHelpEnabled(isAccessible);
        }
    }

    private boolean hasDialogDescriptionHeader() {
        Element element = this.getElement();
        while (element != null) {
            if ("dialogDescriptionHeader".equals(element.getAttribute("role"))) {
                return true;
            }
            element = element.getParentElement();
        }
        return false;
    }

    public List<FormFieldWrapper> getFields() {
        return new ArrayList<FormFieldWrapper>(sections.values());
    }

    public void setFieldCaption(Widget widget, String caption) {
        final FormFieldWrapper wrapper = sections.get(widget);
        if (wrapper != null) {
            wrapper.setCaption(caption);
        }
    }

    public void setFieldRequired(Widget fieldWidget, boolean isRequired) {
        final FormFieldWrapper wrapper = sections.get(fieldWidget);
        if (wrapper != null) {
            wrapper.setRequired(isRequired);
        }
    }

    public void setFieldDescription(Widget w, String description) {
        FormFieldWrapper fieldSection = sections.get(w);
        fieldSection.setHelpDescription(description);
    }

    public void setCaption(String caption) {
        legend.setInnerText(caption);
    }

    public void clearError(Widget widget) {
        sections.get(widget).clearError();
    }

    public void setFieldError(Widget widget, String errorMsg) {
        sections.get(widget).showError(errorMsg);
    }

    public void focus(Widget widget) {
        scrollTo(sections.get(widget));
    }

    private void scrollTo(final FormFieldWrapper field) {
        final int top = JQueryWrapper.select(field).position().top();
        JQueryWrapper.select((com.google.gwt.user.client.Element)getElement().getParentElement())
                .animate(300, new AnimationSettings() {
                    {
                        setProperty("scrollTop", top - 30);
                        addCallback(new JQueryCallback() {
                            @Override
                            public void execute(JQueryWrapper query) {
                                new Timer() {
                                    @Override
                                    public void run() {
                                        field.focusField();
                                    }
                                }.schedule(300);
                            }
                        });
                    }
                });
    }

    public void updateFieldSectionWidths(int offsetWidth) {
        final Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            final Widget w = it.next();
            w.setWidth(offsetWidth + "px");
        }

    }
}
