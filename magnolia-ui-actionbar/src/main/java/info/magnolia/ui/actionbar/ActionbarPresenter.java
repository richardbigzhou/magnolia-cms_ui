/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.actionbar;

import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Resource;

/**
 * Default presenter for an action bar.
 */
public class ActionbarPresenter implements ActionbarView.Listener {

    /**
     * Listener interface for the Actionbar.
     */
    public interface Listener {

        void onActionbarItemClicked(String itemName);
    }

    private static final Logger log = LoggerFactory.getLogger(ActionbarPresenter.class);

    private ActionbarDefinition definition;

    private Map<String, ActionDefinition> actions;

    private ActionbarView view;

    private Listener listener;

    @Inject
    public ActionbarPresenter(ActionbarView view) {
        this.view = view;
        view.setListener(this);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Initializes an action bar with the given definition and returns the view for the parent to add it.
     */
    public ActionbarView start(ActionbarDefinition definition, Map<String, ActionDefinition> actions) {
        this.definition = definition;
        this.actions = actions;

        if (definition != null) {

            // build action bar structure from definition
            for (ActionbarSectionDefinition section : definition.getSections()) {
                view.addSection(section.getName(), section.getLabel());
                List<String> actionNames = new ArrayList<String>();

                for (ActionbarGroupDefinition group : section.getGroups()) {
                    // standalone groups make no sense
                    log.debug("Group actions: {}", group.getItems());

                    for (ActionbarItemDefinition action : group.getItems()) {
                        if (actionNames.contains(action.getName())) {
                            log.warn("Action was not added: an action with name '{}' was already added to the section '{}'.", action.getName(), section.getName());
                            continue;
                        }
                        actionNames.add(action.getName());
                        addActionItem(action.getName(), group.getName(), section.getName());
                    }
                }
            }
        } else {
            log.debug("No actionbar definition found. This will result in an empty action bar. Is that intended?");
        }
        return view;
    }

    private void addActionItem(String actionName, String groupName, String sectionName) {

        ActionDefinition actionDefinition = actions.get(actionName);
        if (actionDefinition != null) {
            String label = actionDefinition.getLabel();
            if (StringUtils.isBlank(label)) {
                label = actionName;
            }
            String icon = actionDefinition.getIcon();

            // only icons from icon-fonts currently work
            ActionbarItem item = new ActionbarItem(actionName, label, icon, groupName);
            view.addAction(item, sectionName);
        }
    }

    public void setPreview(final Resource previewResource) {
        view.setPreview(previewResource);
    }

    // METHODS DELEGATING TO THE VIEW

    public void enable(String... actionNames) {
        if (view != null) {
            for (String action : actionNames) {
                view.setActionEnabled(action, true);
            }
        }
    }

    public void disable(String... actionNames) {
        if (view != null) {
            for (String action : actionNames) {
                view.setActionEnabled(action, false);
            }
        }
    }

    public void enableGroup(String groupName) {
        if (view != null) {
            view.setGroupEnabled(groupName, true);
        }
    }

    public void disableGroup(String groupName) {
        if (view != null) {
            view.setGroupEnabled(groupName, false);
        }
    }

    public void enableGroup(String groupName, String sectionName) {
        if (view != null) {
            view.setGroupEnabled(groupName, sectionName, true);
        }
    }

    public void disableGroup(String groupName, String sectionName) {
        if (view != null) {
            view.setGroupEnabled(groupName, sectionName, false);
        }
    }

    public void showSection(String... sectionNames) {
        if (view != null) {
            for (String section : sectionNames) {
                view.setSectionVisible(section, true);
            }
        }
    }

    public void hideSection(String... sectionNames) {
        if (view != null) {
            for (String section : sectionNames) {
                view.setSectionVisible(section, false);
            }
        }
    }

    // VIEW LISTENER

    @Override
    public void onActionbarItemClicked(String actionToken) {
        String actionName = getActionName(actionToken);
        listener.onActionbarItemClicked(actionName);
    }

    private String getActionName(String actionToken) {
        final String[] chunks = actionToken.split(":");
        if (chunks.length != 2) {
            log.warn("Invalid actionToken [{}]: it is expected to be in the form sectionName:actionName. Action name cannot be resolved. Please check actionbar definition.", actionToken);
            return null;
        }
        final String sectionName = chunks[0];
        final String actionName = chunks[1];

        return actionName;
    }
}
