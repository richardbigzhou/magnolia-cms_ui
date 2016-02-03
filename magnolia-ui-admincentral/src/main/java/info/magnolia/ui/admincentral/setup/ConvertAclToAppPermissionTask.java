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
package info.magnolia.ui.admincentral.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task converts old 45 Admincentral URL ACLs to new app permissions.
 **/
public class ConvertAclToAppPermissionTask extends AbstractRepositoryTask {

    protected static final String APP_PERMISSIONS_PATH = "permissions/roles/";
    protected static final String SUPERUSER_ROLE = "superuser";

    private static final Logger log = LoggerFactory.getLogger(ConvertAclToAppPermissionTask.class);

    private final String oldURL;
    private final String[] newApps;
    private final boolean removeOldPermissions;
    private final String query;

    public ConvertAclToAppPermissionTask(String name, String description, String oldURL, String[] newApps, boolean removeOldPermissions) {
        super(name, description);
        this.oldURL = oldURL;
        this.newApps = newApps;
        this.removeOldPermissions = removeOldPermissions;
        this.query = "select * from ['" + NodeTypes.ContentNode.NAME + "'] as t " +
                "where [path] = '" + oldURL +
                "' OR [path] = '" + oldURL + "*" +
                "' OR [path] = '" + oldURL + "/*'";
    }

    public ConvertAclToAppPermissionTask(String name, String description, String oldURL, String newApp, boolean removeOldPermissions) {
        this(name, description, oldURL, new String[] { newApp }, removeOldPermissions);
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {

        Session config = installContext.getJCRSession(RepositoryConstants.CONFIG);
        Session userRoles = installContext.getJCRSession(RepositoryConstants.USER_ROLES);
        QueryManager qm = userRoles.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, Query.JCR_SQL2);
        QueryResult queryResult = q.execute();
        NodeIterator iter = queryResult.getNodes();
        Set<Node> permissionsToRemove = new HashSet<Node>();

        while (iter.hasNext()) {
            Node node = (Node) iter.next();
            try {
                Node acl_uri = node.getParent();
                Node userRole = acl_uri.getParent();
                if (!userRole.isNodeType(NodeTypes.Role.NAME)) {
                    continue; // this is not user role permission node
                }
                String userRoleName = userRole.getName();
                Long permissions = node.getProperty("permissions").getLong();

                for (String newApp : newApps) {
                    Node newAppNode = config.getNode(newApp);

                    if (permissions == 0) {
                        if (!newAppNode.hasNode(APP_PERMISSIONS_PATH)) { // any permissions defined yet?
                            // we need to set permission at least to one role to deny access to others...superuser
                            NodeUtil.createPath(newAppNode, APP_PERMISSIONS_PATH, NodeTypes.ContentNode.NAME).setProperty(SUPERUSER_ROLE, SUPERUSER_ROLE);
                            log.info("Denying permission for '{}' app to any role except '{}'. Please add extra permissions to this app if required.", newApps, SUPERUSER_ROLE);
                        }

                    } else {
                        Node permissionsNode = NodeUtil.createPath(newAppNode, APP_PERMISSIONS_PATH, NodeTypes.ContentNode.NAME);

                        if (!permissionsNode.hasProperty(userRoleName)) {
                            permissionsNode.setProperty(userRoleName, userRoleName);
                            log.info("Adding permission for '{}' app to '{}' role.", newApps, userRoleName);
                        }
                    }
                }

                if (this.removeOldPermissions) {
                    permissionsToRemove.add(node);
                    log.info("Obsolete permission property '{}={}' for role '{}' will be removed.", node.getProperty("path").getString(), permissions, userRoleName);
                }

            } catch (RepositoryException e) {
                final String errMsg = "Cannot convert old permission '" + oldURL + "' to permissions to new apps.";
                log.error(errMsg);
                throw new TaskExecutionException(errMsg, e);
            }
        }

        for (Node node : permissionsToRemove) {
            node.remove();
        }
    }
}
