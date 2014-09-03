/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.context.MgnlContext;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 * FileSystemDialogDefinitionManager.
 * TODO: Add proper JavaDoc.
 */
@Singleton
public class FileSystemDialogDefinitionManager {

    @Inject
    public FileSystemDialogDefinitionManager(MgnlContext context) {
        try {
            FileSystemManager vfs =  VFS.getManager();
            String directoryName = MgnlContext.getWebContext().getServletContext().getRealPath("/") + "mgnl-files";
            FileObject file = vfs.resolveFile(directoryName);
            FileAlterationObserver observer = new FileAlterationObserver(directoryName);
            FileAlterationMonitor monitor = new FileAlterationMonitor();
            monitor.addObserver(observer);
            observer.addListener(new FileAlterationListener() {
                @Override
                public void onStart(FileAlterationObserver observer) {
                    System.out.println("test");
                }

                @Override
                public void onDirectoryCreate(File directory) {
                }

                @Override
                public void onDirectoryChange(File directory) {
                }

                @Override
                public void onDirectoryDelete(File directory) {
                }

                @Override
                public void onFileCreate(File file) {
                }

                @Override
                public void onFileChange(File file) {
                    System.out.println("test");
                }

                @Override
                public void onFileDelete(File file) {
                }

                @Override
                public void onStop(FileAlterationObserver observer) {
                }
            });
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

    }
}
