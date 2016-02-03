/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Excludes passed workspaces from flush cache policy.
 */
public class ExcludeWorkspacesFromFlushCachePolicy extends AbstractRepositoryTask {

    protected static final String EXCLUDED_WORKSPACES_CONFIG_PATH = "flushPolicy/policies/flushAll/excludedWorkspaces";
    protected static final String CACHE_CONFIGURATION_PATH = "modules/cache/config/contentCaching/";

    private final List<String> workspaceNames;

    public ExcludeWorkspacesFromFlushCachePolicy(String... workspaceNames) {
        super("Exclude workspace from flush cache policy", "Exclude from flush cache policy these workspaces: " + Arrays.asList(workspaceNames));
        this.workspaceNames = Arrays.asList(workspaceNames);
    }

    @Override
    public void doExecute(InstallContext ctx) throws RepositoryException {
        Node root = ctx.getJCRSession(RepositoryConstants.CONFIG).getRootNode();
        if (!root.hasNode(CACHE_CONFIGURATION_PATH)) {
            return;
        }
        for (Node cache : NodeUtil.getNodes(root.getNode(CACHE_CONFIGURATION_PATH))) {
            if (cache.hasNode(EXCLUDED_WORKSPACES_CONFIG_PATH)) {
                Node excludedWorkspaces = cache.getNode(EXCLUDED_WORKSPACES_CONFIG_PATH);
                for (String workspace : workspaceNames) {
                    excludedWorkspaces.setProperty(workspace, workspace);
                }
            }
        }
    }
}
