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
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent.EventType;
import info.magnolia.ui.mediaeditor.field.MediaField;
import info.magnolia.ui.mediaeditor.field.image.ViewImageField;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.name.Named;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Simply displays an image with dialog actions to
 * submit or revert previous modifications. Also updates the undo/redo actions.
 */
public class ViewImageAction extends MediaEditorUIAction {

    private ViewImageField viewField = new ViewImageField();

    private ImageSizeLabel imageSizeLabel;

    private final SimpleTranslator i18n;

    public ViewImageAction(MediaEditorActionDefinition definition, MediaEditorView view, @Named(MediaEditorEventBus.NAME) EventBus eventBus,
            EditHistoryTrackingProperty dataSource, SimpleTranslator i18n) {
        super(definition, view, dataSource, eventBus);
        this.i18n = i18n;
        imageSizeLabel = new ImageSizeLabel(i18n);
        viewField.addImageResizeListener(imageSizeLabel);
    }

    @Override
    protected List<ActionContext> getActionContextList() {
        List<ActionContext> result = new ArrayList<ActionContext>();
        result.add(new ActionContext(new InternalMediaEditorActionDefinition("save", i18n.translate("ui-mediaeditor.internalAction.save.label"), false), new ActionListener() {
            @Override
            public void onActionFired(String actionName, Object... actionContextParams) {
                eventBus.fireEvent(new MediaEditorInternalEvent(EventType.SUBMIT));
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
    public void execute() throws ActionExecutionException {
        ActionbarView actionbar = view.getActionbar();
        actionbar.removeAction("undo");
        actionbar.removeAction("redo");

        String undoLabel = i18n.translate("ui-mediaeditor.action.undo.label")+" " + (dataSource.getLastDoneActionName() != null ? dataSource.getLastDoneActionName() : "");
        String redoLabel = i18n.translate("ui-mediaeditor.action.redo.label")+" " + (dataSource.getLastUnDoneActionName() != null ? dataSource.getLastUnDoneActionName() : "");

        ActionbarItem undo = new ActionbarItem("undo", undoLabel, "icon-undo", "track");
        ActionbarItem redo = new ActionbarItem("redo", redoLabel, "icon-redo", "track");

        actionbar.addAction(undo, "operations");
        actionbar.addAction(redo, "operations");

        actionbar.setActionEnabled("undo", dataSource.getLastDoneActionName() != null);
        actionbar.setActionEnabled("redo", dataSource.getLastUnDoneActionName() != null);
        view.getDialog().asVaadinComponent().removeStyleName("active-footer");
        super.execute();

    }

    @Override
    protected Component getStatusControls() {
        return imageSizeLabel;
    }

    @Override
    protected MediaField createMediaField() {
        return viewField;
    }

    /**
     * ImageSizeLabel.
     */
    public static class ImageSizeLabel extends Label implements ViewImageField.ImageSizeChangeListener {

        private final SimpleTranslator i18n;

        public ImageSizeLabel(SimpleTranslator i18n){
            this.i18n = i18n;
        }
        @Override
        public void onSizeChanged(ViewImageField.ImageResizeEvent e) {
            setValue(String.format(i18n.translate("ui-mediaeditor.view.actualSize.display"), e.getWidth(), e.getHeight()));
        }
    }
}
