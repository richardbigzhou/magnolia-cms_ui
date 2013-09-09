/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionView;
import info.magnolia.ui.dialog.actionpresenter.ActionParameterProvider;
import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenterImpl;
import info.magnolia.ui.dialog.actionpresenter.EditorActionExecutor;
import info.magnolia.ui.dialog.actionpresenter.definition.EditorActionPresenterDefinition;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;
import info.magnolia.ui.framework.action.MoveLocation;

import javax.inject.Inject;
import java.util.Set;

/**
 * Implementation of {@link MoveDialogActionPresenter}.
 */
public class MoveDialogActionPresenterImpl extends DialogActionPresenterImpl implements MoveDialogActionPresenter {

    @Inject
    public MoveDialogActionPresenterImpl(MoveDialogActionView view, ComponentProvider componentProvider, EditorActionExecutor actionExecutor) {
        super(view, componentProvider, actionExecutor);
    }

    @Override
    public DialogActionView start(Iterable<ActionDefinition> actions, EditorActionPresenterDefinition definition, ActionParameterProvider parameterProvider, UiContext uiContext) {
        return super.start(actions, definition, parameterProvider, uiContext);
    }

    @Override
    public void setPossibleMoveLocations(Set<MoveLocation> possibleMoveLocations) {
        for (MoveLocation location : MoveLocation.values()) {
            getView().setActionEnabled(location.name(), possibleMoveLocations.contains(location));
        }
    }

    @Override
    protected MoveDialogActionView getView() {
        return (MoveDialogActionView) super.getView();
    }
}
