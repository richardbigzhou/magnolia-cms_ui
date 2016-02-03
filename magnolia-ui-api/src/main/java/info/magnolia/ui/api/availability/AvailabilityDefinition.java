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
package info.magnolia.ui.api.availability;

import info.magnolia.cms.security.operations.AccessDefinition;

import java.util.Collection;

/**
 * Definition of restrictions on when subject is available.
 */
public interface AvailabilityDefinition {

    /**
     * If true the subject is available when there's no selection.
     */
    boolean isRoot();

    /**
     * If true the subject is available for properties.
     */
    boolean isProperties();

    /**
     * If true the subject is available for nodes.
     */
    boolean isNodes();

    /**
     * If true, the subject is available for multiple item selection.
     */
    boolean isMultiple();

    /**
     * Unless this is empty the subject is available only for these node types.
     */
    Collection<String> getNodeTypes();

    /**
     * Returns the AccessDefinition object for this subject.
     */
    AccessDefinition getAccess();

    /**
     * If true the subject is <i>only</i> available if write permission is granted for the item on which availability is evaluated.
     */
    boolean isWritePermissionRequired();

    /**
     * Returns the collection of availability rule definitions for this subject.
     */
    Collection<? extends AvailabilityRuleDefinition> getRules();
}
