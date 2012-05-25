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
package info.magnolia.m5admincentral.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.event.SimpleEventBus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



/**
 *
 */
public class AppControllerImplTest {

    private AppRegistryImpl appRegistery;
    private AppControllerImpl appController;
    private TestAppLifecycleEventHandler appLifecycleEventHandler;
    private AppDescriptor appDescriptor_1_1_cat_1;
    private AppDescriptor appDescriptor_1_2_cat_2;
    private TestAppLifecycle_1 testAppLifecycle_1;
    private TestAppLifecycle_2 testAppLifecycle_2;

    @Before
    public void setUp() {
        initAppRegistry();

        testAppLifecycle_1 = new TestAppLifecycle_1();
        testAppLifecycle_2 = new TestAppLifecycle_2();
        ComponentProvider cprovider = mock(ComponentProvider.class);
        when(cprovider.newInstance(TestAppLifecycle_1.class)).thenReturn(testAppLifecycle_1);
        when(cprovider.newInstance(TestAppLifecycle_2.class)).thenReturn(testAppLifecycle_2);

        SimpleEventBus eventBus = new SimpleEventBus();
        appLifecycleEventHandler =  new TestAppLifecycleEventHandler();
        eventBus.addHandler(AppLifecycleEvent.class, appLifecycleEventHandler);

        appController = new AppControllerImpl(appRegistery, cprovider, eventBus);
    }

    /**
     * Init an App Registry with 2 cat.
     * Each cat contain one App.
     * App are inner class TestAppLifecycle_1 & 2
     */
     private void initAppRegistry() {
         appRegistery = new AppRegistryImpl();
         //Register App 1_1 on cat_1
         String cat_1 = "cat_1";
         AppCategory appCategory_1 = new AppCategory();
         appCategory_1.setLabel(cat_1);
         appDescriptor_1_1_cat_1 = AppTestUtility.createAppDescriptor("1_1",TestAppLifecycle_1.class);
         appCategory_1.addApp(appDescriptor_1_1_cat_1);

         //Register App 2_1 on cat_2
         String cat_2 = "cat_2";
         AppCategory appCategory_2 = new AppCategory();
         appCategory_2.setLabel(cat_2);
         appDescriptor_1_2_cat_2 = AppTestUtility.createAppDescriptor("1_2",TestAppLifecycle_2.class);
         appCategory_2.addApp(appDescriptor_1_2_cat_2);

         appRegistery.addCategory(appCategory_1);
         appRegistery.addCategory(appCategory_2);
     }

    @After
    public void tearDown() {

    }


