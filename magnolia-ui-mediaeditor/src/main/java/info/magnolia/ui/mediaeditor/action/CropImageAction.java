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
package info.magnolia.ui.mediaeditor.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.mediaeditor.MediaEditorEventBus;
import info.magnolia.ui.mediaeditor.MediaEditorView;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.field.MediaField;
import info.magnolia.ui.mediaeditor.field.image.CropField;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.name.Named;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Installs UI components necessary for conducting the image crop operations.
 */
public class CropImageAction extends MediaEditorUIAction {

    private CropField cropField = new CropField();

    public CropImageAction(MediaEditorActionDefinition definition, MediaEditorView view, @Named(MediaEditorEventBus.NAME) EventBus eventBus, EditHistoryTrackingProperty dataSource) {
        super(definition, view, dataSource, eventBus);
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        view.getDialog().addStyleName("active-footer");
    }

    @Override
    protected List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<ActionContext>();
        result.add(new ActionContext("cancel", "Cancel", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                eventBus.fireEvent(new MediaEditorInternalEvent(MediaEditorInternalEvent.EventType.CANCEL_LAST));
            }
        }));
        result.add(new ActionContext("crop", "Crop Image", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                dataSource.startAction(getDefinition().getTrackingLabel());
                cropField.execute();
                eventBus.fireEvent(new MediaEditorInternalEvent(MediaEditorInternalEvent.EventType.APPLY));
            }
        }));
        return result;
    }

    @Override
    protected Component getStatusControls() {
        Label l = new Label();
        cropField.setStatusComponent(l);
        return l;
    }

    @Override
    protected MediaField createMediaField() {
        return cropField;
    }
}
