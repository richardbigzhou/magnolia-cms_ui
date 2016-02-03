/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionAreaView;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenterImpl;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.workbench.tree.MoveLocation;

import java.util.Set;

import javax.inject.Inject;

/**
 * Implementation of {@link MoveDialogActionAreaPresenter}.
 */
public class MoveDialogActionAreaPresenterImpl extends EditorActionAreaPresenterImpl implements MoveDialogActionAreaPresenter {

    @Inject
    public MoveDialogActionAreaPresenterImpl(MoveDialogActionAreaView view, ComponentProvider componentProvider) {
        super(view, componentProvider);
    }

    @Override
    public EditorActionAreaView start(Iterable<ActionDefinition> actions, EditorActionAreaDefinition definition, ActionListener listener, UiContext uiContext) {
        return super.start(actions, definition, listener, uiContext);
    }

    @Override
    public void setPossibleMoveLocations(Set<MoveLocation> possibleMoveLocations) {
        for (MoveLocation location : MoveLocation.values()) {
            getView().setActionEnabled(location.name(), possibleMoveLocations.contains(location));
        }
    }

    @Override
    protected MoveDialogActionAreaView getView() {
        return (MoveDialogActionAreaView) super.getView();
    }
}
