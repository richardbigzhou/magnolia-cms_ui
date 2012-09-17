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
package info.magnolia.ui.admincentral.file;

import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import org.vaadin.easyuploads.FileBuffer;

import com.vaadin.Application;
import com.vaadin.ui.Component;

/**
 * Used by File fields to handle JcrItems of type mgnl:resource.
 */
public interface FileItemWrapper {

    /**
     * Populate the jcrItem with the bean informations.
     */
    public void populateJcrItemProperty();
    /**
     * Update properties based on the informations contained in the receiver.
     */
    public void updateProperties(FileBuffer receiver);
    /**
     * Clear all properties.
     */
    public void clearProperties();
    /**
     * Create a preview Component object.
     */
    public Component createPreview(Application application);
    /**
     * Return the related JcrItem.
     */
    public JcrItemNodeAdapter getJcrItem();

    /**
     * Remove link between item and parent.
     * In this case the child File Item will not be persisted.
     */
    public void unLinkItemFromParent();

    /**
     * Return true if the binaryData is not empty.
     * false otherwise.
     */
    public boolean isEmpty();

    /**
     * Define if the File is an Image.
     */
    public boolean isImage();

    /**
     * Get Image Size.
     */
    public ImageSize getImageSize();

    public String getFileName();

    public long getFileSize();
}
