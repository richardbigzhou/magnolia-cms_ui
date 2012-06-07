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
package info.magnolia.ui.framework.app.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.registry.ConfiguredAppDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *Test class for {AppLauncherLayout}.
 */
public class AppLauncherLayoutImplTest {

    private AppLauncherLayout appLauncherLayout;
    private AppCategory appCategory1;
    private AppCategory appCategory2;
    private AppDescriptor appDescriptor1;
    private AppDescriptor appDescriptor2;
    private AppDescriptor appDescriptor3;

    @Before
    public void setUp() throws Exception {
        //Init
        appDescriptor1 = createAppDescriptor("appDescriptor1", "appCategory1");
        appDescriptor2 = createAppDescriptor("appDescriptor2", "appCategory1");
        appDescriptor3 = createAppDescriptor("appDescriptor3", "appCategory2");
        appCategory1 =  createAppCategory("appCategory1", appDescriptor1, appDescriptor2);
        appCategory2 =  createAppCategory("appCategory2", appDescriptor3);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put("appCategory1", appCategory1);
        categories.put("appCategory2", appCategory2);
        appLauncherLayout = new AppLauncherLayoutImpl(categories);
    }

    @After
    public void tearDown() throws Exception {
        appLauncherLayout = null;
    }

    @Test
    public void TestGetCategories() {
        // GIVEN

        // WHEN
        Collection<AppCategory> appCategories = appLauncherLayout.getCategories();

        // THEN
        assertNotNull(appCategories);
        assertEquals(2, appCategories.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestGetCategory() {
        // GIVEN

        // WHEN
        AppCategory res = appLauncherLayout.getCategory("appCategory1");

        // THEN
        assertNotNull(res);
        assertEquals(res, appCategory1);

        // WHEN
        res = appLauncherLayout.getCategory("x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestGetAppDescriptor() {
        // GIVEN

        // WHEN
        AppDescriptor res = appLauncherLayout.getAppDescriptor("appDescriptor3");

        // THEN
        assertNotNull(res);
        assertEquals(res, appDescriptor3);

        // WHEN
        res = appLauncherLayout.getAppDescriptor("x");
    }

    @Test
    public void TestIsAppAlreadyRegistered() {
        // GIVEN

        // WHEN
        boolean res = appLauncherLayout.isAppAlreadyRegistered("appDescriptor3");

        // THEN
        assertEquals(res, true);

        // WHEN
        res = appLauncherLayout.isAppAlreadyRegistered("x");

        // THEN
        assertEquals(res, false);
    }



    public static AppCategory createAppCategory(String name, AppDescriptor... appDescriptors) {
        AppCategory res = new AppCategory();
        res.setLabel(name);
        for(AppDescriptor descriptor: appDescriptors) {
            res.addApp(descriptor);
        }
        return res;
    }

    public static AppDescriptor createAppDescriptor(String name, String categoryName) {
        AppDescriptor res = new ConfiguredAppDescriptor();
        res.setCategoryName(categoryName);
        res.setName(name);
        return res;
    }
}
