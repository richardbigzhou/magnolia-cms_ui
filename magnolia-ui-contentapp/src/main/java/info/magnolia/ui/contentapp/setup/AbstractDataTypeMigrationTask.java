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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.importexport.postprocessors.MetaDataAsMixinConversionHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migrate a Data type repository to a new workspace.<br>
 * This migration task: <br>
 * - keep the current node identifier<br>
 * - remove the metaData sub nodes<br>
 * <br>
 * Steps: <br>
 * - Copy the Data repository (data/categorization) to the new workspace (categorization) <br>
 * - For every element of the 'oldToNewNodeTypeMapping' <br>
 * -- Change the primary NodeType (from 'category' to 'mgnl:category') <br>
 * - Remove all metaData nodes.
 */
public abstract class AbstractDataTypeMigrationTask extends AbstractTask {
    private static final Logger log = LoggerFactory.getLogger(AbstractDataTypeMigrationTask.class);

    // data/categorization
    private String dataRootPath;
    private String newRootPath;
    private HashMap<String, String> oldToNewNodeTypeMapping;
    private String newWorkspaceName;
    private Session dataSession;
    private Session newSession;
    private boolean isTargetRoot;

    /**
     * @param dataPath path from the data to migrate ('/category'). If set to root, the task will not be executed.
     * @param newPath new path where the content of dataPath will be copied.
     * @param newWorkspaceName new workspace name.
     */
    public AbstractDataTypeMigrationTask(String taskName, String taskDescription, String dataPath, String newPath, String newWorkspaceName) {
        super(taskName, taskDescription);
        this.dataRootPath = dataPath;
        this.isTargetRoot = (StringUtils.isBlank(newPath) || "/".equals(newPath));
        this.newRootPath = (StringUtils.isNotBlank(newPath) && !"/".equals(newPath)) ? newPath : dataPath;
        this.newWorkspaceName = newWorkspaceName;
        this.oldToNewNodeTypeMapping = new HashMap<String, String>();
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            // Init session
            dataSession = installContext.getJCRSession("data");
            newSession = installContext.getJCRSession(newWorkspaceName);

            // Init oldToNewNodeTypeMapping
            initOldToNewNodeTypeMappingElement(oldToNewNodeTypeMapping);

            // Check that the root Data exist
            if (dataSession.nodeExists(dataRootPath)) {
                migrateData();
            } else {
                installContext.warn("Data migration task cancelled. The following data type do not exist in the data workspace: " + dataRootPath);
            }

        } catch (Exception e) {
            installContext.error("Unable to perform Migration task " + getName(), e);
            TaskExecutionException taskExecutionException = (e instanceof TaskExecutionException) ? (TaskExecutionException) e : new TaskExecutionException(e.getMessage());
            throw taskExecutionException;
        }

    }

    /**
     * Set the HashMap of nodeType to change.<br>
     * key: oldType ('dataFolder')<br>
     * value: newType ('mgnl:folder')<br>
     * <b>Order is important</b><br>
     * Define first the simple node type and the the types with restriction<br>
     * For example, if you have a node type for images that have a constraint for a custom data type (for binary).<br>
     * - First define your custom data type
     * addOldToNewNodeTypeMappingElement("dataFolder", "mgnl:folder");
     * addOldToNewNodeTypeMappingElement("category", "mgnl:category");
     * addOldToNewNodeTypeMappingElement("dataItemNode", "mgnl:content");
     */
    protected abstract void initOldToNewNodeTypeMappingElement(HashMap<String, String> oldToNewNodeTypeMapping);

    private void migrateData() throws TaskExecutionException {
        try {
            copyDataWorkspaceToNewWorkspace();
            for (Entry<String, String> entry : oldToNewNodeTypeMapping.entrySet()) {
                convertPrimaryNodeType(entry.getKey(), entry.getValue());
            }
            removeMetaDataNodes();
        } catch (RepositoryException re) {
            throw new TaskExecutionException("Could not migrate Data folders to the new workspace :" + newWorkspaceName, re);
        }
    }

    /**
     * Copy the dataRootPath to the new workspace at newRootPath.
     */
    private void copyDataWorkspaceToNewWorkspace() throws TaskExecutionException, RepositoryException {
        // Init
        final Node rootData = dataSession.getNode(dataRootPath);
        Workspace newWorkspace = newSession.getWorkspace();
        // Check that the data content to migrate is not the root of the Data module.
        if (rootData.getDepth() == 0) {
            throw new TaskExecutionException("Can not migrate the root of Data workspace. You have to choose a specific data type to migrate like 'data/category'");
        }
        if (!newSession.nodeExists(newRootPath) && !"/".equals(getParentPath(newRootPath)) && !newSession.nodeExists(getParentPath(newRootPath))) {
            NodeUtil.createPath(newSession.getRootNode(), getParentPath(newRootPath), NodeTypes.Folder.NAME).getSession().save();
        }
        newWorkspace.clone("data", dataRootPath, newRootPath, true);
        log.info("Following data workspace part {}: is now moved to the following workspace '{}' location '{}'", Arrays.asList(dataRootPath, newWorkspace.getName(), newRootPath).toArray());

        if (this.isTargetRoot) {
            // move to root
            NodeIterator nodeIterator = newSession.getNode(newRootPath).getNodes();
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                newSession.getWorkspace().move(node.getPath(), "/" + node.getName());
            }
            newSession.removeItem(newRootPath);
            newRootPath = "/";
        }

        newSession.save();
    }

    private void convertPrimaryNodeType(String oldNodeType, String newNodetype) throws RepositoryException {
        final Node root = newSession.getNode(newRootPath);
        NodeUtil.visit(root, createTypeVisitor(oldNodeType, newNodetype));
        newSession.save();
    }


    private NodeVisitor createTypeVisitor(final String oldNodeType, final String newNodetype) {
        NodeVisitor folderVisitor = new NodeVisitor() {
            @Override
            public void visit(Node node) throws RepositoryException {
                if (NodeUtil.isNodeType(node, oldNodeType)) {
                    node.setPrimaryType(newNodetype);
                    log.debug("Node primary Type changed from '{}' to '{}' for '{}' ", Arrays.asList(oldNodeType, newNodetype, node.getPath()).toArray());
                }
            }
        };
        return folderVisitor;
    }


    /**
     * Convert metadata node.
     */
    private void removeMetaDataNodes() throws RepositoryException {
        MetaDataAsMixinConversionHelper conversionHelper = new MetaDataAsMixinConversionHelper();
        conversionHelper.setDeleteMetaDataIfEmptied(true);
        conversionHelper.setPeriodicSaves(true);

        NodeIterator childRootNodes = newSession.getRootNode().getNodes();
        while (childRootNodes.hasNext()) {
            Node child = childRootNodes.nextNode();
            if (!child.getName().startsWith(NodeTypes.JCR_PREFIX) && !child.getName().startsWith(NodeTypes.REP_PREFIX)) {
                conversionHelper.convertNodeAndChildren(child);
            } else {
                log.debug("Node '{}' are not handled by this task", child.getName());
            }
        }
        log.info("Converted MetaData in workspace '{}'", newSession.getWorkspace().getName());
    }

    private String getParentPath(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/");
        if (lastIndexOfSlash > 0) {
            return StringUtils.substringBeforeLast(path, "/");
        }
        return "/";
    }

}


