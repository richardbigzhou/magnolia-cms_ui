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
package info.magnolia.ui.form.field.upload;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.io.Files;

/**
 * Main test class for {@link DefaultFileFactory}.
 */
public class DefaultFileFactoryTest {

    private DefaultFileFactory defaultFieldFactory;
    private File directory;

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFileNoWriteAccess() {
        // GIVEN
        directory = Files.createTempDir();
        directory.setReadOnly();

        // WHEN
        defaultFieldFactory = new DefaultFileFactory(directory);

        // THEN
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFileNoValidDirectory() throws IOException {
        // GIVEN
        directory = File.createTempFile("", null);

        // WHEN
        defaultFieldFactory = new DefaultFileFactory(directory);

        // THEN
    }

    @Test
    public void testCreateFile() throws IOException {
        // GIVEN
        directory = Files.createTempDir();
        defaultFieldFactory = new DefaultFileFactory(directory);
        String fileName = "fileName";

        // WHEN
        File res = defaultFieldFactory.createFile(fileName, null);

        // THEN
        assertNotNull(res);
        assertTrue(res.getName().startsWith("fileName"));
        assertTrue(res.getName().endsWith(".tmp"));

    }

}
