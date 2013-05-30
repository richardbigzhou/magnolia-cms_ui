/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.mediaeditor.data;

import com.vaadin.data.Property;

/**
 * Property interface that is capable of tracking data modification and
 * undo/redo the steps. The original value is buffered and the state can
 * always be rolled back to it.
 */
public interface EditHistoryTrackingProperty extends Property<byte[]> {

    /**
     * Gets the last done action name.
     * @return last action name.
     */
    String getLastDoneActionName();

    /**
     * Gets the last undone action name.
     * @return last undone action name.
     */
    String getLastUnDoneActionName();

    /**
     * Clears modification history which erases all the tracked steps.
     * After this method call it will be impossible to undo/redo operations.
     */
    void purgeHistory();

    /**
     * Sets the amount of steps that can be tracked. When the limit is reached,
     * the eldest recorded steps are erased.
     * @param capacity amount of steps to be available for undo/redo.
     */
    void setCapacity(int capacity);

    /**
     * Starts an action which will cause a new record to appear in history.
     * @param actionName the name of an action.
     */
    void startAction(String actionName);

    /**
     * Undo last step.
     */
    void undo();

    /**
     * Redo last step.
     */
    void redo();

    /**
     * Propagate changes to the original value.
     */
    void commit();

    /**
     * Roll back to original value.
     */
    void revert();
}
