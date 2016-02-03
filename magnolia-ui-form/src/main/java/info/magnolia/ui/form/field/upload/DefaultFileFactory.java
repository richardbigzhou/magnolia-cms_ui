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

import info.magnolia.i18nsystem.SimpleTranslator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyuploads.FileFactory;

/**
 * Default implementation of {@link FileFactory}.<br>
 */
public class DefaultFileFactory implements FileFactory {

    private File directory;

    public DefaultFileFactory(File directory, SimpleTranslator i18n) {
        if (directory.isDirectory() && directory.canWrite()) {
            this.directory = directory;
        } else {
            throw new IllegalArgumentException(i18n.translate("ui-form.filefactory.nonexitantdirectory"));
        }
    }

    @Override
    public File createFile(String fileName, String mimeType) {
        try {
            File tmpFile = File.createTempFile(StringUtils.rightPad(fileName, 5, "x"), null, directory);
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
