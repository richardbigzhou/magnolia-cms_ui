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
package info.magnolia.ui.vaadin.editor;

import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ErrorType;

/**
 * Listener interface for handling events coming from the client side.
 * Do not change the constant action names, without updating configuration of actions used in actionbar.
 */
public interface PageEditorListener {

    static final String ACTION_EDIT_ELEMENT = "editElement";
    static final String ACTION_EDIT_COMPONENT = "editComponent";
    static final String ACTION_ADD_COMPONENT = "addComponent";
    static final String ACTION_SORT_COMPONENT = "sortComponent";
    static final String ACTION_START_MOVE_COMPONENT = "startMoveComponent";
    static final String ACTION_STOP_MOVE_COMPONENT = "stopMoveComponent";
    static final String ACTION_ADD_AREA = "addArea";

    // only used server side,
    static final String ACTION_CANCEL_MOVE_COMPONENT = "cancelMoveComponent";
    static final String ACTION_VIEW_PREVIEW = "preview";
    static final String ACTION_VIEW_EDIT = "edit";

    void onElementSelect(AbstractElement nodeSelection);

    void onAction(String actionName, Object... args);

    void onError(ErrorType errorType, String... parameters);
}
