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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.AbstractAppActivity;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinitionImpl;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinitionImpl;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinitionImpl;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinitionImpl;
import info.magnolia.ui.model.builder.FactoryBase;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;


/**
 * Activity for the Dummy app.
 * 
 * @version $Id$
 */
public class ContactsActivity extends AbstractAppActivity<ContactsPresenter> implements ContactsPresenter {

    private ActionbarDefinition actionbarDefinition;

    private final Map<String, ActionbarItemDefinition> itemDefinitions = new LinkedHashMap<String, ActionbarItemDefinition>();

    private ActionFactory actionFactory;

    @Inject
    public ContactsActivity(ContactsView view) {
        super(view);
        view.createActionbar(getActionbarDefinition());
    }

    @Override
    public ContactsPresenter getReference() {
        return this;
    }

    public ActionbarDefinition getActionbarDefinition() {
        if (actionbarDefinition == null) {

            ActionbarDefinitionImpl def = new ActionbarDefinitionImpl();
            def.setName("contactsActionbar");

            ActionbarSectionDefinitionImpl mainSection = new ActionbarSectionDefinitionImpl();
            mainSection.setTitle("Actions");

            // Action group: contact viewing
            ActionbarGroupDefinitionImpl viewGroup = new ActionbarGroupDefinitionImpl();
            viewGroup.setName("viewGroup");

            ActionbarItemDefinition viewAction = addItemDefinition("viewContact", "View contact", "img/actionbar-icons/icon-action-view-tablet.png");
            viewGroup.addItem(viewAction);

            // Action group: contact editing
            ActionbarGroupDefinitionImpl editGroup = new ActionbarGroupDefinitionImpl();
            editGroup.setName("editGroup");

            ActionbarItemDefinitionImpl addAction = (ActionbarItemDefinitionImpl) addItemDefinition(
                "addContact",
                "Add contact",
                "img/actionbar-icons/icon-action-add-tablet.png");
            addAction.setActionDefinition(new AddContactActionDefinition());
            ActionbarItemDefinition editAction = addItemDefinition("editContact", "Edit contact", "img/actionbar-icons/icon-action-edit-tablet.png");
            ActionbarItemDefinition deleteAction = addItemDefinition(
                "deleteContact",
                "Delete contact",
                "img/actionbar-icons/icon-action-delete-tablet.png");

            editGroup.addItem(addAction);
            editGroup.addItem(editAction);
            editGroup.addItem(deleteAction);

            mainSection.addGroup(viewGroup);
            mainSection.addGroup(editGroup);

            ActionbarSectionDefinitionImpl previewSection = new ActionbarSectionDefinitionImpl();
            previewSection.setTitle("Preview");

            def.addSection(mainSection);
            def.addSection(previewSection);
            actionbarDefinition = def;
        }
        return actionbarDefinition;
    }

    private ActionbarItemDefinition addItemDefinition(String name, String label, String icon) {
        ActionbarItemDefinitionImpl itemDefinition = new ActionbarItemDefinitionImpl();
        itemDefinition.setName(name);
        itemDefinition.setLabel(label);
        itemDefinition.setIcon(icon);
        itemDefinitions.put(name, itemDefinition);
        return itemDefinition;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        ActionbarItemDefinition itemDefinition = getActionbarItemDefinition(actionName);
        // String normalizedPath = (workbenchDefinition.getPath() + path).replaceAll("//", "/");
        // Item item =
        // MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getItem(normalizedPath);
        // Action action = actionFactory.createAction(itemDefinition.getActionDefinition());
        Action action = new AddContactAction((AddContactActionDefinition) itemDefinition.getActionDefinition());
        try {
            action.execute();
        }
        catch (ActionExecutionException e) {
            System.err.println("Can't execute action.\n" + e);
        }
    }

    private ActionbarItemDefinition getActionbarItemDefinition(String actionName) {
        return itemDefinitions.get(actionName);
    }

    /**
     * A factory for creating Action objects.
     */
    public class ActionFactory extends FactoryBase<ActionDefinition, Action> {

        @Inject
        public ActionFactory(ComponentProvider componentProvider) {
            super(componentProvider);
            addMapping(AddContactActionDefinition.class, AddContactAction.class);
        }

        public Action createAction(ActionDefinition definition) {
            return create(definition);
        }

    }

    /**
     * The Class AddContactAction.
     */
    public class AddContactAction extends ActionBase<AddContactActionDefinition> {

        /**
         * @param definition
         */
        public AddContactAction(AddContactActionDefinition definition) {
            super(definition);
        }

        @Override
        public void execute() throws ActionExecutionException {
            System.out.println("ADD A NEW CONTACT, PLEASE, PLEASE, PLEASE!!!");
        }

    }

    /**
     * The Class AddContactActionDefinition.
     */
    public class AddContactActionDefinition implements ActionDefinition {

    }
}
