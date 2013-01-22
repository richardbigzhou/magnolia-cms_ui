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
 * Configure the UploadFileField based on the UploadField Definition.
 * NOTE: verbatim copy of the corresponding class from EasyUploads vaadin add-on
 *
 * @link{http://code.google.com/p/easyuploads-addon/}.
 */
public interface UploadFileField {

    /**
     * Define if the preview Image/Icon has to be displayed.
     */
    public void setPreview(boolean preview);

    /**
     * Define if the Uploaded file Info has to be displayed.
     */
    public void setInfo(boolean info);

    /**
     * Define if the Progress Bar has to be displayed.
     */
    public void setProgressInfo(boolean progressInfo);

    /**
     * Define if the Uploaded file can be deleted.
     *
     * @param: fileDeletion true will add a delete Button.
     */
    public void setFileDeletion(boolean fileDeletion);

    /**
     * Define if the Drag And Drop is allowed.
     */
    public void setDragAndDrop(boolean dragAndDrop);

    /**
     * Set the Upload Button Caption.
     */
    public void setUploadButtonCaption(String uploadButtonCaption);

    /**
     * Define the Tmp Folder used by the main Upload process.
     */
    public void setUploadFileDirectory(File directory);

    /**
     * Define the Maximum Upload File size in bytes.
     */
    public void setMaxUploadSize(long maxUploadSize);

}
