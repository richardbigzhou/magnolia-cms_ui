/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.framework.shell;

import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.overlay.OverlayCloser;
import info.magnolia.ui.framework.overlay.OverlayLayer;
import info.magnolia.ui.vaadin.overlay.Overlay;
import info.magnolia.ui.vaadin.view.View;

import java.util.List;

/**
 * Decouples the presenters and the Vaadin application. Provides methods to show messages and configuration dialogs.
 */
public interface Shell extends OverlayLayer {

    void askForConfirmation(String message, ConfirmationHandler listener);

    void showNotification(String message);

    void showError(String message, Exception e);

    String getFragment();

    void setFragment(String fragment);

    HandlerRegistration addFragmentChangedHandler(FragmentChangedHandler handler);

    void registerApps(List<String> appNames);

    void pushToClient();

    void showInfo(Message message);

    void showError(Message message);

    void showWarning(Message message);

    /**
     * Open an Overlay on top of a specific View.
     * 
     * @param view
     * View to be displayed over the view.
     * @param parent
     * The View to open the Overlay on top of.
     */
    OverlayCloser openOverlayOnView(View view, View parent, Overlay.ModalityDomain modalityLocation, Overlay.ModalityLevel modalityLevel);

}
