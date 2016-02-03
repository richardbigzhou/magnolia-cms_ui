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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * The view interface for MagnoliaShell (implemented by GWT part of MagnoliaShell).
 */
public interface MagnoliaShellView extends HasWidgets, IsWidget {

    void setPresenter(final Presenter presenter);

    void updateViewport(ViewportWidget viewport, ViewportType type);

    void shiftViewportsVertically(int shiftPx, boolean animated);

    void setShellAppIndication(ShellAppType type, int indication);

    void showMessage(final MessageType type, String text, String message, String id);

    void hideAllMessages();

    void closeMessageEager(String id);

    void navigateToMessageDetails(String id);

    void updateShellDivet();

    boolean hasOverlay(Widget widget);

    /**
     * Add the overlayWidget to the Shell - but the DOM of the widget will be placed over
     * the DOM of the specified overlayParent.
     * The overlayWidget gets removed automatically -
     * because the component is no longer returned by MagnoliaShell iterator.
     */
    void openOverlayOnWidget(Widget overlayWidget, Widget overlayParent);

    void onShellAppStarting(ShellAppType type);

    void onAppStarting();

    void setUserMenu(Widget widget);

    /**
     * Presenter for {@link MagnoliaShellView}.
     */
    interface Presenter extends ShellAppLauncher.Listener {

        void activateShellApp(Fragment f);

        void updateViewportLayout(ViewportWidget viewport);

        void closeCurrentApp();

        void removeMessage(String id);

        void initHistory();

        void loadApp(String appName);
    }
}
