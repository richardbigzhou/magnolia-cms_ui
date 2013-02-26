/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.dialog;

import info.magnolia.ui.vaadin.gwt.client.dialog.connector.ModalState;

import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;

/**
 * A Single component container that includes a "glass" or "curtain" which dims out and prevents interaction on the elements
 * below it. It is different then a Vaadin Window in that ONLY the component that it is attached to recieves the modal glass.
 * It is only modal within the component that it is added to.
 * Positioning of the glass and component depends on one of the parents having css position set to relative or absolute.
 */
public class Modal extends AbstractSingleComponentContainer {

    /**
     *
     * The available levels of modality for opening a modal.
     * Represents what will be blocked by the opened modal.
     */
    public static enum ModalityLevel {
        NONE,
        SUB_APP,
        APP,
        ENTIRE_INTERFACE
    }

    final Modal.ModalityLevel modalityLevel;

    public Modal(final Component content, final Component modalityParent, Modal.ModalityLevel modalityLevel) {
        // setSizeFull();
        setImmediate(true);

        content.addStyleName("modal-child");
        setContent(content);

        this.modalityLevel = modalityLevel;
        getState().modalityParent = modalityParent;

        // Set style
        String cssClass = "";
        switch (modalityLevel) {
        case SUB_APP:
            cssClass="sub-app";
            break;
        case APP:
            cssClass="app";
            break;
        case ENTIRE_INTERFACE:
            cssClass="shell";
            break;
        }
        this.setStyleName(cssClass);
    }

    @Override
    protected ModalState getState() {
        return (ModalState) super.getState();
    }
}
