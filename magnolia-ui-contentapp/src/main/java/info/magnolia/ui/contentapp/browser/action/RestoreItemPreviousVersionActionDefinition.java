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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.ui.api.action.CommandActionDefinition;


/**
 * ActionDefinition used for restoring the previous version of an item and its descendants which are in the mgnl: namespace.
 * The default settings are
 * <ul>
 * <li>the action is async
 * <li>the command name is set to restorePreviousVersion
 * <li>parentNodeTypeOnly is false
 * </ul>
 * These settings can be changed for specific cases both in subclasses or directly in the config tree.
 */
public class RestoreItemPreviousVersionActionDefinition extends CommandActionDefinition {

    private boolean parentNodeTypeOnly = false;

    public RestoreItemPreviousVersionActionDefinition() {
        setImplementationClass(RestoreItemPreviousVersionAction.class);
        setAsynchronous(true);
        setCommand("restorePreviousVersion");
    }

    /**
     * Set to true, if one wants to restore all descendants whose type is the same as the parent one.
     */
    public void setParentNodeTypeOnly(boolean parentNodeTypeOnly) {
        this.parentNodeTypeOnly = parentNodeTypeOnly;
    }

    /**
     * @return true, if one wants to restore all descendants whose type is the same as the parent one. Default value is false.
     */
    public boolean isParentNodeTypeOnly() {
        return parentNodeTypeOnly;
    }
}