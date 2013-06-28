/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.admincentral.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * VersionHandler for the Admin Central module.
 */
public class AdmincentralModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final class RenameLegacyI18nNodeIfExistingTask extends IsModuleInstalledOrRegistered {
        public RenameLegacyI18nNodeIfExistingTask() {
            super("Rename legacy i18n node", "Renames /server/i18n/authoring as authoringLegacy. Only run if adminInterface legacy module is installed.", "adminInterface", new RenameNodesTask("", "", RepositoryConstants.CONFIG, "/server/i18n", "authoring", "authoringLegacy", NodeTypes.ContentNode.NAME));
        }
    }

    public AdmincentralModuleVersionHandler() {
        super();
        register(DeltaBuilder.update("5.0.1", "")
                .addTask(new RenameLegacyI18nNodeIfExistingTask())
                .addTask(new RenameNodesTask("Rename 5.0 i18n node", "Renames /server/i18n/authoring50 as authoring.", RepositoryConstants.CONFIG, "/server/i18n", "authoring50", "authoring", NodeTypes.ContentNode.NAME)));
    }
    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> list = new ArrayList<Task>();

        list.add(new IsModuleInstalledOrRegistered(
                "Replace login security pattern",
                "Replaces old login security pattern '/.resources/loginForm' (if present) with the new one '/.resources/defaultLoginForm'.",
                "adminInterface",
                new CheckAndModifyPropertyValueTask(
                        "",
                        "",
                        RepositoryConstants.CONFIG,
                        "/server/filters/uriSecurity/bypasses/login",
                        "pattern",
                        "/.resources/loginForm",
                        "/.resources/defaultLoginForm")));

        return list;
    }
}
