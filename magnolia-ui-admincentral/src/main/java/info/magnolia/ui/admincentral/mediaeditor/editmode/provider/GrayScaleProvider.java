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
package info.magnolia.ui.admincentral.mediaeditor.editmode.provider;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorEvent;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorEvent.EventType;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.image.GrayScaleField;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.vaadin.ui.Component;

/**
 * Provides UI and necessary logic for image gray-scaling operation.
 */
public class GrayScaleProvider implements EditModeProvider {

    private EventBus eventBus;
    
    private GrayScaleField  field =  new GrayScaleField();
    
    public GrayScaleProvider(@Named("mediaeditor") EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @Override
    public MediaField getMediaField() {
        return field;
    }

    @Override
    public Component getStatusControls() {
        return null;
    }

    @Override
    public List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<EditModeProvider.ActionContext>();
        result.add(new ActionContext("cancel", "Cancel", new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                field.revertChanges();
                eventBus.fireEvent(new MediaEditorEvent(EventType.CANCEL_LAST));
            }
        }));
        
        result.add(new ActionContext("save", "Apply", new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                field.applyChanges();
                eventBus.fireEvent(new MediaEditorEvent(EventType.APPLY));
            }
        }));
        return result;
    }

}
