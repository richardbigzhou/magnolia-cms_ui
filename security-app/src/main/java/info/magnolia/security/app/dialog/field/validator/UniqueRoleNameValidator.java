/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.validator.AbstractStringValidator;

/**
 * A validator to ensure uniqueness of role names.
 */
public class UniqueRoleNameValidator extends AbstractStringValidator {

    private static final Logger log = LoggerFactory.getLogger(UniqueRoleNameValidator.class);

    private final SecuritySupport securitySupport;
    private final Item item;

    public UniqueRoleNameValidator(Item item, String errorMessage, SecuritySupport securitySupport) {
        super(errorMessage);
        this.item = item;
        this.securitySupport = securitySupport;
    }

    @Override
    protected boolean isValidValue(String value) {
        if (item instanceof JcrNodeAdapter) {
            // If we're editing an existing node then its allowed to use the current username of course
            if (!(item instanceof JcrNewNodeAdapter)) {
                try {
                    String currentName = ((JcrNodeAdapter) item).getJcrItem().getName();
                    if (StringUtils.equals(value, currentName)) {
                        return true;
                    }
                } catch (RepositoryException e) {
                    log.error("Exception occurred getting node name of node [{}]", ((JcrNodeAdapter) item).getItemId(), e);
                    return false;
                }
            }
            final String newRoleName = Path.getValidatedLabel(value);
            if (securitySupport.getRoleManager().getRole(newRoleName) != null) {
                // role with such name already exists
                return false;
            }
            return true;
        }
        return false;
    }
}
