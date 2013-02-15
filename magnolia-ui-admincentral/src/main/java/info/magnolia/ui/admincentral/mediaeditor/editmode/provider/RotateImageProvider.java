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

import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorEvent;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorEvent.EventType;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.image.RotationField;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * RotationProvider.
 */
public class RotateImageProvider implements EditModeProvider {
    
    private RotationField rotationField = new RotationField();
    
    private TextField angleField = new TextField();
    
    private NativeButton rotateButton = new NativeButton("rotate");
    
    private EventBus eventBus;
    
    @Inject
    public RotateImageProvider(@Named("mediaeditor") EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @Override
    public MediaField getMediaField() {
        return rotationField;
    }

    @Override
    public Component getStatusControls() {
        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth("200px");
        
        NativeButton rotateRight = new NativeButton("<-", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                rotationField.setAngle(-90);
                rotationField.execute();
            }
        });
        
        
        NativeButton rotateLeft = new NativeButton("->", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                rotationField.setAngle(90);
                rotationField.execute();
            }
        });
        
        
        angleField.setWidth("40px");
        angleField.setConverter(new StringToDoubleConverter());
        rotateButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                rotationField.setAngle(Double.valueOf(angleField.getValue()));
                rotationField.execute();
            }
        });
        
        toolbar.setSpacing(true);
        toolbar.addComponent(rotateRight);
        toolbar.addComponent(rotateLeft);
        toolbar.addComponent(angleField);
        toolbar.addComponent(rotateButton);
        for (Iterator<Component> it = toolbar.iterator();it.hasNext();) {
            Component c = it.next();
            toolbar.setExpandRatio(c, 1f);
            toolbar.setComponentAlignment(c, Alignment.MIDDLE_RIGHT);
        }
        toolbar.setComponentAlignment(angleField, Alignment.MIDDLE_RIGHT);
        toolbar.setExpandRatio(angleField, 2f);
        
        return toolbar;
    }

    @Override
    public List<ActionContext> getActionContextList() {
        List<ActionContext> actions = new ArrayList<EditModeProvider.ActionContext>();
        
        actions.add(new ActionContext("cancel", "Cancel", new EditorLikeActionListener() {
            
            @Override
            public void onActionExecuted(String actionName) {
                rotationField.revertChanges();
                eventBus.fireEvent(new MediaEditorEvent(EventType.CANCEL_LAST));
            }
        }));

        actions.add(new ActionContext("rotate", "Apply", new EditorLikeActionListener() {
            
            @Override
            public void onActionExecuted(String actionName) {
                rotationField.applyChanges();
                eventBus.fireEvent(new MediaEditorEvent(EventType.APPLY));
            }
        }));
        return actions;
    }
}
