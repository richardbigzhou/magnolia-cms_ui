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
import info.magnolia.ui.mediaeditor.editmode.field.image.CropField;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.name.Named;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Provides UI and necessary logic for crop operation.
 */
public class CropImageProvider implements EditModeProvider {

    private CropField cropField = new CropField();

    private EventBus eventBus;
    
    @Inject
    public CropImageProvider(@Named("mediaeditor") EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @Override
    public MediaField getMediaField() {
        return cropField;
    }

    @Override
    public Component getStatusControls() {
        Label l = new Label();
        cropField.setStatusComponent(l);
        return l;
    }

    @Override
    public List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<ActionContext>();
        
        result.add(new ActionContext("cancel", "Cancel", new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                cropField.revertChanges();
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.CANCEL_LAST));
            }
        }));
        
        result.add(new ActionContext("crop", "Crop", new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                cropField.execute();
                cropField.applyChanges();
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.APPLY));
            }
        }));
        return result;
    }
}
