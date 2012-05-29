/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.applauncher;

import info.magnolia.ui.admincentral.app.AppCategory;
import info.magnolia.ui.admincentral.app.AppDescriptor;
import info.magnolia.ui.admincentral.app.AppLifecycleEvent;
import info.magnolia.ui.admincentral.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.vaadin.integration.widget.AppButton;
import info.magnolia.ui.widget.magnoliashell.IsVaadinComponent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;


/**
 * Default view implementation for the app launcher.
 *
 */
@SuppressWarnings("serial")
public class AppLauncherViewImpl implements AppLauncherView, IsVaadinComponent {

    private Presenter presenter;

    private CssLayout layout = new CssLayout();

    private final Map<String, AppGroupComponent> appGroupMap = new HashMap<String, AppGroupComponent>();

    // Keep a list of registered AppButton.
    private final Map<AppDescriptor, AppButton> appButtons = new HashMap<AppDescriptor, AppButton>();

    @Inject
    public AppLauncherViewImpl(EventBus eventBus) {
        layout.addStyleName("app-launcher");
        // Add Handler of type AppLifecycleEventHandler in order to catch stop
        // and start App events.
        eventBus.addHandler(AppLifecycleEvent.class,
            new AppLifecycleEventHandler.Adapter() {

                @Override
                /**
                 * Deactivate the visual triangle on the App Icon.
                 */
                public void onStopApp(AppLifecycleEvent event) {
                    AppButton button = appButtons.get(event
                        .getAppDescriptor());
                    button.setActive(false);
                    layout.requestRepaintAll();
                }

                @Override
                /**
                 * Activate the visual triangle on the App Icon.
                 */
                public void onStartApp(AppLifecycleEvent event) {
                    AppButton button = appButtons.get(event
                        .getAppDescriptor());
                    button.setActive(true);
                    layout.requestRepaintAll();
                }

            });
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void registerApp(final AppDescriptor descriptor, AppCategory category) {
        AppGroupComponent group = appGroupMap.get(category.getLabel());
        if (group == null) {
            group = new AppGroupComponent(category.getLabel());
            appGroupMap.put(category.getLabel(), group);
            layout.addComponent(group);
        }
        group.addApp(descriptor);
    }

    /**
     * Block in the applauncher responsible for one app category.
     *
     * @version $Id$
     */
    public class AppGroupComponent extends CssLayout {

        private Label title;

        private CssLayout iconList;

        public AppGroupComponent(String title) {
            setStyleName("app-list");
            addStyleName(title);

            this.title = new Label(title);
            this.title.setStyleName("app-group-title");
            this.title.setSizeUndefined();

            this.iconList = new CssLayout();
            this.iconList.addStyleName("clearfix");

            addComponent(this.title);
            addComponent(this.iconList);
        }

        public void addApp(final AppDescriptor descriptor) {
            final AppButton button = new AppButton(descriptor.getLabel());
            button.addStyleName("item");
            button.setIcon(new ThemeResource(descriptor.getIcon()));
            this.iconList.addComponent(button);
            button.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    presenter.onAppInvoked(descriptor.getName());
                }
            });
            appButtons.put(descriptor, button);
        }
    }

    @Override
    public String getAppName() {
        return ShellAppType.APPLAUNCHER.name();
    }
}
