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
package info.magnolia.security.app.dialog.field.validator;

import static info.magnolia.security.app.util.AccessControlPropertyUtil.*;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.security.app.dialog.field.WorkspaceAccessControlList;

import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.validator.AbstractValidator;

/**
 * Typed validator for workspace-specific ACL {@linkplain WorkspaceAccessControlList.Entry entries}.
 * It examines whether the current user creating/editing a role has himself the required permissions
 * to the workspaces he's specifying in the ACLs. See MGNLUI-2357.
 *
 * <p>The validation strategy is as follows:</p>
 * <ul>
 * <li>Current user must have access to the given workspace
 * <li>Current user needs to have a matching permission to the path he's granting permission for
 * <li>The path he's granting permission must not interfere with other permissions
 * <li>The user's best matching permission needs to grant equal or greater rights than the ones being granted
 * <li>In order to deny permission, user requires read permission to that path
 * <li>Assigning a recursive permission (ending with '*' wildcard) requires user to also have such recursive permission
 * </ul>
 */
public class WorkspaceAccessControlValidator extends AbstractValidator<WorkspaceAccessControlList.Entry> {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceAccessControlValidator.class);
    private final String originalErrorMessage;
    private String workspace;

    public WorkspaceAccessControlValidator(String workspace, String errorMessage) {
        super(errorMessage);
        this.workspace = workspace;
        this.originalErrorMessage = errorMessage;
    }

    @Override
    public Class<WorkspaceAccessControlList.Entry> getType() {
        return WorkspaceAccessControlList.Entry.class;
    }

    @Override
    protected boolean isValidValue(WorkspaceAccessControlList.Entry entry) {
        boolean isValid = true;

        if (MgnlContext.getUser().hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        // This is an ACL added using WorkspaceAccessFieldFactory
        WorkspaceAccessControlList.Entry entryItem = entry;
        String path = entryItem.getPath();
        long accessType = entryItem.getAccessType();
        long permissions = entryItem.getPermissions();

        if (accessType < WorkspaceAccessControlList.ACCESS_TYPE_NODE || accessType > WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN) {
            throw new IllegalArgumentException("Access type should be one of ACCESS_TYPE_NODE (1), ACCESS_TYPE_CHILDREN (2) or ACCESS_TYPE_NODE_AND_CHILDREN (3)");
        }

        try {
            if (!isCurrentUserEntitledToGrantRights(workspace, path, accessType, permissions)) {
                isValid = !isValid;
            }
        } catch (AccessControlException e) {
            isValid = !isValid;
        } catch (RepositoryException e) {
            log.error("Could not validate current user permissions: ", e);
            isValid = !isValid;
        }

        if (!isValid) {
            setErrorMessage(MessageFormat.format(originalErrorMessage, permissions, path, accessType));
        }

        return isValid;
    }

    /**
     * @return false if current user has no matching permission in the given workspace, if the matching permission does not grant sufficient rights,
     * or if the granted permission conflicts with sub-path restrictions.
     */
    private boolean isCurrentUserEntitledToGrantRights(String workspaceName, String path, long accessType, long permissions) throws RepositoryException {
        // Granting DENY access is only allowed if the user has READ access to the node
        if (permissions == Permission.NONE) {
            permissions = Permission.READ;
        }

        ACL acl = PrincipalUtil.findAccessControlList(MgnlContext.getSubject(), workspaceName);
        if (acl == null) {
            return false;
        }

        String selectedPath = stripWildcardsFromPath(path);

        // validate node permission
        if ((accessType & WorkspaceAccessControlList.ACCESS_TYPE_NODE) == WorkspaceAccessControlList.ACCESS_TYPE_NODE) {
            Permission nodePerm = findBestMatchingPermissions(acl.getList(), selectedPath);
            if (nodePerm == null || !granted(nodePerm, permissions)) {
                return false;
            }
        }

        // validate sub-node permission
        if ((accessType & WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN) == WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN) {

            String suffixForChildren = selectedPath.equals("/") ? "*" : "/*";
            String childPath = selectedPath + suffixForChildren;

            // find sub-path restrictions: any permission to a sub-path of the selected path, with lower permission
            Set<Permission> violatedPerms = findViolatedPermissions(acl.getList(), childPath, permissions);
            if (!violatedPerms.isEmpty()){
                return false;
            }

            Permission childPerm = findBestMatchingPermissions(acl.getList(), childPath);
            // The child permission must end with /*
            if (childPerm == null || !granted(childPerm, permissions) || !StringUtils.endsWith(childPerm.getPattern().getPatternString(), "/*")) {
                return false;
            }
        }

        return true;
    }

    private String stripWildcardsFromPath(String path) {
        path = StringUtils.stripEnd(path, "/*");
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        return path;
    }

    private boolean granted(Permission permissionsGranted, long permissionsNeeded) {
        return (permissionsGranted.getPermissions() & permissionsNeeded) == permissionsNeeded;
    }
}
