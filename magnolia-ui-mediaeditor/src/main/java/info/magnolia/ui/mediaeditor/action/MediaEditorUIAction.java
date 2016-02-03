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
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.mediaeditor.MediaEditorEventBus;
import info.magnolia.ui.mediaeditor.MediaEditorView;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.field.MediaField;
import info.magnolia.ui.mediaeditor.provider.MediaEditorActionDefinition;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.vaadin.ui.Component;

/**
 * Updates media editor UI in order to perform certain modification
 * on the media data.
 */
public abstract class MediaEditorUIAction extends MediaEditorAction {

    private Logger log = Logger.getLogger(getClass());

    protected MediaEditorView view;

    public MediaEditorUIAction(MediaEditorActionDefinition definition, MediaEditorView view, EditHistoryTrackingProperty dataSource, @Named(MediaEditorEventBus.NAME) EventBus eventBus) {
        super(definition, dataSource, eventBus);
        this.view = view;
    }

    @Override
    public void execute() throws ActionExecutionException {
        MediaField newMediaField = createMediaField();
        if (newMediaField != null) {
            view.clearActions();
            view.setMediaContent(newMediaField);
            view.setToolbar(getStatusControls());

            List<ActionContext> actionContexts = getActionContextList();
            for (ActionContext ctx : actionContexts) {
                view.getDialog().addAction(ctx.getActionId(), ctx.getLabel(), ctx.getListener());
            }

            if (!actionContexts.isEmpty()) {
                view.getDialog().setDefaultAction(actionContexts.get(actionContexts.size() - 1).getActionId());
            }
            newMediaField.setPropertyDataSource(dataSource);
        } else {
            log.warn("Provider did not provide any content UI ");
        }

    }

    protected abstract List<ActionContext> getActionContextList();

    protected abstract Component getStatusControls();

    protected abstract MediaField createMediaField();


}