    @Test
    public void testStartIfNotAlreadyRunning() {
        // GIVEN
        checkApp1(false, false, false);
        // WHEN
        appController.startIfNotAlreadyRunning("1_1_name");
        // THEN
        checkApp1(true, false, false);
        checkEvents("TestAppLifecycle_1",true, false, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartIfNotAlreadyRunningNoApp() {
        // GIVEN
        checkApp1(false, false, false);
        // WHEN
        appController.startIfNotAlreadyRunning("");
        // THEN
        checkApp1(true, false, false);
    }


    @Test
    public void testStartIfNotAlreadyRunningThenFocus() {
        // GIVEN
        checkApp1(false, false, false);
        // WHEN
        appController.startIfNotAlreadyRunningThenFocus("1_1_name");
        // THEN
        checkApp1(true, false, true);
        checkEvents("TestAppLifecycle_1",true, false, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartIfNotAlreadyRunningThenFocusNoApp() {
        // GIVEN
        checkApp1(false, false, false);
        // WHEN
        appController.startIfNotAlreadyRunningThenFocus("");
        // THEN
        checkApp1(true, false, true);
    }

    @Test
    public void testGetAppDescriptor() {
        // GIVEN
        AppDescriptor res = appController.getAppDescriptor(testAppLifecycle_1);
        assertNull(res);
        appController.startIfNotAlreadyRunningThenFocus("1_1_name");

        // WHEN
        res = appController.getAppDescriptor(testAppLifecycle_1);

        // THEN
        assertNotNull(res);
        assertEquals(res, appDescriptor_1_1_cat_1);
    }

    @Test
    public void testStopCurrentApplication_OneApp() {
        // GIVEN
        appController.startIfNotAlreadyRunningThenFocus("1_1_name");
        checkApp1(true, false, true);

        // WHEN
        appController.stopCurrentApplication();

        // THEN
        checkApp1(true, true, true);
        checkEvents("TestAppLifecycle_1",false, true, false);
    }

    @Test
    public void testStopCurrentApplication_TwoApp() {
        // GIVEN
        appController.startIfNotAlreadyRunningThenFocus("1_1_name");
        checkApp1(true, false, true);
        appController.startIfNotAlreadyRunningThenFocus("1_2_name");
        checkApp2(true, false, true);
        checkEvents("TestAppLifecycle_2",true, false, true);
        assertEquals( 1, testAppLifecycle_1.focusCalledNum);

        // WHEN
        appController.stopCurrentApplication();

        // THEN
        checkApp1(true, false, true);
        assertEquals( 2, testAppLifecycle_1.focusCalledNum);
        checkApp2(true, true, true);
    }



    @Test
    public void testStopApplication_OneApp() {
        // GIVEN
        checkApp1(false, false, false);
        appController.startIfNotAlreadyRunning("1_1_name");
        checkApp1(true, false, false);

        // WHEN
        appController.stopApplication("1_1_name");

        // THEN
        checkApp1(true, true, false);
    }

    @Test
    public void testStopApplication_TwoApp() {
        // GIVEN
        appController.startIfNotAlreadyRunningThenFocus("1_1_name");
        checkApp1(true, false, true);
        appController.startIfNotAlreadyRunningThenFocus("1_2_name");
        checkApp2(true, false, true);
        checkEvents("TestAppLifecycle_2",true, false, true);
        assertEquals( 1, testAppLifecycle_1.focusCalledNum);
        assertEquals( 1, testAppLifecycle_2.focusCalledNum);

        // WHEN
        appController.stopApplication("1_1_name");

        // THEN
        checkApp1(true, true, true);
        checkApp2(true, false, true);
        assertEquals( 2, testAppLifecycle_2.focusCalledNum);
    }

    /**
     * Private part.
     */
    private void checkApp1(boolean start, boolean stop, boolean focus) {
        assertEquals( focus, testAppLifecycle_1.focusCalled);
        assertEquals( start, testAppLifecycle_1.startCalled);
        assertEquals( stop, testAppLifecycle_1.stopCalled);
    }

    private void checkApp2(boolean start, boolean stop, boolean focus) {
        assertEquals( focus, testAppLifecycle_2.focusCalled);
        assertEquals( start, testAppLifecycle_2.startCalled);
        assertEquals( stop, testAppLifecycle_2.stopCalled);
    }

    private void checkEvents(String appClassName, boolean start, boolean stop, boolean focus) {
        assertEquals( appClassName, appLifecycleEventHandler.appClassName);
        assertEquals( focus, appLifecycleEventHandler.focusEvent);
        assertEquals( start, appLifecycleEventHandler.startEvent);
        assertEquals( stop, appLifecycleEventHandler.stopEvent);
    }

    /**
    *
    */
   private  class TestAppLifecycle_1 implements AppLifecycle {
       private boolean focusCalled = false;
       private boolean stopCalled = false;
       private boolean startCalled = false;
       private int focusCalledNum = 0;

       @Override
       public void start() {
           startCalled = true;
       }

       @Override
       public void focus() {
           focusCalled = true;
           focusCalledNum +=1;
       }

       @Override
       public void stop() {
           stopCalled = true;
       }
   }

   private  class TestAppLifecycle_2 implements AppLifecycle {
       private boolean focusCalled = false;
       private boolean stopCalled = false;
       private boolean startCalled = false;
       private int focusCalledNum = 0;

       @Override
       public void start() {
           startCalled = true;
       }

       @Override
       public void focus() {
           focusCalled = true;
           focusCalledNum +=1;
       }

       @Override
       public void stop() {
           stopCalled = true;
       }
   }

   private  class TestAppLifecycleEventHandler implements AppLifecycleEventHandler {
       private boolean focusEvent = false;
       private boolean stopEvent = false;
       private boolean startEvent = false;
       private String appClassName;
       @Override
       public void onAppFocus(AppLifecycleEvent event) {
           focusEvent = true;
           appClassName = event.getApp().getClass().getSimpleName();
       }

       @Override
       public void onStopApp(AppLifecycleEvent event) {
           stopEvent = true;
           startEvent = false;
           focusEvent = false;
           appClassName = event.getApp().getClass().getSimpleName();
       }

       @Override
       public void onStartApp(AppLifecycleEvent event) {
           startEvent = true;
           focusEvent = false;
           stopEvent = false;
           appClassName = event.getApp().getClass().getSimpleName();
       }

   }



}
