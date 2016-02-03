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
package info.magnolia.ui.mediaeditor.action;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.mediaeditor.MediaEditorEventBus;
import info.magnolia.ui.mediaeditor.MediaEditorView;
import info.magnolia.ui.mediaeditor.action.feature.Scalable;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent.EventType;
import info.magnolia.ui.mediaeditor.field.MediaField;
import info.magnolia.ui.mediaeditor.field.image.CropField;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.name.Named;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Installs UI components necessary for conducting the image crop operations.
 */
public class CropImageAction extends MediaEditorUIAction {

    private CropField cropField = new CropField();

    private final SimpleTranslator i18n;

    public CropImageAction(MediaEditorActionDefinition definition, MediaEditorView view, @Named(MediaEditorEventBus.NAME) EventBus eventBus, EditHistoryTrackingProperty dataSource, SimpleTranslator i18n) {
        super(definition, view, dataSource, eventBus);

        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        view.getDialog().asVaadinComponent().addStyleName("active-footer");
        if (view.getDialog().getContentView().asVaadinComponent() instanceof Scalable) {
            Scalable scalable = (Scalable) view.getDialog().getContentView().asVaadinComponent();
            scalable.scaleToFit();
        }
    }

    @Override
    protected List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<ActionContext>();
        result.add(new ActionContext(new InternalMediaEditorActionDefinition("crop", i18n.translate("ui-mediaeditor.action.crop.label"), true), new ActionListener() {
            @Override
            public void onActionFired(String actionName, Object... actionContextParams) {
                dataSource.startAction(StringUtils.lowerCase(getDefinition().getLabel()));
                cropField.execute();
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.APPLY));
            }
        }));

        result.add(new ActionContext(new InternalMediaEditorActionDefinition("cancel", i18n.translate("ui-mediaeditor.internalAction.cancel.label"), true), new ActionListener() {
            @Override
            public void onActionFired(String actionName, Object... actionContextParams) {
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.CANCEL_ALL));
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
