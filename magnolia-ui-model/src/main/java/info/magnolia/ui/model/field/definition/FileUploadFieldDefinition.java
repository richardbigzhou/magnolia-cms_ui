/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.field.definition;

/**
 * Field definition for a upload field.
 */
public class FileUploadFieldDefinition extends ConfiguredFieldDefinition {

    // Display Thumbnail
    private boolean preview = true;
    // Display Image Info
    private boolean info = true;
    // Define the upload Binary Node name.
    private String imageNodeName = "imageBinary";
    // Define If the Image can be removed (Display a delete Button)
    private boolean fileDeletesAllowed = false;

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isInfo() {
        return info;
    }

    public void setInfo(boolean info) {
        this.info = info;
    }

    public String getImageNodeName() {
        return imageNodeName;
    }

    public void setImageNodeName(String imageNodeName) {
        this.imageNodeName = imageNodeName;
    }

    public boolean isFileDeletesAllowed() {
        return fileDeletesAllowed;
    }

    public void setFileDeletesAllowed(boolean fileDeletesAllowed) {
        this.fileDeletesAllowed = fileDeletesAllowed;
    }
}