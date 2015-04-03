/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.mediaeditor.field.image;

import info.magnolia.ui.vaadin.editor.JCropField;
import info.magnolia.ui.vaadin.gwt.shared.jcrop.SelectionArea;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jhlabs.image.CropFilter;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

/**
 * Provides the functionality for image crop.
 */
public class CropField extends ImageMediaField {

    private final static int DEFAULT_CROP_OFFSET = 50;

    private final static int DEFAULT_CROP_DIMENSION = 200;

    private SelectionArea selectedArea = null;

    private JCropField jcropField = new JCropField();

    private BufferedImage image;
    
    public CropField() {
        jcropField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                selectedArea = (SelectionArea) event.getProperty().getValue();
            }
        });
    }

    @Override
    protected Component createImage() {
        return jcropField;
    }

    @Override
    public void refreshImageSource() {
        try {
            image = ImageIO.read(new ByteArrayInputStream(getValue()));
            jcropField.setTrueHeight(image.getHeight());
            jcropField.setTrueWidth(image.getWidth());
            jcropField.setImageSource(createResourceFromValue());
            jcropField.select(new SelectionArea(
                    DEFAULT_CROP_OFFSET,
                    DEFAULT_CROP_OFFSET,
                    DEFAULT_CROP_DIMENSION,
                    DEFAULT_CROP_DIMENSION));
        } catch (IOException e) {
            log.error("Error reading the image data: " + e.getMessage(), e);
        }
    }

    public BufferedImage cropImage(SelectionArea area) throws IOException {
        final CropFilter cropFilter = new CropFilter();
        cropFilter.setX(area.getLeft());
        cropFilter.setY(area.getTop());
        cropFilter.setWidth(area.getWidth());
        cropFilter.setHeight(area.getHeight());
        return cropFilter.filter(image, null);
    }

    public void setStatusComponent(Component c) {
        jcropField.setStatusComponent(c);
    }

    @Override
    protected BufferedImage executeImageModification() throws IOException {
        return  (selectedArea != null) ? cropImage(selectedArea) : null;
    }
}
