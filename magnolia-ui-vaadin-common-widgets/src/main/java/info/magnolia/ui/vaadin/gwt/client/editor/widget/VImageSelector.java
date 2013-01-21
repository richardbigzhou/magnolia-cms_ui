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
package info.magnolia.ui.vaadin.gwt.client.editor.widget;

import info.magnolia.ui.vaadin.gwt.client.editor.shared.SelectionArea;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Client side implementation for ImageEditor widget.
 */
public class VImageSelector extends VerticalPanel {

    public static final String CLASSNAME = "v-image-editor";

    private int margins = 0;

    private int nativeImageWidth;

    private int nativeImageHeight;

    private int explicitWidth = 0;

    private int explicitHeight = 0;

    private double scaleRatio = 1d;

    private boolean isCropping = false;

    private Image img = null;

    private final ImageSelectorWidget selector = new ImageSelectorWidget();

    private final Label scaleLabel = new Label();

    private final Label fileNameLabel = new Label();

    private final Label sizeLabel = new Label();

    private final Label mimeLabel = new Label();

    public void setMimeType(String mimeType) {
        mimeLabel.setText(mimeType);
    }

    public void setSource(String sourceUrl) {
        if (img != null) {
            if (img.getUrl().equals(sourceUrl)) {
                return;
            } else {
                remove(img);
            }
        }
        img = new Image(sourceUrl);
        img.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                nativeImageWidth = img.getOffsetWidth();
                nativeImageHeight = img.getOffsetHeight();
                sizeLabel.setText(nativeImageWidth + " x " + nativeImageHeight);
                updateImage();
            }
        });
        insert(img, 1);
    }

    public void setMarginsPx(int marginsPx) {
        this.margins = marginsPx;
        updateImage();
    }

    public SelectionArea getSelectionArea() {
        int x = (int) (selector.getSelectionXCoordinate() / scaleRatio);
        int y = (int) (selector.getSelectionYCoordinate() / scaleRatio);
        int w = (int) (selector.getSelectionWidth() / scaleRatio);
        int h = (int) (selector.getSelectionHeight() / scaleRatio);
        return new SelectionArea(x, y, w, h);
    }

    public void setFileName(String fileName) {
        fileNameLabel.setText(fileName);
    }

    public void setMinDimension(int minDimension) {

    }

    public void setIsCropAspectRatioLocked(boolean isAspectRatioLocked) {
        if (selector != null) {
            selector.setAspectRatio(isAspectRatioLocked ? img.getOffsetWidth() * 1d / img.getOffsetHeight() : -1);
        }
    }

    public VImageSelector() {
        setStyleName(CLASSNAME);
        getElement().getStyle().setBackgroundColor("rgba(51,51,51,1)");
        setHorizontalAlignment(ALIGN_CENTER);
        setVerticalAlignment(ALIGN_MIDDLE);

        scaleLabel.getElement().getStyle().setColor("#FFFFFF");
        fileNameLabel.getElement().getStyle().setColor("#FFFFFF");

        final HorizontalPanel details = new HorizontalPanel();
        details.getElement().getStyle().setColor("#FFFFFF");
        details.setWidth("360px");

        details.add(fileNameLabel);
        details.add(sizeLabel);
        details.add(mimeLabel);
        add(details);
        add(scaleLabel);
    }

    public void scale(double ratio) {
        if (selector.isAttached()) {
            selector.scale(ratio);
        }
    }

    public void setIsCropping(boolean isCropping) {
        if (isCropping != this.isCropping) {
            if (isCropping) {
                remove(img);
                selector.cropImage(img);
                insert(selector, 1);
            } else {
                remove(selector);
                img.setStyleName("");
                insert(img, 1);
            }
        }
    }

    private void updateImage() {
        if (nativeImageHeight > 0 && nativeImageWidth > 0) {
            int width = explicitWidth == 0 ? nativeImageWidth : explicitWidth - 2 * margins;
            int height = explicitHeight == 0 ? nativeImageHeight : explicitHeight - 2 * margins;

            double heightRatio = height * 1d / nativeImageHeight;
            double widthRatio = width * 1d / nativeImageWidth;
            scaleRatio = Math.min(heightRatio, widthRatio);

            img.setWidth((int) (nativeImageWidth * scaleRatio) + "px");
            img.setHeight((int) (nativeImageHeight * scaleRatio) + "px");

            scaleLabel.setText("Showing " + (int) (width * 1d / nativeImageWidth * 100) + "% of original size");
        }
    }

    public void adjustWidth(int outerWidth) {
        this.explicitWidth = outerWidth;
        if (selector.isAttached()) {
            selector.setWidth(img.getWidth() + "px");
        }
    }

    public void adjustHeight(int outerHeight) {
        this.explicitHeight = outerHeight;
        if (selector.isAttached()) {
            selector.setHeight(img.getHeight() + "px");
        }
    }
}
