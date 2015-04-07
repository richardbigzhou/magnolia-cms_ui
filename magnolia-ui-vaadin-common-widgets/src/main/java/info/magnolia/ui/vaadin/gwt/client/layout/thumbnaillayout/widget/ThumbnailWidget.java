/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.IconWidget;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.ServerConnector;

/**
 * Thumbnail widget.
 *
 * @deprecated since 5.3.9 - Not used any more in favor of mere dom elements in
 *             order to avoid the widget usage overhead for the case of numerous thumbnails.
 */
@Deprecated
public class ThumbnailWidget extends Composite {

    private final SimplePanel panel = new SimplePanel();

    private ThumbnailData data;

    private boolean isSelected = false;

    public ThumbnailWidget() {
        super();
        initWidget(panel);
        addStyleName("thumbnail");
    }

    public String getId() {
        return data.getThumbnailId();
    }

    public void setData(ThumbnailData data, ServerConnector connector) {
        this.data = data;
        if (data.isRealResource()) {
            Image image = new Image(LazyThumbnailLayoutImageBundle.INSTANCE.getStubImage().getSafeUri());
            image.setUrl(connector.getState().resources.get(data.getThumbnailId()).getURL());
            image.setStyleName("thumbnail-image");
            panel.setWidget(image);
        } else {
            // iconFont
            IconWidget fileIcon = new IconWidget();
            String iconFontClass = data.getIconFontId();
            fileIcon.setIconName(iconFontClass);
            panel.setWidget(fileIcon);
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
