/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework.app;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.AbstractActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;

import javax.inject.Inject;

/**
 * {@link info.magnolia.ui.api.action.ActionExecutor} used in the scope of sub apps. Reads the {@link ActionDefinition} from the {@link SubAppDescriptor} bound to the current {@link SubAppContext}.
 */
public class SubAppActionExecutor extends AbstractActionExecutor {

    private SubAppDescriptor subAppDescriptor;

    @Inject
    public SubAppActionExecutor(final ComponentProvider componentProvider, final SubAppContext subAppContext) {
        super(componentProvider);
        this.subAppDescriptor = subAppContext.getSubAppDescriptor();
    }

    @Override
    public ActionDefinition getActionDefinition(String actionName) {
        return subAppDescriptor.getActions().get(actionName);
    }

}
