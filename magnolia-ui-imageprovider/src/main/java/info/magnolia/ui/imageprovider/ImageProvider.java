/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.imageprovider;

/**
 * The {@link ImageProvider} provides portrait or thumbnail images for arbitrary content.
 * It is primarily used to streamline preview images across the Magnolia UI.
 */
public interface ImageProvider {

    String PORTRAIT_GENERATOR = "portrait";
    String THUMBNAIL_GENERATOR = "thumbnail";

    /**
     * Gets an image preview for the content represented by the given itemId.
     * This preview may be an image or an icon related to the content type.
     *
     * @param itemId the id of an {@link com.vaadin.data.Item Item}
     * @param generator the name of a registered {@link info.magnolia.imaging.ImageGenerator ImageGenerator} of the imaging module
     * @return a Vaadin {@link com.vaadin.server.Resource Resource} or a String corresponding to an icon class name
     */
    Object getThumbnailResource(Object itemId, String generator);

    /**
     * Gets an icon class name for the given media type.
     *
     * @param mimeType a MIME type
     * @return a String corresponding to an icon class name
     */
    String resolveIconClassName(String mimeType);

    /**
     * @return a link to the generated "portrait" preview for the content represented by the given itemId.
     */
    String getPortraitPath(Object itemId);

    /**
     * @return a link to the generated "thumbnail" preview for the content represented by the given itemId.
     */
    String getThumbnailPath(Object itemId);
}
