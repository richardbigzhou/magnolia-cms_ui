/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.api.action;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.i18nsystem.I18nable;
import info.magnolia.ui.api.availability.AvailabilityDefinition;

/**
 * Action definitions are used to configure actions in many parts of the UI. The definition holds a name which is used
 * to identify the action within a certain scope, for instance within a sub app. Many actions have dedicated action
 * definition classes implementing this interface that allows supplying additional parameters to the action.
 * Implementations are expected to provide correct {@link Object#equals(Object)} and {@link Object#hashCode()} methods.
 * 
 * @see Action
 * @see ActionExecutor
 */
@I18nable(keyGenerator = ActionDefinitionKeyGenerator.class)
public interface ActionDefinition {

    String getName();

    @I18nText
    String getLabel();

    String getIcon();

    String getI18nBasename();

    @I18nText
    String getDescription();

    @I18nText
    String getSuccessMessage();

    @I18nText
    String getFailureMessage();

    @I18nText
    String getErrorMessage();

    Class<? extends Action> getImplementationClass();

    AvailabilityDefinition getAvailability();
}
