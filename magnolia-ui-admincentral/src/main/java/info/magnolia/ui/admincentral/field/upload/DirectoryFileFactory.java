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
package info.magnolia.ui.admincentral.field.upload;

import java.io.File;

/**
 * DirectoryFileFactory.
 * NOTE: verbatim copy of the corresponding class from EasyUploads vaadin add-on 
 * @link{http://code.google.com/p/easyuploads-addon/}.
 */
public class DirectoryFileFactory implements FileFactory {

    private File directory;

    public DirectoryFileFactory(File directory) {
        if (directory.isDirectory() && directory.canWrite()) {
            this.directory = directory;
        } else {
            throw new IllegalArgumentException("The directory does not exist or is not writeable!");
        }
    }

    @Override
    public File createFile(String fileName, String mimeType) {
        File file = new File(directory.getPath() + "/" + fileName);
        if (file.exists()) {
            int i = 0;
            int lastIndexOf = fileName.lastIndexOf(".");
            String corepart = null;
            String extension = "";
            if (lastIndexOf > 0) {
                extension = fileName.substring(lastIndexOf);
                corepart = fileName.substring(0, lastIndexOf);
            } else {
                corepart = fileName;
            }

            while (file.exists()) {
                i++;
                file = new File(directory.getPath() + "/" + corepart + "_" + i + extension);
            }

        }
        return file;
    }

}
