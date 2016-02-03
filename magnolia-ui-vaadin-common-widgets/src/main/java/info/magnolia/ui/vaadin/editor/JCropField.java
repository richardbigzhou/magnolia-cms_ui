/**
 * This file Copyright (c) 2010-2016 Magnolia International
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


import info.magnolia.ui.vaadin.editor.CroppableImage.JCropReleaseEvent;
import info.magnolia.ui.vaadin.editor.CroppableImage.JCropSelectionEvent;
import info.magnolia.ui.vaadin.editor.CroppableImage.ReleaseListener;
import info.magnolia.ui.vaadin.editor.CroppableImage.SelectionListener;
import info.magnolia.ui.vaadin.gwt.shared.jcrop.SelectionArea;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * Field that wraps A {@link CroppableImage}, manages the {@link SelectionArea} as a value.
 */
public class JCropField extends CustomField<SelectionArea> {

    private final JCrop jcrop;

    private CroppableImage image;

    public JCropField() {
        super();
        this.jcrop = getContent().getJcrop();
        setWidth("");
        setCropVisible(true);
        setEnabled(true);
        image.addSelectionListener(new SelectionListener() {
            @Override
            public void onSelected(JCropSelectionEvent e) {
                setValue(e.getArea());
            }
        });
        image.addReleaseListener(new ReleaseListener() {
            @Override
            public void onRelease(JCropReleaseEvent e) {
                setValue(null);
            }
        });
    }

    @Override
    protected CroppableImage getContent() {
        return (CroppableImage)super.getContent();
    }

    public void setStatusComponent(Component c) {
        jcrop.setSelectionStatusComponent(c);
    }

    public Resource getImageSource() {
        return image.getSource();
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
        jcrop.invalidate();
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

    public void select(SelectionArea area) {
        jcrop.select(area);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);
        getContent().setWidth(width, unit);
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);
        getContent().setHeight(height, unit);
    }

    @Override
    public void setValue(SelectionArea newFieldValue) throws ReadOnlyException,
            ConversionException {
        if ((newFieldValue == null && getValue() != null) || (newFieldValue != null && !newFieldValue.equals(getValue()))) {
            super.setValue(newFieldValue);
        }
    }

    @Override
    protected Component initContent() {
        if (image == null) {
            image = new CroppableImage();
        }
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

    public void setTrueHeight(int height) {
        jcrop.setTrueHeight(height);
    }

    public void setTrueWidth(int width) {
        jcrop.setTrueWidth(width);
    }
}
