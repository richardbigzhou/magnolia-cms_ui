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
package info.magnolia.ui.vaadin.gwt.client.form.tab.widget;

import info.magnolia.ui.vaadin.gwt.client.form.formsection.widget.FormSectionWidget;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormFieldWrapper;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.connector.MagnoliaTabConnector;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget.MagnoliaTabWidget;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;

/**
 * An extension of {@link MagnoliaTabWidget}. Its content type is restricted to
 * {@link FormSectionWidget} and the FielWrappers are exposed.
 */
public class FormTabWidget extends MagnoliaTabWidget {

    public FormTabWidget(MagnoliaTabConnector connector) {
        super(connector);
    }

    private FormSectionWidget content;

    @Override
    public void setWidget(Widget w) {
        if (!(w instanceof FormSectionWidget)) {
            throw new RuntimeException("Invalid type of tab content. Must be VFormSection. You have used: " + w.getClass());  //TODO-TRANSLATE-EXCEPTION
        }
        content = (FormSectionWidget) w;
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

}
