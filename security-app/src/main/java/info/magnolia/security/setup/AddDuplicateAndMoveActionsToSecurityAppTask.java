/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.security.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Adds move and duplicate actions to users, roles and groups sub-apps of Security app.
 */
class AddDuplicateAndMoveActionsToSecurityAppTask extends AbstractTask {

    private final Map<String, String> names = new HashMap<String, String>() {{
        put("users", "User");
        put("groups", "Group");
        put("roles", "Role");
    }};

    public AddDuplicateAndMoveActionsToSecurityAppTask() {
        super("Add move and duplicate actions", "Add move and duplicate actions to security app sub-apps");
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        for (String subAppName : names.keySet()) {
            String subAppUnitName = names.get(subAppName);

            String moveActionName = String.format("move%s", subAppUnitName);
            String duplicateActionName = String.format("duplicate%s", subAppUnitName);

            String subAppPath = String.format("/security/subApps/%s/", subAppName);
            String actionPath = subAppPath + "actions/";

            String moveActionPath = actionPath + moveActionName;
            String duplicateActionPath = actionPath + duplicateActionName;

            new PartialBootstrapTask("", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", moveActionPath).execute(installContext);
            new PartialBootstrapTask("", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", duplicateActionPath).execute(installContext);

            String actionbarPath = String.format("%sactionbar/sections/%s/groups/editActions/items/", subAppPath, StringUtils.lowerCase(subAppUnitName));
            String moveActionBarEntry = actionbarPath + moveActionName;
            String duplicateActionBarEntry = actionbarPath + duplicateActionName;

            new PartialBootstrapTask("", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", moveActionBarEntry).execute(installContext);
            new PartialBootstrapTask("", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", duplicateActionBarEntry).execute(installContext);
        }
    }
}
