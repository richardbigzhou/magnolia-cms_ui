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
package info.magnolia.ui.vaadin.gwt.client.layout;

import info.magnolia.ui.vaadin.gwt.client.icon.GwtIcon;
import info.magnolia.ui.vaadin.integration.serializer.ResourceSerializer;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Thumbnail widget.
 * 
 */
public class VThumbnail extends Composite {

    private final SimplePanel panel = new SimplePanel();

    private VThumbnailData data;

    private boolean isSelected = false;

    public VThumbnail() {
        super();
        initWidget(panel);
        addStyleName("thumbnail");
    }

    public String getId() {
        return data.getId();
    }

    public void setData(VThumbnailData data) {
        this.data = data;

        if (data != null) {
            String src = data.getSrc();
            if (src != null) {
                if (src.startsWith(ResourceSerializer.RESOURCE_URI_SCHEME_ICONFONT)) {
                    // iconFont
                    GwtIcon fileIcon = new GwtIcon();
                    String iconFontClass = data.getSrc().substring(ResourceSerializer.RESOURCE_URI_SCHEME_ICONFONT.length());
                    fileIcon.updateIconName(iconFontClass);
                    panel.setWidget(fileIcon);

                } else {
                    // image
                    Image image = new Image(LazyThumbnailLayoutImageBundle.INSTANCE.getStubImage().getSafeUri());
                    // Add cachebuster so that browser definitely displays updated thumbnails after edits.
                    String cacheBuster = "?cb=" + System.currentTimeMillis();
                    image.setUrl(data.getSrc() + cacheBuster);
                    image.setStyleName("thumbnail-image");
                    panel.setWidget(image);
                }
            }
        }
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (isSelected) {
            addStyleName("selected");
        } else {
            removeStyleName("selected");
        }
    }

    public boolean isSelected() {
        return isSelected;
    }
}
