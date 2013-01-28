/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.editor;


import info.magnolia.ui.vaadin.editor.CroppableImage.ReleaseListener;
import info.magnolia.ui.vaadin.editor.CroppableImage.SelectionListener;
import info.magnolia.ui.vaadin.gwt.shared.jcrop.SelectionArea;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * JCropField.
 */
public class JCropField extends CustomField<SelectionArea> {

    private final JCrop jcrop;

    private final CroppableImage image = new CroppableImage();

    public JCropField() {
        this.jcrop = image.getJcrop();
        setCropVisible(true);
        setEnabled(true);
    }

    public void setAspectRatio(double aspectRatio) {
        jcrop.setAspectRatio(aspectRatio);
    }

    public void setCropVisible(boolean isVisible) {
        jcrop.setCropVisible(isVisible);
    }

    public void addSelectionListener(SelectionListener listener) {
        image.addSelectionListener(listener);
    }

    public void addReleaseListener(ReleaseListener listener) {
        image.addReleaseListener(listener);
    }

    public void setImageSource(Resource source) {
        image.setSource(source);
    }

    public void setBackgroundColor(String color) {
        jcrop.setBackgroundColor(color);
    }

    public void setBackgroundOpacity(double opacity) {
        jcrop.setBackgroundOpacity(opacity);
    }

    public void setMinHeight(int height) {
        jcrop.setMinHeight(height);
    }

    public void setMaxHeight(int height) {
        jcrop.setMaxHeight(height);
    }

    public void setMinWidth(int width) {
        jcrop.setMinWidth(width);
    }

    public void setMaxWidth(int width) {
        jcrop.setMaxWidth(width);
    }

    public void setRatio(double ratio) {
        jcrop.setAspectRatio(ratio);
    }

    @Override
    protected Component initContent() {
        return image;
    }

    @Override
    public Class<? extends SelectionArea> getType() {
        return SelectionArea.class;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jcrop.setEnabled(enabled);
    }

    public void animateSelection(SelectionArea selectionArea) {
        jcrop.animateTo(selectionArea);
    }
}
