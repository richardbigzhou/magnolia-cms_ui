/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.browser.action.DelegateByNodeTypeActionDefinition;
import info.magnolia.ui.contentapp.browser.action.ExpandNodeActionDefinition;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Task that replaces defaultAction in the actionbar with a delegate action.
 *
 * @see DelegateByNodeTypeActionDefinition
 * @see ExpandNodeActionDefinition
 */
public class DefaultActionToDelegateActionTask extends AbstractRepositoryTask {

    private static final String EXPAND_NODE_ACTION_NAME = "expandNodeAction";
    private static final String DELEGATE_BY_NODE_TYPE_ACTION_NAME = "delegateByNodeTypeAction";
    private static final String NODE_TYPE_TO_ACTION_MAPPINGS_NAME = "nodeTypeToActionMappings";

    private static final String CLASS_PROPERTY = "class";
    private static final String NODE_TYPE = "nodeType";
    private static final String ACTION = "action";
    private static final String DEFAULT_ACTION = "defaultAction";

    private static final String ACTIONS_PATH = "/modules/%s/apps/%s/subApps/%s/actions";
    private static final String ACTIONBAR_PATH = "/modules/%s/apps/%s/subApps/%s/actionbar";

    private final String appName;
    private final String subAppName;
    private final Map<String, String> nodeTypeToActionMapping;

    public DefaultActionToDelegateActionTask(String name, String description, String appName, String subAppName, Map<String, String> nodeTypeToActionMapping) {
        super(name, description);
        this.appName = appName;
        this.subAppName = subAppName;
        this.nodeTypeToActionMapping = nodeTypeToActionMapping;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        String moduleName = installContext.getCurrentModuleDefinition().getName();
        Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
        setupExpandNodeAction(session, moduleName);
        setupDelegateAction(session, moduleName);
        linkDefaultActionToDelegateAction(session, moduleName);
    }

    private void setupDelegateAction(Session session, String moduleName) throws RepositoryException {
        Node actionsNode = session.getNode(String.format(ACTIONS_PATH, moduleName, appName, subAppName));
        Node delegateActionNode = actionsNode.addNode(DELEGATE_BY_NODE_TYPE_ACTION_NAME, NodeTypes.ContentNode.NAME);
        delegateActionNode.setProperty(CLASS_PROPERTY, DelegateByNodeTypeActionDefinition.class.getCanonicalName());
        Node mappingsNode = delegateActionNode.addNode(NODE_TYPE_TO_ACTION_MAPPINGS_NAME, NodeTypes.ContentNode.NAME);
        for (Map.Entry<String, String> entry : nodeTypeToActionMapping.entrySet()) {
            Node node = mappingsNode.addNode(Path.getValidatedLabel(entry.getKey()), NodeTypes.ContentNode.NAME);
            node.setProperty(NODE_TYPE, entry.getKey());
            node.setProperty(ACTION, entry.getValue());
        }
    }

    private void setupExpandNodeAction(Session session, String moduleName) throws RepositoryException {
        Node actionsNode = session.getNode(String.format(ACTIONS_PATH, moduleName, appName, subAppName));
        Node expandActionNode = actionsNode.addNode(EXPAND_NODE_ACTION_NAME, NodeTypes.ContentNode.NAME);
        expandActionNode.setProperty(CLASS_PROPERTY, ExpandNodeActionDefinition.class.getCanonicalName());
    }

    private void linkDefaultActionToDelegateAction(Session session, String moduleName) throws RepositoryException {
        Node actionbarNode = session.getNode(String.format(ACTIONBAR_PATH, moduleName, appName, subAppName));
        actionbarNode.setProperty(DEFAULT_ACTION, DELEGATE_BY_NODE_TYPE_ACTION_NAME);
    }
}
