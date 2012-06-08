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

import info.magnolia.ui.admincentral.app.AbstractAppView;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.widget.actionbar.ActionButton;
import info.magnolia.ui.widget.actionbar.Actionbar;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * View implementation for the Contacts app.
 * 
 * @version $Id$
 */
@SuppressWarnings("serial")
public class ContactsViewImpl extends AbstractAppView<ContactsPresenter> implements ContactsView {

    private final Actionbar actionbar = new Actionbar();

    public ContactsViewImpl() {
        super();

        final HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setWidth("100%");

        final VerticalLayout tableContainer = new VerticalLayout();
        Label label = new Label("<center>Contacts App</center>", Label.CONTENT_XHTML);
        tableContainer.addComponent(label);

        wrapper.addComponent(tableContainer);
        wrapper.setExpandRatio(tableContainer, 1.0f);

        wrapper.addComponent(actionbar);

        addTab(wrapper, "Contacts");
    }

    @Override
    public void createActionbar(ActionbarDefinition actionbarDefinition) {

        for (ActionbarSectionDefinition section : actionbarDefinition.getSections()) {
            for (ActionbarGroupDefinition group : section.getGroups()) {
                for (ActionbarItemDefinition item : group.getItems()) {

                    ActionButton button = new ActionButton(item.getLabel());
                    button.setIcon(new ThemeResource(item.getIcon()));

                    final String actionName = item.getName();
                    button.setActionName(actionName);
                    button.setGroupName(group.getName());
                    button.setSectionTitle(section.getTitle());

                    button.addListener(new ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                            getPresenter().onActionbarItemClicked(actionName);
                        }
                    });
                    actionbar.addComponent(button);
                }
            }
        }

        // actionbar.setDefinition(actionbarDefinition);

        // actionbar.addSection("actions", "Actions");
        // actionbar.addGroup("group1", "actions");
        // actionbar.addGroup("group2", "actions");
        // actionbar.addGroup("group3", "actions");
        // actionbar.addAction()

    }
}
