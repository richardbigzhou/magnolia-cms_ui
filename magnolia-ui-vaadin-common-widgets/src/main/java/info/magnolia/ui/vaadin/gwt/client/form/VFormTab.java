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
package info.magnolia.ui.vaadin.gwt.client.form;

import info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout.FormFieldWrapper;
import info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout.VFormSection;
import info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout.ValidationChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout.ValidationChangedEvent.Handler;
import info.magnolia.ui.vaadin.gwt.client.dialog.dialoglayout.ValidationChangedEvent.HasValidationChangeHanlders;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.VMagnoliaTab;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;


/**
 * VFormTab.
 */
public class VFormTab extends VMagnoliaTab implements HasValidationChangeHanlders {

    private VFormSection content;

    @Override
    public void setWidget(Widget w) {
        if (!(w instanceof VFormSection)) {
            throw new RuntimeException("Invalid type of tab content. Must be VDialogLayout. You have used: " + w.getClass());
        }
        content = (VFormSection) w;
        super.setWidget(w);
    }

    public List<FormFieldWrapper> getFields() {
        if (content != null) {
            return content.getFields();
        }
        return new LinkedList<FormFieldWrapper>();
    }

    public void setDescriptionVisible(boolean visible) {
        if (content != null) {
            content.setDescriptionVisible(visible);
        }
    }

    public void setValidationVisible(boolean isVisible) {
        if (content != null) {
            content.setValidationVisible(isVisible);
        }
    }

    @Override
    public void setHasError(boolean hasError) {
        super.setHasError(hasError);
        fireEvent(new ValidationChangedEvent());
    }

    public int getErrorAmount() {
        return content.getErrorAmount();
    }

    public List<FormFieldWrapper> getProblematicFields() {
        return content.getProblematicFields();
    }

    @Override
    public HandlerRegistration addValidationChangeHandler(Handler handler) {
        return addHandler(handler, ValidationChangedEvent.TYPE);
    }

}
