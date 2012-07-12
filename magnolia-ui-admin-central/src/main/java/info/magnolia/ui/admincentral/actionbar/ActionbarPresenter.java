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
package info.magnolia.ui.admincentral.actionbar;

import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.model.action.AbstractActionFactory;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import com.google.inject.Inject;


/**
 * Default presenter for an action bar.
 */
public class ActionbarPresenter implements ActionbarView.Listener {

    private final ActionbarDefinition definition;

    private final AbstractActionFactory<ActionDefinition, Action> actionFactory;

    private final ActionbarView actionbar;

    /**
     * Instantiates a new action bar presenter.
     */
    @Inject
    public ActionbarPresenter(ActionbarDefinition definition, AbstractActionFactory<ActionDefinition, Action> actionFactory) {
        this.definition = definition;
        this.actionFactory = actionFactory;
        actionbar = ActionbarBuilder.build(definition, this);
    }

    @Override
    public ActionbarView getView() {
        return actionbar;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        ActionDefinition actionDefinition = getActionDefinition(actionName);
        if (actionDefinition != null) {
            Action action = actionFactory.createAction(actionDefinition);
        }
        // somehow return action to subApp level so that it can execute it with content view params
    }

    private ActionDefinition getActionDefinition(String actionName) {
        for (ActionbarSectionDefinition section : definition.getSections()) {
            for (ActionbarGroupDefinition group : section.getGroups()) {
                for (ActionbarItemDefinition action : group.getItems()) {
                    if (actionName.equals(action.getName())) {
                        return action.getActionDefinition();
                    }
                }
            }
        }
        return null;
    }

}
