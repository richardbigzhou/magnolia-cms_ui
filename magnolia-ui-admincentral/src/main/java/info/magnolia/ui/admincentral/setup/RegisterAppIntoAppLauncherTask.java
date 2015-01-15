/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import java.util.Collection;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Task for registering apps into app launcher.
 */
public class RegisterAppIntoAppLauncherTask extends AbstractRepositoryTask {

    private final String APP_LAUNCHER_LAYOUT_GROUPS_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups";

    private final String appName;
    private final String groupName;
    private String fallbackGroupName;
    private boolean createGroup;
    private Collection<String> permissionsRolesNames;
    private Boolean permanent;
    private String color;

    public RegisterAppIntoAppLauncherTask(String appName, String groupName) {
        this(appName, groupName, false);
    }

    public RegisterAppIntoAppLauncherTask(String appName, String groupName, boolean createGroup) {
        this(appName, groupName, createGroup, Collections.EMPTY_LIST);
    }

    public RegisterAppIntoAppLauncherTask(String appName, String groupName, boolean createGroup, Collection<String> permissionsRolesNames) {
        this(appName, groupName, createGroup, permissionsRolesNames, null, null);
    }

    public RegisterAppIntoAppLauncherTask(String appName, String groupName, boolean createGroup, Boolean permanent, String color) {
        this(appName, groupName, createGroup, Collections.EMPTY_LIST, permanent, color);
    }

    public RegisterAppIntoAppLauncherTask(String appName, String groupName, boolean createGroup, Collection<String> permissionsRolesNames, Boolean permanent, String color) {
        super("Register app into App Launcher", String.format("This task register the %s app in the %s group of the app-launcher.", appName, groupName));
        this.appName = appName;
        this.groupName = groupName;
        this.createGroup = createGroup;
        this.permissionsRolesNames = permissionsRolesNames;
        this.permanent = permanent;
        this.color = color;
    }

    public RegisterAppIntoAppLauncherTask(String appName, String groupName, String fallbackGroupName) {
        super("Register app into App Launcher", String.format("This task register the %s app in the %s group of the app-launcher. If the group doesn't exist then the app will be registered into group %s.", appName, groupName, fallbackGroupName));
        this.appName = appName;
        this.groupName = groupName;
        this.fallbackGroupName = fallbackGroupName;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        if(appName == null || groupName == null){
            throw new TaskExecutionException("Could not execute task because app name or group name isn't specified.");
        }

        Session session = installContext.getConfigJCRSession();
        Node parent = NodeUtil.createPath(session.getRootNode(), APP_LAUNCHER_LAYOUT_GROUPS_PATH, NodeTypes.ContentNode.NAME);

        if(parent.hasNode(groupName) || createGroup){
            Node groupNode = NodeUtil.createPath(parent, groupName, NodeTypes.ContentNode.NAME);
            if(createGroup){
                if(permanent != null){
                    groupNode.setProperty("permanent", permanent);
                }
                if(color != null){
                    groupNode.setProperty("color", color);
                }
                if(permissionsRolesNames != null && !permissionsRolesNames.isEmpty()) {
                    Node permissionRolesNode = NodeUtil.createPath(groupNode, "permissions/roles", NodeTypes.ContentNode.NAME);
                    for(String roleName: permissionsRolesNames){
                        permissionRolesNode.setProperty(roleName, roleName);
                    }
                }
            }
            NodeUtil.createPath(groupNode, "apps/" + appName, NodeTypes.ContentNode.NAME);
        } else if(fallbackGroupName != null){
            if(parent.hasNode(fallbackGroupName)){
                Node groupNode = parent.getNode(fallbackGroupName);
                NodeUtil.createPath(groupNode, "apps/" + appName, NodeTypes.ContentNode.NAME);
            } else {
                installContext.warn(String.format("Can't register %s app, because %s group and %s fallback group do not exist.", appName, groupName, fallbackGroupName));
            }
        } else {
            installContext.warn(String.format("Can't register %s app, because %s group does not exist.", appName, groupName));
        }
    }

}
