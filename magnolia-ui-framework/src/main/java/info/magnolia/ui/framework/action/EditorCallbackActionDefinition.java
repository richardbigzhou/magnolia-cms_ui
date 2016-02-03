/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Configures an action which simply delegates to an {@link info.magnolia.ui.form.EditorCallback EditorCallback},
 * optionally performing validation beforehand.
 *
 * <p>This action is typically configured as a dialog/editor's 'commit' action (instead of a plain save action).
 * It allows to keep the callback code close to that of the dialog/editor opening action.
 */
public class EditorCallbackActionDefinition extends ConfiguredActionDefinition {

    private boolean callSuccess = true;

    private String successActionName = "success";

    private boolean isValidationEnabled = false;

    public EditorCallbackActionDefinition() {
        setImplementationClass(EditorCallbackAction.class);
    }

    /**
     * Defines whether the action will invoke the success callback when executed (or alternatively the cancel callback).
     * Default is <code>true</code>, i.e. success.
     *
     * @see #getSuccessActionName()
     * @see info.magnolia.ui.form.EditorCallback#onSuccess(String)
     */
    public boolean isCallSuccess() {
        return this.callSuccess;
    }

    public void setCallSuccess(boolean callSuccess) {
        this.callSuccess = callSuccess;
    }

    /**
     * Defines the success string to pass to the success callback when executed.
     * Default is <code>"success"</code>.
     *
     * @see #isCallSuccess()
     * @see info.magnolia.ui.form.EditorCallback#onSuccess(String)
     */
    public String getSuccessActionName() {
        return this.successActionName;
    }

    public void setSuccessActionName(String successActionName) {
        this.successActionName = successActionName;
    }

    /**
     * Defines whether validation should be performed before invoking the callback.
     * Default is <code>false</code>, i.e. non-validated.
     *
     * @see info.magnolia.ui.form.EditorValidator
     */
    public boolean isValidationEnabled() {
        return isValidationEnabled;
    }

    public void setValidationEnabled(boolean isValidationEnabled) {
        this.isValidationEnabled = isValidationEnabled;
    }
}
