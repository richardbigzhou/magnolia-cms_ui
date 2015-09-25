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

import info.magnolia.ui.mediaeditor.action.feature.Scalable;
import info.magnolia.ui.mediaeditor.field.MediaField;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

/**
 * Base class for image-editing fields.
 */
public abstract class ImageMediaField extends CustomField<byte[]> implements MediaField, Scalable {

    protected final static String DEFAULT_FORMAT = "jpg";

    protected final static String TEMP_FILE_NAME_BASE = "image_media_editor";

    protected Logger log = Logger.getLogger(getClass());

    private Component fieldComponent;

    @Override
    protected Component initContent() {
        addStyleName("image-media-field");
        setSizeFull();
        VerticalLayout mediaContentWrapper = new VerticalLayout();
        mediaContentWrapper.addStyleName("media-wrapper");
        mediaContentWrapper.setSizeFull();
        fieldComponent = createImage();
        mediaContentWrapper.addComponent(fieldComponent);
        mediaContentWrapper.setComponentAlignment(fieldComponent, Alignment.MIDDLE_CENTER);
        return mediaContentWrapper;
    }

    protected abstract Component createImage();

    @Override
    protected void setInternalValue(byte[] newValue) {
        super.setInternalValue(newValue);
        if (newValue != null) {
            refreshImageSource();
        }
    }

    @Override
    public void scaleToActualSize() {
        doScaleToActual(this);
    }

    private void doScaleToActual(HasComponents hc) {
        Iterator<Component> it = hc.iterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof Image) {
                c.removeStyleName("scale-to-fit");
            } else if (c instanceof HasComponents) {
                doScaleToActual((HasComponents)c);
            }
        }
    }

    @Override
    public void scaleToFit() {
        doScaleToFit(this);
    }

    private void doScaleToFit(HasComponents hc) {
        Iterator<Component> it = hc.iterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof Image) {
                c.addStyleName("scale-to-fit");
            } else if (c instanceof HasComponents) {
                doScaleToFit((HasComponents)c);
            }
        }
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    @Override
    public void applyChanges() {
    }

    @Override
    public void revertChanges() {
    }

    public void execute() {
        InputStream stream = null;
        try {
            BufferedImage result = executeImageModification();
            stream = createStreamSource(result, DEFAULT_FORMAT);
            if (result != null) {
                setValue(IOUtils.toByteArray(stream));
                refreshImageSource();
            }
        } catch (IOException e) {
            log.error("Error occurred while converting operation result into stream: " + e.getMessage(), e);  //TODO-TRANSLATE-EXCEPTION
            revertChanges();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    protected BufferedImage executeImageModification() throws IOException {
        return null;
    }

    public abstract void refreshImageSource();

    protected String generateTempFileName() {
        return TEMP_FILE_NAME_BASE + System.currentTimeMillis();
    }

    protected StreamResource createResourceFromValue() {
        return new StreamResource(new StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(getValue());
            }
        }, generateTempFileName()) {{
                setMIMEType("image/" + DEFAULT_FORMAT);
        }};
    }

    protected InputStream createStreamSource(final BufferedImage img, final String formatName) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            if (img == null) {
                return null;
            }
            os = new ByteArrayOutputStream();
            ImageIO.write(img, formatName, os);
            return new ByteArrayInputStream(os.toByteArray());
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}
