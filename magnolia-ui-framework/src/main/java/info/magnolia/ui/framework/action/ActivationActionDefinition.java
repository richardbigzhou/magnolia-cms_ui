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
package info.magnolia.ui.framework.action;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.CommandActionDefinition;

/**
 * Activation action definition. By default performs a non-recursive activation.
 * 
 * @see ActivationAction
 */
public class ActivationActionDefinition extends CommandActionDefinition {

    private boolean recursive = false;
    private String messageView;
    private String workflowSuccessMessage;
    private String workflowFailureMessage;
    private String workflowErrorMessage;

    // Workaround for MAGNOLIA-5317.
    private Class<? extends Action> implementationClass = ActivationAction.class;

    public ActivationActionDefinition() {
        // Workaround for MAGNOLIA-5317.
        // setImplementationClass(ActivationAction.class);
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }

    @I18nText
    public String getWorkflowSuccessMessage() {
        return workflowSuccessMessage;
    }

    public void setWorkflowSuccessMessage(String workflowSuccessMessage) {
        this.workflowSuccessMessage = workflowSuccessMessage;
    }

    @I18nText
    public String getWorkflowFailureMessage() {
        return workflowFailureMessage;
    }

    public void setWorkflowFailureMessage(String workflowFailureMessage) {
        this.workflowFailureMessage = workflowFailureMessage;
    }

    @I18nText
    public String getWorkflowErrorMessage() {
        return workflowErrorMessage;
    }

    public void setWorkflowErrorMessage(String workflowErrorMessage) {
        this.workflowErrorMessage = workflowErrorMessage;
    }

    public String getMessageView() {
        return messageView;
    }

    public void setMessageView(String messageView) {
        this.messageView = messageView;
    }

    // Workaround for MAGNOLIA-5317.
    @Override
    public Class<? extends Action> getImplementationClass() {
        return implementationClass;
    }

    // Workaround for MAGNOLIA-5317.
    @Override
    public void setImplementationClass(Class<? extends Action> implementationClass) {
        this.implementationClass = implementationClass;
    }
}
