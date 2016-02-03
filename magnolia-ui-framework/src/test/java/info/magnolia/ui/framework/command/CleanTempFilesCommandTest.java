/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.framework.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.ui.framework.command.CleanTempFilesCommand}.
 */
public class CleanTempFilesCommandTest {

    private static final String MAGNOLIA_APP_ROOTDIR = "magnoliaAppRootDir";
    private static final String PATH_TO_TMP = MAGNOLIA_APP_ROOTDIR + File.separator + "tmp";

    private CleanTempFilesCommand cleanTempFilesCommand;
    private File tmpDir;
    private long lastModified;

    @Before
    public void setUp() throws Exception {
        cleanTempFilesCommand = new CleanTempFilesCommand();
        tmpDir = new File(PATH_TO_TMP);
        tmpDir.mkdirs();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, MAGNOLIA_APP_ROOTDIR);
        lastModified = System.currentTimeMillis() - 13 * 60 * 60 * 1000;
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir.getParentFile());
        SystemProperty.clear();
    }

    @Test
    public void testCleanTempFilesOlderThan12HoursTmpDirPathIsSetRelatively() throws Exception {
        testCleanTempFiles("tmp");
    }

    @Test
    public void testCleanTempFilesOlderThan12HoursTmpDirPathIsSetAbsolutely() throws Exception {
        testCleanTempFiles(tmpDir.getAbsolutePath());
    }

    private void testCleanTempFiles(String pathToTmpDir) throws Exception {
        //GIVEN
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, pathToTmpDir);

        File file1 = new File(PATH_TO_TMP + File.separator + "file1");
        file1.createNewFile();
        file1.setLastModified(lastModified);

        File file2 = new File(PATH_TO_TMP + File.separator + "file2");
        file2.createNewFile();

        File file3 = new File(PATH_TO_TMP + File.separator + "file3");
        file3.createNewFile();

        File file4 = new File(PATH_TO_TMP + File.separator + "file4");
        file4.createNewFile();
        file4.setLastModified(lastModified);

        //WHEN
        cleanTempFilesCommand.execute(null);
        List<File> files = Arrays.asList(tmpDir.listFiles());

        //THEN
        assertFalse(files.contains(file1));
        assertTrue(files.contains(file2));
        assertTrue(files.contains(file3));
        assertFalse(files.contains(file4));

    }

}
