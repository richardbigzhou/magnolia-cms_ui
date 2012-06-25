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
package info.magnolia.ui.admincentral.actionbar.builder;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.widget.actionbar.ActionButton;
import info.magnolia.ui.widget.actionbar.Actionbar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * Basic builder for an action bar widget based on an action bar definition.
 */
@SuppressWarnings("serial")
public class ActionbarBuilder {

    private static final Logger log = LoggerFactory.getLogger(ActionbarBuilder.class);

    public static Actionbar build(ActionbarDefinition definition, final ActionbarPresenter presenter) {
        Actionbar actionbar = new Actionbar();
        if (definition == null) {
            log.warn("No actionbar definition found. This will result in an empty action bar. Is that intended?");
            return actionbar;
        } else {

            final ClickListener clickListener = new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    ActionButton button = (ActionButton) event.getButton();
                    presenter.onActionbarItemClicked(button.getActionName());
                }
            };

            for (ActionbarSectionDefinition section : definition.getSections()) {
                for (ActionbarGroupDefinition group : section.getGroups()) {
                    for (ActionbarItemDefinition item : group.getItems()) {

                        ActionButton button = new ActionButton(item.getLabel());
                        button.setIcon(new ThemeResource(item.getIcon()));

                        final String actionName = item.getName();
                        button.setActionName(actionName);
                        button.setGroupName(group.getName());
                        button.setSectionTitle(section.getTitle());

                        button.addListener(clickListener);

                        presenter.addAction(actionName, item.getActionDefinition());
                        actionbar.addComponent(button);
                    }
                }
            }
        }
        return actionbar;
    }

}
