/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.mediaeditor.editmode.provider;

import info.magnolia.event.EventBus;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorInternalEvent.EventType;
import info.magnolia.ui.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.mediaeditor.editmode.field.image.ViewImageField;
import info.magnolia.ui.mediaeditor.editmode.field.image.ViewImageField.ImageResizeEvent;
import info.magnolia.ui.mediaeditor.editmode.field.image.ViewImageField.ImageSizeChangeListener;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Provides UI and necessary logic for simple image previewing operation.
 */
public class ViewImageProvider implements EditModeProvider {

    private ViewImageField viewField = new ViewImageField();
   
    private ImageSizeLabel imageSizeLabel = new ImageSizeLabel();
    
    private EventBus eventBus;
    
    @Inject
    public ViewImageProvider(@Named("mediaeditor") EventBus eventBus) {
        this.eventBus = eventBus;
        viewField.addImageResizeListener(imageSizeLabel);
    }
    
    @Override
    public MediaField getMediaField() {
        return viewField;
    }

    @Override
    public Component getStatusControls() {
        return imageSizeLabel;
    }

    @Override
    public List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<EditModeProvider.ActionContext>();
        result.add(new ActionContext("cancel", "Cancel", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.CANCEL_ALL));
            }
        }));
        
        result.add(new ActionContext("save", "Save", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.SUBMIT));
            }
        }));
        return result;
    }
   
    /**
     * ImageSizeLabel.
     */
    public static class ImageSizeLabel extends Label implements ImageSizeChangeListener {

        @Override
        public void onSizeChanged(ImageResizeEvent e) {
            setValue(String.format("Size: %d x %d px", e.getWidth(), e.getHeight()));
        }
    }
}
