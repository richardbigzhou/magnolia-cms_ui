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

import java.io.File;

import com.vaadin.data.Item;

/**
 * Used by Upload fields to handle Items and perform the bridge between a Vaadin Item and a UploadReceiver <br>
 * <b>FileItemWrapper is used by :</b><br>
 * - Implementation class of {@link AbstractUploadField} in order to display File information's: newly Uploaded File or already Stored File by using the <b>getter's</b><br>
 * - {@link AbstractUploadField} in order to update the Item based on the Uploaded File (Dropped File): This is done during handling of events like UploadFinished or UploadFailed...
 */
public interface FileItemWrapper {
    /**
     * Populate the FileItemWrapper with the provided {@link Item}.<br>
     * Generally done in the constructor (in Builder).
     */
    public void populateFromItem(Item item);

    /**
     * Populate the FileItemWrapper with the provided {@link UploadReceiver}.
     */
    public void populateFromReceiver(UploadReceiver receiver);

    /**
     * Clear all properties.
     */
    public void clearProperties();

    /**
     * Used to restore the previous Uploaded File if existing.
     */
    public void reloadPrevious();

    /**
     * Return true if the binaryData is not empty.
     * false otherwise.
     */
    public boolean isEmpty();


    public long getFileSize();

    public String getMimeType();

    public String getExtension();

    public String getFileName();

    public File getFile();

}
