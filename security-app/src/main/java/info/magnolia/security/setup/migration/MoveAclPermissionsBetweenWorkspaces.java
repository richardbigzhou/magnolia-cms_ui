/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.security.setup.migration;

import info.magnolia.cms.util.PathUtil;
import info.magnolia.jcr.predicate.NodeNamePredicate;
import info.magnolia.jcr.predicate.NodePropertyNamePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update ACL's from a workspace to another. <br>
 * For example move ACL's from dms to dam workspace: <br>
 * - rename nodes with name acl_dms to acl_dam <br>
 * <br>
 * Check the associated path and correct them if required. <br>
 * - If the related path (/demo-project/image/icon) is no more valid on the target workspace.<br>
 * -- check if the path prefixed with one of the element of 'subPaths' will match<br>
 * --- If one match found and 'updatePath' is set to true, set this path to the ACL property.<br>
 * - Else log a warning message.<br>
 */
public class MoveAclPermissionsBetweenWorkspaces  extends AbstractRepositoryTask {

    private static final Logger log = LoggerFactory.getLogger(MoveAclPermissionsBetweenWorkspaces.class);

    private String everythingUnderThis = "/*";
    private String SelectedNode = "$";

    private final List<String> subPaths;
    private final String sourceWorkspaceName;
    private final String targetWorkspaceName;
    private final boolean updatePath;

    /**
     * @param sourceWorkspaceName source workspace name like 'dms'
     * @param targetWorkspaceName target workspace name like 'dam'
     * @param subPaths List of sub path set during migration process.
     */
    public MoveAclPermissionsBetweenWorkspaces(String name, String description, String sourceWorkspaceName, String targetWorkspaceName, List<String> subPaths, boolean updatePath) {
        super(name, description);
        this.sourceWorkspaceName = sourceWorkspaceName;
        this.targetWorkspaceName = targetWorkspaceName;
        this.updatePath = updatePath;
        this.subPaths = subPaths == null ? new ArrayList<String>() : subPaths;
    }


    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        if (StringUtils.isBlank(this.sourceWorkspaceName) || StringUtils.isBlank(this.targetWorkspaceName)) {
            log.warn("sourceWorkspaceName:'{}' and targetWorkspaceName:'{}' have to be defined ", this.sourceWorkspaceName, this.targetWorkspaceName);
            return;
        }
        // Initialize
        final Session targetSession = installContext.getJCRSession(targetWorkspaceName);
        final Session session = installContext.getJCRSession(RepositoryConstants.USER_ROLES);
        final Node rootNode = session.getRootNode();

        String nodeName = "acl_" + sourceWorkspaceName;
        String newAclNodeName = "acl_" + targetWorkspaceName;
        Iterator<Node> iterator = NodeUtil.collectAllChildren(rootNode, new NodeNamePredicate(nodeName)).iterator();
        while (iterator.hasNext()) {
            Node aclNode = iterator.next();
            handleAclNode(targetSession, aclNode, newAclNodeName, installContext);
        }
    }

    /**
     * Rename the ACL node and check the related permission path.
     */
    private void handleAclNode(Session targetSession, Node aclNode, String newAclNodeName, InstallContext installContext) throws RepositoryException {
        // If node already exist, return
        final Node parent = aclNode.getParent();
        final String newAclPath = NodeUtil.combinePathAndName(parent.getPath(), newAclNodeName);
        if (aclNode.getSession().nodeExists(newAclPath)) {
            log.warn("{} already exist. No migration will be performed.", newAclPath);
            return;
        }

        // Rename node
        String oldAclNodePath = aclNode.getPath();
        NodeUtil.renameNode(aclNode, newAclNodeName);
        log.info("The following ACL node '{}' was succesfully renamed to '{}'", oldAclNodePath, aclNode.getPath());
        // Check the permission path
        Iterator<Node> childNodeIterator = NodeUtil.collectAllChildren(aclNode, new NodePropertyNamePredicate("path")).iterator();
        // Iterate child and check if the path is still valid
        while (childNodeIterator.hasNext()) {
            Property pathProperty = childNodeIterator.next().getProperty("path");
            String originalPath = pathProperty.getString();
            String extraParameter = getExtraAclParameter(originalPath);
            String pathToHandle = StringUtils.removeEnd(originalPath, extraParameter);
            // If the path is blank, handle the next one.
            if (StringUtils.isBlank(pathToHandle)) {
                continue;
            }

            // Check if the path is well-formed
            if (!isPathWellFormed(targetSession, pathToHandle)) {
                log.info("Following ACL link is not a well-formed path '{}'. ", originalPath);
            } else if (!targetSession.itemExists(pathToHandle)) {
                // Path is well-formed but not valid
                handleNoNoValidACLPath(targetSession, pathToHandle, extraParameter, pathProperty, installContext);
            } else {
                log.info("Following ACL link is valid '{}'. ", originalPath);
            }
        }
    }

    /**
     * @return true if the path is well formed, false otherwise.
     */
    private boolean isPathWellFormed(Session targetSession, String path) {
        try {
            targetSession.itemExists(path);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * Try to found a valid path based on the defined subpaths.
     */
    private void handleNoNoValidACLPath(Session targetSession, String originalPath, String extraParameter, Property pathProperty, InstallContext installContext) throws RepositoryException {
        // Check the path prefixed with the subpath
        String validPath = getValidPathWithSubPath(targetSession, originalPath);
        if (StringUtils.isNotBlank(validPath)) {
            if (this.updatePath) {
                // re-add the /* removed in the previous step.
                validPath = validPath + extraParameter;
                pathProperty.setValue(validPath);
                log.info("The original path was incorect '{}' and is replaced by '{}'", originalPath, validPath);
            } else {
                installContext.info("The path '" + originalPath + "' defined for the following ACL '" + pathProperty.getParent().getPath() + "' is no more valid. The following is Valid '" + validPath + "'. Please use Security App to correct this.");
            }
        } else {
            installContext.warn("The path '" + originalPath + "' defined for the following ACL '" + pathProperty.getParent().getPath() + "' is no more valid. Please use Security App to correct this.");
            log.warn("The path '{}' defined for the following ACL '{}' is no more valid. Please use Security App to correct this.", originalPath, pathProperty.getParent().getPath());

        }
    }

    /**
     * Iterate the subPaths list and try to found a valid one.
     *
     * @return the first valid path found or null otherwise.
     */
    private String getValidPathWithSubPath(Session targetSession, String originalPath) throws RepositoryException {
        for (String subPath : subPaths) {
            String migratedPath = PathUtil.createPath(StringUtils.removeEnd(subPath, "/"), StringUtils.removeStart(originalPath, "/"));
            log.debug("Check if the following migrated path exist {}", migratedPath);
            if (targetSession.itemExists(migratedPath)) {
                return migratedPath;
            }
        }
        return null;
    }

    private String getExtraAclParameter(String acl) {
        if (StringUtils.endsWith(acl, everythingUnderThis)) {
            return everythingUnderThis;
        }
        if (StringUtils.endsWith(acl, SelectedNode)) {
            return SelectedNode;
        }
        return StringUtils.EMPTY;
    }

}
