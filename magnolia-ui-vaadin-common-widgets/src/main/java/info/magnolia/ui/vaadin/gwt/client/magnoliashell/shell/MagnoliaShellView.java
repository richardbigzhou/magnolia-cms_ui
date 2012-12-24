/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.Collection;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;


/**
 * The view interface for MagnoliaShell (implemented by GWT part of MagnoliaShell).
 */
public interface MagnoliaShellView extends HasWidgets, IsWidget {

    void setActiveViewport(ViewportWidget viewport);

    void updateViewport(ViewportWidget viewport, ViewportType type);

    void setPresenter(final Presenter presenter);

    void showMessage(final MessageType type, String text, String message, String id);

    void hideAllMessages();

    void navigate(String appId, String subAppId, String parameter);

    void shiftViewportsVertically(int shiftPx, boolean animated);

    //void setPusher(VICEPush pusher);

    void setShellAppIndication(ShellAppType type, int indication);

    Presenter getPresenter();

    Collection<ViewportWidget> getViewports();

    void closeMessageEager(String id);

    void navigateToMessageDetails(String id);

    void showAppPreloader(String prefix, PreloaderCallback preloaderCallback);

    void updateShellDivet();
    
    /**
     * Presenter. Meant for Vaadin part of MagnoliaShell.
     */
    interface Presenter {

        Widget getShellAppWidget(ShellAppType type);
        
        void activateRunningApp(Fragment dto);

        void startApp(Fragment f);
        
        void loadShellApp(Fragment f);

        void updateViewportLayout(ViewportWidget activeViewport);

        void closeCurrentApp();

        void closeCurrentShellApp();

        void removeMessage(String id);

        boolean isAppRegistered(String appName);

        boolean isAppRunning(String appName);

        void handleHistoryChange(String fragment);
        
    }
}
