/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.api.app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test for {@link AppDescriptorKeyGenerator}.
 */
public class AppDescriptorKeyGeneratorTest {

    private static final String TEST_APP = "test-app";
    private AppDescriptorKeyGenerator generator;
    private AppDescriptor app;

    @Before
    public void setUp() {
        generator = new AppDescriptorKeyGenerator();
        app = mock(AppDescriptor.class);
        when(app.getName()).thenReturn(TEST_APP);
    }

    @Test
    public void keysForLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        Method method = app.getClass().getMethod("getLabel");
        List<String> keys = new ArrayList<String>();

        // WHEN
        generator.keysFor(keys, app, method);

        // THEN
        assertEquals(2, keys.size());
        assertEquals(TEST_APP + ".app.label", keys.get(0));
        assertEquals(TEST_APP + ".app", keys.get(1));
    }

    @Test
    public void keysForIcon() throws SecurityException, NoSuchMethodException {
        // GIVEN
        Method method = app.getClass().getMethod("getIcon");
        List<String> keys = new ArrayList<String>();

        // WHEN
        generator.keysFor(keys, app, method);

        // THEN
        assertEquals(1, keys.size());
        assertEquals(TEST_APP + ".app.icon", keys.get(0));
    }
}
