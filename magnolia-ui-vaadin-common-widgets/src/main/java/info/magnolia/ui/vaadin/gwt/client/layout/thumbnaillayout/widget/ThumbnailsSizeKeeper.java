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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.vaadin.client.ComputedStyle;

/**
 * Tracks the base sizes of the thumbnails set from the server, applies those to all the thumbnails via generated CSS rules,
 * calculates the resulting actual size. Also is capable of applying a scaling ratio to the thumbnails.
 */
class ThumbnailsSizeKeeper {

    private static final int MAX_SIZE = 250;

    private int calculatedWidth;

    private int calculatedHeight;

    private int baseWidth;

    private int baseHeight;

    private Element thumbnailParent;

    private float ratio;

    private int unscaledWidth;

    private int unscaledHeight;

    ThumbnailsSizeKeeper(Element thumbnailParent) {
        this.thumbnailParent = thumbnailParent;
    }

    void scale(float ratio) {
        this.ratio = ratio;
        doUpdateAllThumbnailsSize(scaleDimension(baseWidth, ratio), scaleDimension(baseHeight, ratio));
        updateCalculatedOffsetSizes();
    }

    public void scaleToWidth(int width) {
        int targetWidth = Math.min(width, MAX_SIZE);
        if (baseWidth < MAX_SIZE) {
            scale((float)(targetWidth - unscaledWidth) / (MAX_SIZE - baseWidth));
        }
    }

    int height() {
        return this.calculatedHeight;
    }

    int width() {
        return this.calculatedWidth;
    }

    boolean updateAllThumbnailsSize(int width, int height) {
        if (this.baseHeight != height || this.baseWidth != width) {
            this.baseHeight = height;
            this.baseWidth = width;

            doUpdateAllThumbnailsSize(width, height);
            updateCalculatedOffsetSizes();
            return true;
        }

        return false;
    }

    void applySizeToThumbnail(Element thumbnail) {
        doSetThumbnailSize(scaleDimension(baseWidth, ratio), scaleDimension(baseHeight, ratio), thumbnail);
    }

    private int scaleDimension(int minValue, double ratio) {
        if (minValue == MAX_SIZE) {
            return MAX_SIZE;
        }
        return (int) ((MAX_SIZE - minValue) * ratio + minValue);
    }

    private void doUpdateAllThumbnailsSize(int width, int height) {
        Node element = thumbnailParent.getFirstChildElement();
        while (element != null) {
            doSetThumbnailSize(width, height, Element.as(element));
            element = element.getNextSibling();
        }
    }

    private void doSetThumbnailSize(int width, int height, Element element) {
        final Style style = element.getStyle();
        style.setFontSize(width * 0.75d, Style.Unit.PX);
        style.setWidth(width, Style.Unit.PX);
        style.setHeight(height, Style.Unit.PX);

        Style imageStyle = element.getElementsByTagName(ImageElement.TAG).getItem(0).getStyle();
        imageStyle.setProperty("maxWidth", width + "px");
        imageStyle.setProperty("maxHeight", height + "px");
    }

    private void updateCalculatedOffsetSizes() {
        ComputedStyle cs;

        int[] padding;
        int[] border;
        int[] margin;

        if (thumbnailParent.getChildCount() > 0) {
            final Element firstThumbnail = Element.as(thumbnailParent.getFirstChild());
            cs = new ComputedStyle(firstThumbnail);

            padding = cs.getPadding();
            border = cs.getBorder();
            margin = cs.getMargin();
        } else {
            final Element stub = Element.as(DOM.createDiv());
            stub.addClassName("thumbnail");
            thumbnailParent.appendChild(stub);
            cs = new ComputedStyle(stub);
            padding = cs.getPadding();
            border = cs.getBorder();
            margin = cs.getMargin();

            thumbnailParent.removeChild(stub);
        }


        final int horizontalDecorations = padding[1] + padding[3] + border[1] + border[3] + margin[1] + margin[3];
        final int verticalDecorations = padding[0] + padding[2] + border[0] + border[2] + margin[0] + margin[2];

        this.unscaledWidth = baseWidth + horizontalDecorations;
        this.unscaledHeight = baseHeight + verticalDecorations;

        this.calculatedWidth = scaleDimension(unscaledWidth, ratio);
        this.calculatedHeight = scaleDimension(unscaledHeight, ratio);

    }

    float getScaleRatio() {
        return ratio;
    }

    int getUnscaledWidth() {
        return unscaledWidth;
    }

    int getUnscaledHeight() {
        return unscaledHeight;
    }
}
