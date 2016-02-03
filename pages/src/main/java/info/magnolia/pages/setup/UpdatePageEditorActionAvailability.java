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
package info.magnolia.pages.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.pages.app.action.DeleteComponentAction;
import info.magnolia.pages.app.action.DeletePageItemAction;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Updates actions and actionbar section to use availability for all actions and remove unused actions.
 */
public class UpdatePageEditorActionAvailability extends ArrayDelegateTask {

    private String bootstrapFile = "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml";
    private String[] actions = {"editArea", "addArea", "deleteArea", "editComponent", "addComponent", "deleteComponent", "startMoveComponent", "stopMoveComponent", "showPreviousVersion"};
    private String[] actionbarSections = {"pagePreviewActions", "pageActions", "areaActions", "componentActions", "pageDeleteActions"};
    private String[] obsoleteActionbarSections = {"editableAreaActions", "optionalAreaActions", "optionalEditableAreaActions"};
    private String[] unusedActions = {"redo", "undo", "copyComponent", "pasteComponent"};


    public UpdatePageEditorActionAvailability() {
        super("Bootstrap availability rules to page editor actions and actionbar sections.");

        bootstrapActionAvailability();
        bootstrapActionbarSectionAvailability();
        deleteObsoleteActionbarSections();
        deleteUnusedAction();

        addTask(new NodeExistsDelegateTask("Update deleteArea action.", "/modules/pages/apps/pages/subApps/detail/actions/deleteArea",
                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/detail/actions/deleteArea", "implementationClass", DeletePageItemAction.class.getName())));

        addTask(new NodeExistsDelegateTask("Update deleteArea action.", "/modules/pages/apps/pages/subApps/detail/actions/deleteComponent",
                new CheckAndModifyPropertyValueTask("/modules/pages/apps/pages/subApps/detail/actions/deleteComponent", "implementationClass", DeleteComponentAction.class.getName(), DeletePageItemAction.class.getName())));


        addTask(new UpdateAreaSectionActionsTask());


        addTask(new NodeExistsDelegateTask("Bootstrap new pageNodeAreaActions.", "/modules/pages/apps/pages/subApps/detail/actionbar/sections",
                new PartialBootstrapTask("Bootstrap pageNodeAreaActions actionbar section.",
                        bootstrapFile,
                        "/pages/subApps/detail/actionbar/sections/pageNodeAreaActions"))
        );

        addTask(new NodeExistsDelegateTask("Bootstrap new editPageNodeArea action.", "/modules/pages/apps/pages/subApps/detail/actions/",
                new PartialBootstrapTask("Bootstrap pageNodeAreaActions actionbar section.",
                        bootstrapFile,
                        "/pages/subApps/detail/actions/editPageNodeArea"))
        );
    }

    private void deleteUnusedAction() {
        for (String action : unusedActions) {
            String actionPath = "/modules/pages/apps/pages/subApps/detail/actions/" + action;

            addTask(new NodeExistsDelegateTask(String.format("Remove unused action: %s", action), actionPath,
                    new RemoveNodeTask("", actionPath))
            );
        }
    }

    private void bootstrapActionAvailability() {
        for (String action : actions) {
            String actionPath = "/pages/subApps/detail/actions/" + action;

            addTask(new NodeExistsDelegateTask(String.format("Update action availability for %s action.", action), "/modules/pages/apps" + actionPath,
                    new PartialBootstrapTask(String.format("Bootstrap availability for %s action.", action),
                            bootstrapFile,
                            actionPath + "/availability"))
            );
        }
    }

    private void bootstrapActionbarSectionAvailability() {
        for (String section : actionbarSections) {
            String sectionPath = "/pages/subApps/detail/actionbar/sections/" + section;

            addTask(new NodeExistsDelegateTask(String.format("Update action availability for %s section.", section), "/modules/pages/apps" + sectionPath,
                    new PartialBootstrapTask(String.format("Bootstrap availability for %s action.", section),
                            bootstrapFile,
                            sectionPath + "/availability"))
            );
        }
    }

    private void deleteObsoleteActionbarSections() {
        for (String section : obsoleteActionbarSections) {
            String sectionPath = "/modules/pages/apps/pages/subApps/detail/actionbar/sections/" + section;

            addTask(new NodeExistsDelegateTask(String.format("Remove obsolete page editor actionbar section: %s", section), sectionPath,
                    new RemoveNodeTask("", sectionPath))
            );
        }
    }

    /**
     * Update tasks for areaActions section.
     */
    private class UpdateAreaSectionActionsTask extends AbstractRepositoryTask {

        public UpdateAreaSectionActionsTask() {
            super("Update page editor actionbar", "Add missing actions to areaActions section.");
        }

        @Override
        protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
            Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
            String areaSection = "/modules/pages/apps/pages/subApps/detail/actionbar/sections/areaActions";

            if (!session.nodeExists(areaSection)) {
                return;
            }
            Node areaActions = session.getNode(areaSection);
            Node groups = NodeUtil.createPath(areaActions, "groups", NodeTypes.ContentNode.NAME);

            NodeUtil.createPath(groups, "editingActions/items/editArea", NodeTypes.ContentNode.NAME);
            Node addingActions = NodeUtil.createPath(groups, "addingActions/items", NodeTypes.ContentNode.NAME);

            NodeUtil.createPath(addingActions, "addArea", NodeTypes.ContentNode.NAME);
            NodeUtil.createPath(addingActions, "deleteArea", NodeTypes.ContentNode.NAME);
            NodeUtil.createPath(addingActions, "addComponent", NodeTypes.ContentNode.NAME);

        }
    }
}
