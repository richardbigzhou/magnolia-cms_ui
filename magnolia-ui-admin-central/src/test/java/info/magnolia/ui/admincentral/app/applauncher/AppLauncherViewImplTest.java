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
import static org.junit.Assert.assertNotNull;
import info.magnolia.ui.admincentral.app.simple.AppTestUtility;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherViewImpl;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherViewImpl.AppGroupComponent;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayoutImpl;
import info.magnolia.ui.vaadin.integration.widget.AppButton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Simple AppLauncherViewImpl test class.
 *
 */
public class AppLauncherViewImplTest {

    private AppLauncherViewImpl appLauncherViewImpl;
    public static String appLauncherStyleName = "app-launcher";
    public static  String appLauncherGroupStyleName = "app-list";
    public static  String appLauncherCssLayoutStyleName = "clearfix";


    @Before
    public void setUp() {
        initAppLauncherViewImpl();
    }


    private void initAppLauncherViewImpl() {
        appLauncherViewImpl = new AppLauncherViewImpl();
    }


    @Test
    public void testAsVaadinComponent() {
        // GIVEN
        String cat_1 = "cat_1";
        AppDescriptor descriptor_1 = AppTestUtility.createAppDescriptor("1", null);
        AppCategory category = AppTestUtility.createAppCategory(cat_1, descriptor_1);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(cat_1, category);
        // WHEN
        appLauncherViewImpl.registerApp(new AppLayoutImpl(categories));

        // THEN
        CssLayout component =  (CssLayout)appLauncherViewImpl.asVaadinComponent();
        assertEquals("", appLauncherStyleName,component.getStyleName());

        //Get AppGroupComponent
        AppGroupComponent groupComponent = getComponent( AppGroupComponent.class,  null, appLauncherGroupStyleName+" "+cat_1,  component);
        assertNotNull("Should not be null", groupComponent);

        //Get the CssLayout
        CssLayout cssLayoutComponent =  getComponent( CssLayout.class,  null, null, component);
        assertNotNull("Should not be null", cssLayoutComponent);
        assertEquals(appLauncherCssLayoutStyleName,cssLayoutComponent.getStyleName());

        //Get Button
        AppButton appButton = getComponent( AppButton.class,  "1_label", null, component);
        assertNotNull("Should not be null", appButton);
        assertEquals(false,appButton.isActive());

    }

    @Test
    public void testRegisterApp_2App_1Group() {
        // GIVEN
        String cat_1 = "cat_1";
        AppDescriptor descriptor_1 = AppTestUtility.createAppDescriptor("1", null);
        AppDescriptor descriptor_2 = AppTestUtility.createAppDescriptor("2",null);
        AppCategory category = AppTestUtility.createAppCategory(cat_1, descriptor_1,descriptor_2);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(cat_1, category);
        // WHEN
        appLauncherViewImpl.registerApp(new AppLayoutImpl(categories));

        // THEN
        CssLayout component =  (CssLayout)appLauncherViewImpl.asVaadinComponent();
        //Get AppGroupComponent
        AppGroupComponent groupComponent = getComponent( AppGroupComponent.class,  null, appLauncherGroupStyleName+" "+cat_1,  component);
        assertNotNull("Should not be null", groupComponent);
        //Get Button
        AppButton appButton1 = getComponent( AppButton.class,  "1_label", null, component);
        assertNotNull("Should not be null", appButton1);
        assertEquals(false,appButton1.isActive());
        AppButton appButton2 = getComponent( AppButton.class,  "2_label", null, component);
        assertNotNull("Should not be null", appButton2);
        assertEquals(false,appButton2.isActive());
    }

    @Test
    public void testRegisterApp_2App_2Group() {
        // GIVEN
        String cat_1 = "cat_1";
        String cat_2 = "cat_2";
        AppDescriptor descriptor_1 = AppTestUtility.createAppDescriptor("1", null);
        AppDescriptor descriptor_2 = AppTestUtility.createAppDescriptor("2",null);
        AppCategory category1 = AppTestUtility.createAppCategory(cat_1, descriptor_1);
        AppCategory category2 = AppTestUtility.createAppCategory(cat_2, descriptor_2);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(cat_1, category1);
        categories.put(cat_2, category2);
        // WHEN
        appLauncherViewImpl.registerApp(new AppLayoutImpl(categories));

        // THEN
        CssLayout component =  (CssLayout)appLauncherViewImpl.asVaadinComponent();
        //Get AppGroupComponent
        AppGroupComponent groupComponent_1 = getComponent( AppGroupComponent.class,  null, appLauncherGroupStyleName+" "+cat_1,  component);
        assertNotNull("Should not be null", groupComponent_1);
        AppGroupComponent groupComponent_2 = getComponent( AppGroupComponent.class,  null, appLauncherGroupStyleName+" "+cat_2,  component);
        assertNotNull("Should not be null", groupComponent_2);

        //Get Button
        AppButton appButton1 = getComponent( AppButton.class,  "1_label", null, component);
        assertNotNull("Should not be null", appButton1);
        assertEquals(groupComponent_1,appButton1.getParent().getParent());
        AppButton appButton2 = getComponent( AppButton.class,  "2_label", null, component);
        assertNotNull("Should not be null", appButton2);
        assertEquals(groupComponent_2,appButton2.getParent().getParent());
    }

    @Test(expected=NullPointerException.class)
    public void testRegisterApp_0App_1Group() {

        // GIVEN
        String cat_1 = "cat_1";
        AppDescriptor descriptor_1 = null;
        AppCategory category = AppTestUtility.createAppCategory(cat_1, descriptor_1);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(cat_1, category);
        // WHEN
        appLauncherViewImpl.registerApp(new AppLayoutImpl(categories));
    }

    /**
     * From the following incoming Component Structure:
     * CssLayout
     *   AppGroupComponent
     *      CssLayout
     *        AppButton
     *
     * Return AppGroupComponent, CssLayout or AppButton.
     *
     * @param: buttonCaption Name of the buttonCaption to return.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T getComponent(Class<T> clazz,  String buttonCaptionStyleName, String groupCaptionStyleName, CssLayout rootComponent) {

        Iterator<Component> appGroupComponentIterator = rootComponent.getComponentIterator();

        while(appGroupComponentIterator.hasNext()) {
            AppGroupComponent groupComponent = (AppGroupComponent)appGroupComponentIterator.next();
            if(AppGroupComponent.class.isAssignableFrom(clazz) && groupCaptionStyleName !=null && groupComponent.getStyleName().equals(groupCaptionStyleName)) {
                return (T)groupComponent;
            }
            Iterator<Component> groupComponentIterator = groupComponent.getComponentIterator();
            while(groupComponentIterator.hasNext()) {
                Component c = groupComponentIterator.next();
                if (c instanceof CssLayout) {
                    if(clazz.isInstance(c) ) {
                        return (T)c;
                    }
                    Iterator<Component> cssLayoutIterator = ((CssLayout)c).getComponentIterator();
                    while (cssLayoutIterator.hasNext()) {
                        AppButton butComponent = (AppButton)(cssLayoutIterator.next());
                        if(clazz.isInstance(butComponent) && buttonCaptionStyleName !=null && butComponent.getCaption().equals(buttonCaptionStyleName)) {
                            return (T)butComponent;
                        }
                    }
                }
            }
        }
        return null;
    }

}
