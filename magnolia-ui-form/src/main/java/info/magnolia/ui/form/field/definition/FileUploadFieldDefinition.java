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
package info.magnolia.ui.form.field.definition;

/**
 * Field definition for a upload field.
 */
public class FileUploadFieldDefinition extends ConfiguredFieldDefinition {

    /**
     * TODO Define the workspace here so we could instruct to save into the DAM (DMS) workspace if needed.
     */
    // Display upload file preview
    private boolean preview = true;
    // Define the upload Binary Node name.
    private String imageNodeName = "imageBinary";
    // Define the maximum file size in bite.
    private long maxUploadSize = Long.MAX_VALUE;
    // Define allowed uploadMimeType
    private String allowedMimeType = ".*";

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public String getImageNodeName() {
        return imageNodeName;
    }

    public void setImageNodeName(String imageNodeName) {
        this.imageNodeName = imageNodeName;
    }

    public long getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public String getAllowedMimeType() {
        return allowedMimeType;
    }

    public void setAllowedMimeType(String allowedMimeType) {
        this.allowedMimeType = allowedMimeType;
    }
}