/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.contacts;

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.actionbar.Actionbar;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * View implementation for the Contacts app.
 *
 * @version $Id$
 */
@SuppressWarnings("serial")
public class ContactsViewImpl implements ContactsView, IsVaadinComponent {

    private final HorizontalLayout wrapper;

    public ContactsViewImpl() {
        super();

        wrapper = new HorizontalLayout();
        wrapper.setWidth("100%");

        final VerticalLayout tableContainer = new VerticalLayout();
        Label label = new Label("<center>Contacts App</center>", Label.CONTENT_XHTML);
        tableContainer.addComponent(label);

        wrapper.addComponent(tableContainer);
        wrapper.setExpandRatio(tableContainer, 1.0f);

        wrapper.addComponent(createActionbar());
    }

    private Actionbar createActionbar() {
        Actionbar actionbar = new Actionbar();

        // actionbar.addSection("actions", "Actions");
        // actionbar.addGroup("group1", "actions");
        // actionbar.addGroup("group2", "actions");
        // actionbar.addGroup("group3", "actions");
        // actionbar.addAction()

        return actionbar;
    }

    @Override
    public String getCaption() {
        return "Contacts";
    }

    @Override
    public Component asVaadinComponent() {
        return wrapper;
    }
}
