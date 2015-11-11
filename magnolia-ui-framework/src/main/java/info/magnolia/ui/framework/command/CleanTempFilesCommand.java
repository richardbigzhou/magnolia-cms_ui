/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.cms.core.Path;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.init.MagnoliaConfigurationProperties;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Cleans temp files in tmp directory.
 */
public class CleanTempFilesCommand extends MgnlCommand {

    public static final int TIME_OFFSET_IN_HOURS = -12;

    public CleanTempFilesCommand() {}

    /**
     * @deprecated since 5.4.4, use {@link #CleanTempFilesCommand()} instead.
     */
    @Deprecated
    public CleanTempFilesCommand(MagnoliaConfigurationProperties configurationProperties) {
        this();
    }

    @Override
    public boolean execute(final Context context) throws Exception {
        File tmpDir = Path.getTempDirectory();
        Date date = DateUtils.addHours(new Date(), TIME_OFFSET_IN_HOURS); // current time minus 12 hours
        Iterator<File> files = FileUtils.iterateFiles(tmpDir, FileFilterUtils.ageFileFilter(date), FileFilterUtils.ageFileFilter(date));
        while (files.hasNext()) {
            files.next().delete();
        }
        return true;
    }
}
