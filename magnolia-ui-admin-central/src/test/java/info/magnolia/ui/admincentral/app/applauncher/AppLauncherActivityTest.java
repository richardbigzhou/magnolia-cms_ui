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
package info.magnolia.ui.admincentral.app.applauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.ui.admincentral.app.AppTestUtility;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherActivity;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherViewImpl;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppControllerImpl;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLauncherLayout;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutImpl;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutManagerImpl;
import info.magnolia.ui.framework.app.layout.event.LayoutEvent;
import info.magnolia.ui.framework.app.layout.event.LayoutEventType;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.event.SimpleSystemEventBus;
import info.magnolia.ui.framework.event.SystemEventBus;
import info.magnolia.ui.vaadin.integration.widget.AppButton;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.CssLayout;


/**
 * Main test class for {AppLauncherActivity}.
 */
public class AppLauncherActivityTest {

    private EventBus eventBus;
    private SystemEventBus systemEventBus;
    private AppLauncherViewImpl view;
    private AppController appController;
    private AppLauncherLayoutManager appLauncherLayoutManager;
    private AppLauncherLayout layout;
    private AppCategory appCategory1;
    private AppCategory appCategory2;
    private AppDescriptor appDescriptor1;
    private AppDescriptor appDescriptor2;
    private AppDescriptor appDescriptor3;

    @Before
    public void setUp() {
        eventBus = new SimpleEventBus();
        systemEventBus = new SimpleSystemEventBus();
        view = new AppLauncherViewImpl();
        layout = createLayout();
        appLauncherLayoutManager = mock(AppLauncherLayoutManagerImpl.class);
        when(appLauncherLayoutManager.getLayout()).thenReturn(layout);
        when(appLauncherLayoutManager.isAppDescriptionRegistered("appDescriptor1")).thenReturn(true);
        when(appLauncherLayoutManager.isAppDescriptionRegistered("appDescriptor2")).thenReturn(true);
        when(appLauncherLayoutManager.isAppDescriptionRegistered("appDescriptor3")).thenReturn(true);

        appController = mock(AppControllerImpl.class);
    }


    private AppLauncherLayout createLayout() {
        appDescriptor1 = AppTestUtility.createAppDescriptor("appDescriptor1", null);
        appDescriptor2 = AppTestUtility.createAppDescriptor("appDescriptor2", null);
        appDescriptor3 = AppTestUtility.createAppDescriptor("appDescriptor3", null);
        appCategory1 =  AppTestUtility.createAppCategory("appCategory1", appDescriptor1, appDescriptor2);
        appCategory2 =  AppTestUtility.createAppCategory("appCategory2", appDescriptor3);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put("appCategory1", appCategory1);
        categories.put("appCategory2", appCategory2);
        return new AppLauncherLayoutImpl(categories);
    }

    @Test
    public void testLayoutEvent() {
        // GIVEN
        new AppLauncherActivity(view, appController, appLauncherLayoutManager, eventBus, systemEventBus);
        CssLayout component =  (CssLayout)view.asVaadinComponent();
        assertEquals(AppLauncherViewImplTest.appLauncherStyleName,component.getStyleName());
        AppButton appButtonBeforeReload = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButtonBeforeReload.isActive(),false);

        // WHEN
        systemEventBus.fireEvent(new LayoutEvent(LayoutEventType.RELOAD_APP, "appDescriptor1"));

        // THEN
        // Shoud have a new Button id
        AppButton appButtonAfterReload = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButtonBeforeReload.getStyleName(),appButtonAfterReload.getStyleName());
        assertNotSame(appButtonBeforeReload,appButtonAfterReload);
        assertEquals(appButtonAfterReload.isActive(),false);
    }

    @Test
    public void testLayoutEvent_ActiveButton() {
        // GIVEN
        new AppLauncherActivity(view, appController, appLauncherLayoutManager, eventBus, systemEventBus);
        CssLayout component =  (CssLayout)view.asVaadinComponent();
        assertEquals(AppLauncherViewImplTest.appLauncherStyleName,component.getStyleName());
        AppButton appButtonBeforeReload = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButtonBeforeReload.isActive(),false);

        when(appController.isAppStarted(appDescriptor1)).thenReturn(true);

        // WHEN
        systemEventBus.fireEvent(new LayoutEvent(LayoutEventType.RELOAD_APP, "appDescriptor1"));

        // THEN
        // Shoud have a new Button id
        AppButton appButtonAfterReload = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButtonBeforeReload.getStyleName(),appButtonAfterReload.getStyleName());
        assertNotSame(appButtonBeforeReload,appButtonAfterReload);
        assertEquals(appButtonAfterReload.isActive(),true);
    }

    @Test
    public void testAppEvent_Start() {
        // GIVEN
        new AppLauncherActivity(view, appController, appLauncherLayoutManager, eventBus, systemEventBus);
        CssLayout component =  (CssLayout)view.asVaadinComponent();
        assertEquals(AppLauncherViewImplTest.appLauncherStyleName,component.getStyleName());
        AppButton appButton = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButton.isActive(),false);

        // WHEN
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor1, AppEventType.STARTED));

        // THEN
        assertEquals(appButton.isActive(),true);
    }

    @Test
    public void testAppEvent_Stop() {
        // GIVEN
        when(appController.isAppStarted(appDescriptor1)).thenReturn(true);
        new AppLauncherActivity(view, appController, appLauncherLayoutManager, eventBus, systemEventBus);
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor1, AppEventType.STARTED));
        CssLayout component =  (CssLayout)view.asVaadinComponent();
        assertEquals(AppLauncherViewImplTest.appLauncherStyleName,component.getStyleName());
        AppButton appButton = AppLauncherViewImplTest.getComponent( AppButton.class,  "appDescriptor1_label", null, component);
        assertEquals(appButton.isActive(),true);

        // WHEN
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor1, AppEventType.STOPPED));

        // THEN
        assertEquals(appButton.isActive(),false);
    }

}
