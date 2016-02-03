/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.workbench.definition;

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a workbench. Contains all elements which define a workbench configuration.
 */
@I18nable
public interface WorkbenchDefinition extends Serializable {

    String getName();

    String getWorkspace();

    /**
     * @return the path configured as root for this workspace. If not specified, defaults to root ("/").
     */
    String getPath();

    /**
     * @return all configured NodeTypes.
     */
    List<NodeTypeDefinition> getNodeTypes();

    /**
     * @return whether properties should be displayed as well (or just nodes)
     */
    boolean isIncludeProperties();

    /**
     * @return whether nodes used by the system should be included, for instance nodes internal to the operations of the JCR implementation.
     */
    boolean isIncludeSystemNodes();

    /**
     * Define if this workbench is used for Dialog.
     */
    boolean isDialogWorkbench();

    /**
     * @return the property (or comma separated list of properties) to be applied when no other order is requested.
     */
    String getDefaultOrder();

    /**
     * Checks if workbench can edit tree view inplace.
     *
     * @return true, if workbench is editable
     */
    boolean isEditable();

    /**
     * @return the DropConstraint class used to handle drag&drop.
     */
    Class<? extends DropConstraint> getDropConstraintClass();

    /**
     * @return the list of configured views.<br>
     */
    List<ContentPresenterDefinition> getContentViews();

}
