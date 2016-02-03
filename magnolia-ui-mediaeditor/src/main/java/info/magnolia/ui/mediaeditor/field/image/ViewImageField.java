/**
 * This file Copyright (c) 2013-2016 Magnolia International
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


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.util.ReflectTools;

/**
 * Provides the functionality for simple image view.
 */
public class ViewImageField extends ImageMediaField {
    
    private Image image = new Image();
    
    private BufferedImage bufferedImage;
    
    @Override
    public void refreshImageSource() {
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(getValue()));
            fireEvent(new ImageResizeEvent(this, bufferedImage.getWidth(), bufferedImage.getHeight()));
            image.setSource(createResourceFromValue());
        } catch (IOException e) {
            log.error("Error reading the image data: " + e.getMessage(), e);
        }
    }

    public void addImageResizeListener(ImageSizeChangeListener listener) {
        addListener(ImageSizeChangeListener.EVENT_ID, ImageResizeEvent.class, listener, ImageSizeChangeListener.EVENT_METHOD);
    }
    
    public void removeImageResizeListener(ImageSizeChangeListener listener) {
        removeListener(ImageSizeChangeListener.EVENT_ID, ImageResizeEvent.class, listener);
    }
    
    @Override
    protected Component createImage() {
        return image;
    }
    
    @Override
    public void attach() {
        super.attach();
    }
    
    /**
     * ImageResizeEvent.
     */
    public static class ImageResizeEvent extends Component.Event {
        
        private int width;
        
        private int height;
        
        public ImageResizeEvent(Component source, int newWidth, int newHeight) {
            super(source);
            this.width = newWidth;
            this.height = newHeight;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
    
   
    /**
     * ImageSizeChangeListener.
     */
    public interface ImageSizeChangeListener {
        public static String EVENT_ID = "image_resize";
        public static Method EVENT_METHOD = 
                ReflectTools.findMethod(ImageSizeChangeListener.class, "onSizeChanged", ImageResizeEvent.class);
        void onSizeChanged(ImageResizeEvent e);
    }
}
