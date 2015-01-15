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
package info.magnolia.ui.admincentral.usermenu.action;

import info.magnolia.audit.AuditLoggingUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;

import com.vaadin.ui.UI;

/**
 * Action for logging out of the admin central. Closes the current vaadin session and performs a system logout
 * using {@link info.magnolia.context.UserContext#logout()}.
 * Redirects to {@link MgnlContext#getContextPath()}.
 */
public class LogoutAction extends AbstractAction<LogoutActionDefinition> {
    public LogoutAction(LogoutActionDefinition definition) {
        super(definition);
    }

    @Override
    public void execute() throws ActionExecutionException {
        Context ctx = MgnlContext.getInstance();
        if (ctx instanceof UserContext) {
            // log before actual op, to preserve username for logging
            AuditLoggingUtil.log((UserContext) ctx);
            ((UserContext) ctx).logout();
        }
        UI.getCurrent().getSession().close();
        UI.getCurrent().getPage().setLocation(MgnlContext.getContextPath());
    }
}
