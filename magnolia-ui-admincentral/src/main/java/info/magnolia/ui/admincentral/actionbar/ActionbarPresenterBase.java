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
package info.magnolia.ui.admincentral.actionbar;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionFactory;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.vaadin.server.Resource;

/**
 * ActionbarPresenterBase.
 */
public abstract class ActionbarPresenterBase implements ActionbarView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ActionbarPresenterBase.class);

    private static final String PREVIEW_SECTION_NAME = "preview";

    private ActionbarDefinition definition;

    private ActionbarView actionbar;

    private final EventBus subAppEventBus;

    private ActionFactory<ActionDefinition, Action> actionFactory;

    /**
     * Instantiates a new action bar presenter.
     */
    @Inject
    public ActionbarPresenterBase(EventBus subAppEventBus) {
        this.subAppEventBus = subAppEventBus;
    }

    /**
     * Initializes an actionbar with given definition and returns the view for
     * parent to add it.
     */
    public ActionbarView start(final ActionbarDefinition definition, final ActionFactory<ActionDefinition, Action> actionFactory) {
        this.actionFactory = actionFactory;
        this.definition = definition;
        actionbar = ActionbarBuilder.build(definition);
        actionbar.setListener(this);
        return actionbar;
    }

    public void setPreview(final Resource previewResource) {
        if (previewResource != null) {
            if (!((Actionbar) actionbar).getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionbar.addSection(PREVIEW_SECTION_NAME, "Preview");
            }
            actionbar.setSectionPreview(previewResource, PREVIEW_SECTION_NAME);
        } else {
            if (((Actionbar) actionbar).getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionbar.removeSection(PREVIEW_SECTION_NAME);
            }
        }
    }

    // JUST DELEGATING CONTEXT SENSITIVITY TO WIDGET

    public void enable(String... actionNames) {
        if (actionbar != null) {
            for (String action : actionNames) {
                actionbar.setActionEnabled(action, true);
            }
        }
    }

    public void disable(String... actionNames) {
        if (actionbar != null) {
            for (String action : actionNames) {
                actionbar.setActionEnabled(action, false);
            }
        }
    }

    public void enableGroup(String groupName) {
        if (actionbar != null) {
            actionbar.setGroupEnabled(groupName, true);
        }
    }

    public void disableGroup(String groupName) {
        if (actionbar != null) {
            actionbar.setGroupEnabled(groupName, false);
        }
    }

    public void enableGroup(String groupName, String sectionName) {
        if (actionbar != null) {
            actionbar.setGroupEnabled(groupName, sectionName, true);
        }
    }

    public void disableGroup(String groupName, String sectionName) {
        if (actionbar != null) {
            actionbar.setGroupEnabled(groupName, sectionName, false);
        }
    }

    public void showSection(String... sectionNames) {
        if (actionbar != null) {
            for (String section : sectionNames) {
                actionbar.setSectionVisible(section, true);
            }
        }
    }

    public void hideSection(String... sectionNames) {
        if (actionbar != null) {
            for (String section : sectionNames) {
                actionbar.setSectionVisible(section, false);
            }
        }
    }

    // WIDGET LISTENER
    @Override
    public void onActionbarItemClicked(String actionToken) {
        ActionDefinition actionDefinition = getActionDefinition(actionToken);
        if (actionDefinition != null) {
            subAppEventBus.fireEvent(new ActionbarItemClickedEvent(actionDefinition));
        }
    }

    private ActionDefinition getActionDefinition(String actionToken) {
        final String[] chunks = actionToken.split(":");
        if (chunks.length != 2) {
            log.warn(
                    "Invalid actionToken [{}]: it is expected to be in the form sectionName:actionName. ActionDefintion cannot be retrieved. Please check actionbar definition.", actionToken);
            return null;
        }
        final String sectionName = chunks[0];
        final String actionName = chunks[1];

        for (ActionbarSectionDefinition section : definition.getSections()) {
            if (sectionName.equals(section.getName())) {
                for (ActionbarGroupDefinition group : section.getGroups()) {
                    for (ActionbarItemDefinition action : group.getItems()) {
                        if (actionName.equals(action.getName())) {
                            final ActionDefinition actionDefinition = action.getActionDefinition();
                            if (actionDefinition == null) {
                                log.warn(
                                        "No action definition found for actionToken [{}]. Please check actionbar definition.", actionToken);
                            }
                            return actionDefinition;
                        }
                    }
                }

                break;
            }
        }
        log.warn("No action definition found for actionToken [{}]. Please check actionbar definition.", actionToken);
        return null;
    }

    protected ActionFactory<ActionDefinition, Action> getActionFactory() {
        return actionFactory;
    }

    // DEFAULT ACTION

    /**
     * Gets the default action definition, i.e. finds the first action bar item
     * whose name matches the action bar definition's 'defaultAction' property,
     * and returns its actionDefinition property.
     */
    public ActionDefinition getDefaultActionDefinition() {
        String defaultAction = definition.getDefaultAction();
        if (StringUtils.isBlank(defaultAction)) {
            log.warn("Default action is null. Please check actionbar definition.");
            return null;
        }

        // considering actionbar item name unique, returning first match
        if (!definition.getSections().isEmpty()) {
            for (ActionbarSectionDefinition sectionDef : definition.getSections()) {
                if (actionbar.isSectionVisible(sectionDef.getName()) && !sectionDef.getGroups().isEmpty()) {
                    for (ActionbarGroupDefinition groupDef : sectionDef.getGroups()) {
                        if (!groupDef.getItems().isEmpty()) {
                            for (ActionbarItemDefinition action : groupDef.getItems()) {
                                if (action.getName().equals(defaultAction)) {
                                    final ActionDefinition actionDefinition = action.getActionDefinition();
                                    if (actionDefinition == null) {
                                        log.warn(
                                                "No action definition found for default action [{}]. Please check actionbar definition.", defaultAction);
                                    }
                                    return actionDefinition;
                                }
                            }
                        }
                    }
                }
            }
        }
        log.warn("No action definition found for default action [{}]. Please check actionbar definition.", defaultAction);
        return null;
    }
}
