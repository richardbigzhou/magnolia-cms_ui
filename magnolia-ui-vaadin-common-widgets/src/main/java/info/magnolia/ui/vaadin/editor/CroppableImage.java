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

import info.magnolia.ui.vaadin.gwt.shared.jcrop.SelectionArea;

import java.lang.reflect.Method;
import java.util.EventObject;

import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.util.ReflectTools;

/**
 * Image extended with {@link JCrop}.
 */
public final class CroppableImage extends Image implements JCropHandler {
    
    private final JCrop jcrop;
    
    public CroppableImage() {
        this.jcrop = new JCrop(this);
        addExtension(jcrop);
    }
    
    public JCrop getJcrop() {
        return jcrop;
    }

    @Override
    public void fireEvent(EventObject event) {
        super.fireEvent(event);
    }
    
    @Override
    public void addSelectionListener(SelectionListener listener) {
        addListener(SelectionListener.EVENT_ID, JCropSelectionEvent.class, listener, SelectionListener.EVENT_METHOD);
    }
    
    @Override
    public void removeSelectionListener(SelectionListener listener) {
        removeListener(SelectionListener.EVENT_ID, JCropSelectionEvent.class, listener);
    }
    
    @Override
    public void addReleaseListener(ReleaseListener listener) {
        addListener(ReleaseListener.EVENT_ID, JCropReleaseEvent.class, listener, ReleaseListener.EVENT_METHOD);
    }
    
    @Override
    public void removeReleaseListener(ReleaseListener listener) {
        removeListener(ReleaseListener.EVENT_ID, JCropReleaseEvent.class, listener);
    }
    
    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);
        jcrop.invalidate();
    }
    
    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);
        jcrop.invalidate();
    }
    
    @Override
    public void addStyleName(String style) {
        super.addStyleName(style);
        jcrop.invalidate();
    }
    
    @Override
    public void removeStyleName(String style) {
        super.removeStyleName(style);
        jcrop.invalidate();
    }
    
    /**
     * JCropEvent.
     */
    public static class JCropEvent extends Component.Event {
        
        private final SelectionArea area;
        
        public JCropEvent(Component source, SelectionArea area) {
            super(source);
            this.area = area;
        }
        
        public SelectionArea getArea() {
            return area;
        }
        
    }
    
    /**
     * JCropSelectionEvent.
     */
    public static class JCropSelectionEvent extends JCropEvent {
        
        public JCropSelectionEvent(Component source, SelectionArea area) {
            super(source, area);
        }
    }

    /**
     * JCropReleaseEvent.
     */
    public static class JCropReleaseEvent extends Component.Event {
        public JCropReleaseEvent(Component source) {
            super(source);
        }
    }

    /**
     * SelectionListener.
     */
    public interface SelectionListener {
        public static String EVENT_ID = "jcrop_sl";
        public static Method EVENT_METHOD = 
                ReflectTools.findMethod(SelectionListener.class, "onSelected", JCropSelectionEvent.class);
        void onSelected(JCropSelectionEvent e);
    }
    
    /**
     * ReleaseListener.
     */
    public interface ReleaseListener {
        public static String EVENT_ID = "jcrop_rl";
        public static Method EVENT_METHOD = 
                ReflectTools.findMethod(ReleaseListener.class, "onRelease", JCropReleaseEvent.class);
        void onRelease(JCropReleaseEvent e);
    }

    @Override
    public void handleSelection(SelectionArea area) {
        
    }

    @Override
    public void handleRelease() {
        
    }
}
