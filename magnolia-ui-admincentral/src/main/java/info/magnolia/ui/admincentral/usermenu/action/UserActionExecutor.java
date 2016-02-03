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
package info.magnolia.ui.admincentral.usermenu.action;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.AdmincentralModule;
import info.magnolia.ui.admincentral.usermenu.definition.UserMenuDefinition;
import info.magnolia.ui.api.action.AbstractActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

/**
 * Action Executor for actions registered in {@link UserMenuDefinition} and obtained by {@link info.magnolia.ui.admincentral.AdmincentralModule#getUserMenu()}.
 */
public class UserActionExecutor extends AbstractActionExecutor {

    private final UserMenuDefinition userMenuDefinition;

    @Inject
    public UserActionExecutor(ComponentProvider componentProvider, AdmincentralModule module) {
        super(componentProvider);
        this.userMenuDefinition = module.getUserMenu();
    }

    @Override
    public ActionDefinition getActionDefinition(String actionName) {

        return (userMenuDefinition != null) ? userMenuDefinition.getActions().get(actionName) : null;
    }

    public Collection<ActionDefinition> getActions() {
        return (userMenuDefinition != null) ? userMenuDefinition.getActions().values() : Collections.EMPTY_LIST;
    }
}
