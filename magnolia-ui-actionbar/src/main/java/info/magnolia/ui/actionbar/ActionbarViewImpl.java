/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.actionbar.Actionbar.ActionTriggerEvent;
import info.magnolia.ui.vaadin.actionbar.Actionbar.ActionTriggerListener;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import javax.inject.Inject;

import com.vaadin.server.Resource;

/**
 * Default Vaadin implementation of the action bar view.
 */
public class ActionbarViewImpl implements ActionbarView {

    private static final String PREVIEW_SECTION_NAME = "preview";

    private Actionbar actionBar = new Actionbar();

    private ActionbarView.Listener listener;

    private SimpleTranslator i18n;

    @Inject
    public ActionbarViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;
        actionBar.addActionTriggerListener(new ActionTriggerListener() {

            @Override
            public void actionTrigger(ActionTriggerEvent event) {
                if (listener != null) {
                    listener.onActionbarItemClicked(event.getActionName());
                }
            }
        });
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void addSection(String sectionName, String label) {
        actionBar.addSection(sectionName, label);
    }

    @Override
    public void removeSection(String sectionName) {
        actionBar.removeSection(sectionName);
    }

    @Override
    public void setPreview(Resource previewResource) {
        if (previewResource != null) {
            if (!actionBar.getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionBar.addSection(PREVIEW_SECTION_NAME, i18n.translate("actionbar.preview"));

            }
            actionBar.setSectionPreview(previewResource, PREVIEW_SECTION_NAME);
        } else {
            if (actionBar.getSections().containsKey(PREVIEW_SECTION_NAME)) {
                actionBar.removeSection(PREVIEW_SECTION_NAME);
            }
        }
    }

    @Override
    public void addAction(ActionbarItem action, String sectionName) {
        actionBar.addAction(action, sectionName);
    }

    @Override
    public void removeAction(String actionName) {
        actionBar.removeAction(actionName);
    }

    @Override
    public void setActionEnabled(String actionName, boolean isEnabled) {
        actionBar.setActionEnabled(actionName, isEnabled);
    }

    @Override
    public void setActionEnabled(String actionName, String sectionName, boolean isEnabled) {
        actionBar.setActionEnabled(sectionName, actionName, isEnabled);
    }

    @Override
    public void setGroupEnabled(String groupName, boolean isEnabled) {
        actionBar.setGroupEnabled(groupName, isEnabled);
    }

    @Override
    public void setGroupEnabled(String groupName, String sectionName, boolean isEnabled) {
        actionBar.setGroupEnabled(groupName, sectionName, isEnabled);
    }

    @Override
    public void setSectionVisible(String sectionName, boolean isVisible) {
        actionBar.setSectionVisible(sectionName, isVisible);
    }

    @Override
    public boolean isSectionVisible(String sectionName) {
        return actionBar.isSectionVisible(sectionName);
    }

    @Override
    public void setOpen(boolean isOpen) {
        actionBar.setOpen(isOpen);
    }

    @Override
    public Actionbar asVaadinComponent() {
        return actionBar;
    }

}
