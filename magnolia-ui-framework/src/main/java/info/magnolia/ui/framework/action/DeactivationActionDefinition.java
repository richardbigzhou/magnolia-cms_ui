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
package info.magnolia.ui.framework.action;

import info.magnolia.ui.api.action.CommandActionDefinition;

/**
 * The deactivation action, invoking the deactivation command, and updating the UI accordingly.
 *
 * @see DeactivationAction
 */
public class DeactivationActionDefinition extends CommandActionDefinition {

    private static final String MESSAGE_KEY_SUCCESS = "action.deactivation.success";
    private static final String MESSAGE_KEY_FAILURE = "action.deactivation.failure";
    private static final String MESSAGE_KEY_ERROR = "action.deactivation.error";

    private static final String MESSAGE_KEY_WORKFLOW_SUCCESS = "action.deactivation.workflow.success";
    private static final String MESSAGE_KEY_WORKFLOW_FAILURE = "action.deactivation.workflow.failure";
    private static final String MESSAGE_KEY_WORKFLOW_ERROR = "action.deactivation.workflow.error";

    private String workflowSuccessMessage;
    private String workflowFailureMessage;
    private String workflowErrorMessage;

    public DeactivationActionDefinition() {
        setImplementationClass(DeactivationAction.class);
        setSuccessMessage(MESSAGE_KEY_SUCCESS);
        setFailureMessage(MESSAGE_KEY_FAILURE);
        setErrorMessage(MESSAGE_KEY_ERROR);
        setWorkflowSuccessMessage(MESSAGE_KEY_WORKFLOW_SUCCESS);
        setWorkflowFailureMessage(MESSAGE_KEY_WORKFLOW_FAILURE);
        setWorkflowErrorMessage(MESSAGE_KEY_WORKFLOW_ERROR);
    }

    public String getWorkflowSuccessMessage() {
        return workflowSuccessMessage;
    }

    public void setWorkflowSuccessMessage(String workflowSuccessMessage) {
        this.workflowSuccessMessage = workflowSuccessMessage;
    }

    public String getWorkflowFailureMessage() {
        return workflowFailureMessage;
    }

    public void setWorkflowFailureMessage(String workflowFailureMessage) {
        this.workflowFailureMessage = workflowFailureMessage;
    }

    public String getWorkflowErrorMessage() {
        return workflowErrorMessage;
    }

    public void setWorkflowErrorMessage(String workflowErrorMessage) {
        this.workflowErrorMessage = workflowErrorMessage;
    }
}
