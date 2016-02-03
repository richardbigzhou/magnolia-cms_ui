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
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;


/**
 * A task to create a JCR browser content app. The app configuration will be created under <code>/modules/ui-admincentral/apps</code>. An entry for the app will be also created in the app launcher layout under the specified app group.
 * The app group is assumed to be existing at the time of executing this task. The task relies on the existence of the <code>ui-admincentral</code> (part of Magnolia UI project) at its default location under <code>/modules/ui-admincentral</code> as it
 * uses Magnolia's extension mechanism in order to inherit most of its configuration. Being the configuration inherited from ui-admincentral the tree will be editable and include properties.
 */
public class JcrBrowserContentAppTask extends AbstractTask {
    private final String appName;
    private final String label;
    private final String appGroup;
    private final String icon;
    private final String workspace;
    private final String rootPath;
    private NodeTypeDefinition mainNodeType;

    /**
     * @param appName a string representing a unique name for this app. If not unique, the task will fail.
     * @param label the app label to be displayed in the app launcher.
     * @param appGroup a string representing one of the default app groups (i.e. <code>edit, manage, dev, tools</code>) or a custom one (which is assumed to be already existing when running this task).
     * @param icon a string representing an icon. If not set defaults to <code>icon-app</code>.
     * @param workspace name of the workspace. If null or empty the task will fail.
     * @param absRootPath the absolute path to use as the root for the content app to be created. If not set, defaults to <code>/</code>. If not absolute, the task will fail.
     */
    public JcrBrowserContentAppTask(String appName, String label, String appGroup, String icon, String workspace, String absRootPath) {
        super("JCR browser app", String.format("Creates a JCR browser content app named '%s' for workspace '%s' starting at path '%s'.", appName, workspace, StringUtils.defaultIfEmpty(absRootPath, "/")));

        this.appName = appName;
        this.label = label;
        this.appGroup = appGroup;
        this.icon = icon;
        this.workspace = workspace;
        this.rootPath = absRootPath;
    }

    /**
     * @param mainNodeType the main node type for the specified workspace. If null, the name defaults to {@link NodeTypes.ContentNode.NAME}.
     * @see JcrBrowserContentAppTask
     */
    public JcrBrowserContentAppTask(String appName, String label, String appGroup, String icon, String workspace, String absRootPath, NodeTypeDefinition mainNodeType) {
        this(appName, label, appGroup, icon, workspace, absRootPath);
        this.mainNodeType = mainNodeType;
    }

    @Override
    public final void execute(InstallContext ctx) throws TaskExecutionException {

        try {
            final Session session = ctx.getConfigJCRSession();
            final Node appsNode = NodeUtil.createPath(session.getRootNode(), "modules/ui-admincentral/apps", NodeTypes.Content.NAME);

            if (session.itemExists(appsNode.getPath() + appName)) {
                throw new TaskExecutionException(String.format("An app named [%s] already exists at [%s], please choose a unique name for your app.", appName, appsNode.getPath()));
            }

            Node app = createApp(appsNode);
            createMainSubapp(app);
            addAppToLauncherLayout(session);

            session.save();
        } catch (RepositoryException e) {
            throw new TaskExecutionException("An error occurred while executing the task [" + getName() + "] ", e);
        }
    }

    protected void createMainSubapp(Node appNode) throws TaskExecutionException {
        try {
            Node subappsNode = NodeUtil.createPath(appNode, "subApps", NodeTypes.ContentNode.NAME);
            subappsNode.setProperty("extends", "/modules/ui-admincentral/apps/configuration/subApps");
            Node workbenchNode = NodeUtil.createPath(subappsNode, "browser/workbench", NodeTypes.ContentNode.NAME);

            if (StringUtils.isBlank(workspace)) {
                throw new TaskExecutionException("workspace cannot be null or empty");
            }
            workbenchNode.setProperty("workspace", workspace);

            if (StringUtils.isNotBlank(rootPath)) {
                if (!rootPath.startsWith("/")) {
                    throw new TaskExecutionException(String.format("Expected an absolute path for workspace [%s] but got [%s] instead.", workspace, rootPath));
                }
                workbenchNode.setProperty("path", rootPath);
            }
            if (mainNodeType != null) {
                Node nodeType = NodeUtil.createPath(workbenchNode, "nodeTypes/mainNodeType", NodeTypes.ContentNode.NAME);
                String icon = mainNodeType.getIcon();
                String name = mainNodeType.getName();
                nodeType.setProperty("icon", StringUtils.defaultIfEmpty(icon, "icon-node-content"));
                nodeType.setProperty("name", StringUtils.defaultIfEmpty(name, NodeTypes.ContentNode.NAME));
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.getMessage());
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getLabel() {
        return label;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public String getIcon() {
        return icon;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NodeTypeDefinition getMainNodeType() {
        return mainNodeType;
    }

    private Node createApp(Node appsNode) throws RepositoryException {
        Node app = NodeUtil.createPath(appsNode, appName, NodeTypes.ContentNode.NAME);
        app.setProperty("app", "info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor");
        app.setProperty("appClass", "info.magnolia.ui.contentapp.ContentApp");
        app.setProperty("icon", StringUtils.isNotBlank(icon) ? icon : "icon-app");
        app.setProperty("label", label);
        return app;
    }

    private void addAppToLauncherLayout(Session configJCRSession) throws RepositoryException {
        Node configNode = configJCRSession.getNode("/modules/ui-framework/config");
        NodeUtil.createPath(configNode, "appLauncherLayout/groups/" + appGroup + "/apps/" + appName, NodeTypes.ContentNode.NAME);
    }

}
