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

import static info.magnolia.security.app.util.AccessControlPropertyUtil.findBestMatchingPermissions;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.security.app.dialog.field.AccessControlList;

import java.security.AccessControlException;
import java.text.MessageFormat;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.validator.AbstractValidator;

/**
 * Typed validator for ACL {@linkplain AccessControlList.Entry entries}.
 * It examines whether the current user creating/editing a role has himself the required permissions
 * to the URIs he's specifying in the ACLs. See MGNLUI-2357.
 *
 * <p>The validation strategy is as follows:</p>
 * <ul>
 * <li>Current user must have access to the uri "workspace"
 * <li>Current user needs to have a matching permission to the path he's granting permission for
 * <li>The user's best matching permission needs to grant equal or greater rights than the ones being granted
 * <li>In order to deny permission, user requires read/get permission to that path
 * <li>Assigning a recursive permission (ending with '*' wildcard) requires user to also have such recursive permission
 * </ul>
 */
public class WebAccessControlValidator extends AbstractValidator<AccessControlList.Entry> {
    private static final Logger log = LoggerFactory.getLogger(WebAccessControlValidator.class);
    private final String originalErrorMessage;

    public WebAccessControlValidator(String errorMessage) {
        super(errorMessage);
        this.originalErrorMessage = errorMessage;
    }

    @Override
    public Class<AccessControlList.Entry> getType() {
        return AccessControlList.Entry.class;
    }

    @Override
    protected boolean isValidValue(AccessControlList.Entry entry) {
        boolean isValid = true;

        if (MgnlContext.getUser().hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        String path = entry.getPath();
        long permissions = entry.getPermissions();

        try {
            if (!isCurrentUserEntitledToGrantUriRights(path, permissions)) {
                isValid = !isValid;
            }
        } catch (AccessControlException e) {
            isValid = !isValid;
        } catch (RepositoryException e) {
            log.error("Could not validate current user permissions: ", e);
            isValid = !isValid;
        }

        if (!isValid) {
            setErrorMessage(MessageFormat.format(originalErrorMessage, permissions, path));
        }

        return isValid;
    }

    /**
     * @return false if current user has no matching permission in the uri "workspace", or if the matching permission does not grant sufficient rights.
     */
    private boolean isCurrentUserEntitledToGrantUriRights(String path, long permissions) throws RepositoryException {
        // Granting DENY access is only allowed if the user has READ access to the path
        if (permissions == Permission.NONE) {
            permissions = Permission.READ;
        }

        ACL acl = PrincipalUtil.findAccessControlList(MgnlContext.getSubject(), "uri");
        if (acl == null) {
            return false;
        }

        boolean recursive = path.endsWith("*");

        Permission ownPermissions = findBestMatchingPermissions(acl.getList(), stripWildcardsFromPath(path));
        if (ownPermissions == null) {
            return false;
        }

        if (recursive && !ownPermissions.getPattern().getPatternString().endsWith("*")) {
            return false;
        }

        return granted(ownPermissions, permissions);
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
