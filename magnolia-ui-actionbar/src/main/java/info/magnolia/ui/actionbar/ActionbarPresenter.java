/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.actionbar.builder.ActionbarFactory;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import javax.inject.Inject;

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

        String getLabel(String itemName);

        String getIcon(String itemName);

    }

    private static final Logger log = LoggerFactory.getLogger(ActionbarPresenter.class);

    private static final String PREVIEW_SECTION_NAME = "preview";

    private ActionbarDefinition definition;

    private ActionbarView actionbar;

    private Listener listener;

    private SimpleTranslator translator;

    @Inject
    public ActionbarPresenter(SimpleTranslator translator) {
        this.translator = translator;
    }

    /**
     * @deprecated since 5.2.1 - please use {@link #ActionbarPresenter(SimpleTranslator)}.
     */
    public ActionbarPresenter() {
        this.translator = Components.getComponent(SimpleTranslator.class);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Initializes an actionbar with given definition and returns the view for
     * parent to add it.
     */
    public ActionbarView start(final ActionbarDefinition definition) {
        this.definition = definition;
        actionbar = ActionbarFactory.build(definition, listener);
        actionbar.setListener(this);
        return actionbar;
    }

    public void setPreview(final Resource previewResource) {
        if (previewResource != null) {
            if (!((Actionbar) actionbar).getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionbar.addSection(PREVIEW_SECTION_NAME, translator.translate("actionbar.preview"));
            }
            actionbar.setSectionPreview(previewResource, PREVIEW_SECTION_NAME);
        } else {
            if (((Actionbar) actionbar).getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionbar.removeSection(PREVIEW_SECTION_NAME);
            }
        }
    }

    // METHODS DELEGATING TO THE VIEW

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
