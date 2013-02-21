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
package info.magnolia.ui.admincentral.mediaeditor;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.mediaeditor.action.EditModeActionDefinition;
import info.magnolia.ui.admincentral.mediaeditor.editmode.factory.EditModeBuilderFactory;
import info.magnolia.ui.admincentral.mediaeditor.editmode.presenter.EditorPresenter;
import info.magnolia.ui.model.action.ActionDefinition;

import java.io.ByteArrayInputStream;

import org.apache.commons.lang.ArrayUtils;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * MediaEditorPresenterImpl.
 */
public class MediaEditorPresenterImpl extends CustomField<Byte[]> implements MediaEditorPresenter {

    /**
     * ActionbarEventHandler.
     */
    private final class ActionbarEventHandler implements ActionbarItemClickedEvent.Handler {
        @Override
        public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
            final ActionDefinition actionDefinition = event.getActionDefinition();
            dispatchActionbarEvent(actionDefinition);
        }
    }

    private MediaEditorView view;

    private EditorPresenter editorPresenter;

    private EditModeBuilderFactory editModeBuilderFactory;

    public MediaEditorPresenterImpl(EventBus eventBus, MediaEditorView view, EditorPresenter editorPresenter,
            EditModeBuilderFactory modeBuilderFactory) {
        eventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarEventHandler());
        this.view = view;
        this.editModeBuilderFactory = modeBuilderFactory;
        this.editorPresenter = editorPresenter;
    }

    protected void dispatchActionbarEvent(ActionDefinition actionDefinition) {
        if (actionDefinition instanceof EditModeActionDefinition) {
            editModeBuilderFactory.getBuilder((EditModeActionDefinition) actionDefinition);
        }
    }

    @Override
    public Class<? extends Byte[]> getType() {
        return Byte[].class;
    }

    @Override
    public void setValue(Byte[] value) throws ReadOnlyException, ConversionException {
        super.setValue(value);
        editorPresenter.setInputStream(new ByteArrayInputStream(ArrayUtils.toPrimitive(value)));
    }

    @Override
    protected Component initContent() {
        return view.asVaadinComponent();
    }

}
