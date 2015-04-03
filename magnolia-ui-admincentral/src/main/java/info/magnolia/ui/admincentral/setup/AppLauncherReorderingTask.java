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

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic safe reordering task to make it easy to rearrange order of apps on the applauncher.
 */
public class AppLauncherReorderingTask extends AbstractRepositoryTask {

    private static final Logger log = LoggerFactory.getLogger(AppLauncherReorderingTask.class);

    private final String appName;
    private final String groupName;
    private final Order order;
    private final String relativeAppName;

    public AppLauncherReorderingTask(String appName, String groupName, Order order, String relativeAppName) {
        super("Reorder applauncher", String.format("This task reorders the %s app in the %s group of the app-launcher.", appName, groupName));

        this.appName = appName;
        this.groupName = groupName;
        this.order = order;
        this.relativeAppName = relativeAppName;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {

        Session session = ctx.getConfigJCRSession();

        if (!session.nodeExists(String.format("/modules/ui-admincentral/config/appLauncherLayout/groups/%s/apps/%s", groupName, appName))) {
            log.warn("Not reordering the {} app in the {} group because either the group or the app doesn't exist.", appName, groupName);
            return;
        }

        Node apps = session.getNode(String.format("/modules/ui-admincentral/config/appLauncherLayout/groups/%s/apps", groupName));
        Node app = apps.getNode(appName);

        switch (order) {
        case FIRST:
            NodeUtil.orderFirst(app);
            break;

        case BEFORE:
            if (apps.hasNode(relativeAppName)) {
                NodeUtil.orderBefore(app, relativeAppName);
            } else {
                log.warn("Not reordering the {} app in the {} group because the relative app {} does not exist.", appName, groupName, relativeAppName);
            }
            break;

        case AFTER:
            if (apps.hasNode(relativeAppName)) {
                NodeUtil.orderAfter(app, relativeAppName);
            } else {
                log.warn("Not reordering the {} app in the {} group because the relative app {} does not exist.", appName, groupName, relativeAppName);
            }
            break;

        case LAST:
            NodeUtil.orderLast(app);
            break;

        }
    }

    /**
     * The type of the applauncher reordering operation.
     */
    public static enum Order {
        FIRST, BEFORE, AFTER, LAST
    }

}
