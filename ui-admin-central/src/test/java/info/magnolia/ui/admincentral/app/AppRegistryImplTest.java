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
package info.magnolia.ui.admincentral.app;


import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Simple TestClass Example.
 *
 * @version $Id$
 *
 */
public class AppRegistryImplTest {


    private AppRegistryImpl appRegistery;
    private AppDescriptor appDescriptor_1_1_cat_1;
    private AppDescriptor appDescriptor_1_2_cat_1;
    private AppDescriptor appDescriptor_1_1_cat_2;

    @Before
    public void setUp() {
        appRegistery = new AppRegistryImpl();
        String cat_1 = "cat_1";
        AppCategory appCategory_1 = new AppCategory();
        appCategory_1.setLabel(cat_1);
        appDescriptor_1_1_cat_1 = AppTestUtility.createAppDescriptor("1_1", null);
        appDescriptor_1_2_cat_1 = AppTestUtility.createAppDescriptor("1_2", null);
        appCategory_1.addApp(appDescriptor_1_1_cat_1);
        appCategory_1.addApp(appDescriptor_1_2_cat_1);

        String cat_2 = "cat_2";
        AppCategory appCategory_2 = new AppCategory();
        appCategory_2.setLabel(cat_2);
        appDescriptor_1_1_cat_2 = AppTestUtility.createAppDescriptor("1_1", null);
        appCategory_2.addApp(appDescriptor_1_1_cat_2);

        appRegistery.addCategory(appCategory_1);
        appRegistery.addCategory(appCategory_2);

    }

    @After
    public void tearDown() {

    }


    @Test
    public void testAddGetCategories() {
        // GIVEN

        // WHEN

        // THEN
        List<AppCategory> res = appRegistery.getCategories();
        assertNotNull(res);
        assertEquals("Should have two AppCategorys",2, res.size());
    }


    @Ignore
    @Test
    public void testGetAppDescriptor() {
        // GIVEN

        // WHEN

        // THEN
        AppDescriptor appDescriptor = appRegistery.getAppDescriptor("1_1_name");
        assertNotNull(appDescriptor);
        assertEquals("Should be Equal to ",appDescriptor_1_1_cat_2, appDescriptor);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAppDescriptorThrowsException() {
        // GIVEN

        // WHEN

        // THEN
        appRegistery.getAppDescriptor("");
    }

    @Test
    public void testIsAppDescriptionRegistered() {
        // GIVEN

        // WHEN

        // THEN
        boolean res = appRegistery.isAppDescriptionRegistered("");
        assertEquals("Should be false",false, res);

        res = appRegistery.isAppDescriptionRegistered("1_2_name");
        assertEquals("Should be true",true, res);
    }

}
