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
package info.magnolia.ui.framework.context;

import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.overlay.OverlayPresenter;

/**
 * Abstract basic implementation of {@link UiContext}. Delegates method calls to {@link OverlayPresenter}.
 */
public abstract class AbstractUIContext implements UiContext {

    private OverlayPresenter overlayPresenter;

    public AbstractUIContext() {
        super();
        overlayPresenter = initializeOverlayPresenter();
    }

    protected abstract OverlayPresenter initializeOverlayPresenter();

    @Override
    public OverlayCloser openOverlay(View view) {
        return overlayPresenter.openOverlay(view);
    }

    @Override
    public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
        return overlayPresenter.openOverlay(view, modalityLevel);
    }

    @Override
    public void openAlert(MessageStyleType type, View body, String okButton, AlertCallback callback) {
        overlayPresenter.openAlert(type, body, okButton, callback);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, String body, String okButton, AlertCallback callback) {
        overlayPresenter.openAlert(type, title, body, okButton, callback);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, View body, String okButton, AlertCallback callback) {
        overlayPresenter.openAlert(type, title, body, okButton, callback);
    }

    @Override
    public void openConfirmation(MessageStyleType type, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback) {
        overlayPresenter.openConfirmation(type, body, confirmButton, cancelButton, cancelIsDefault, callback);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, String body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback) {
        overlayPresenter.openConfirmation(type, title, body, confirmButton, cancelButton, cancelIsDefault, callback);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback) {
        overlayPresenter.openConfirmation(type, title, body, confirmButton, cancelButton, cancelIsDefault, callback);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        overlayPresenter.openNotification(type, doesTimeout, viewToShow);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        overlayPresenter.openNotification(type, doesTimeout, title);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        overlayPresenter.openNotification(type, doesTimeout, title, linkText, cb);
    }

}
