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
package info.magnolia.ui.admincentral.mediaeditor.actionbar;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenterBase;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Actionbar presenter bound to the scope of media editor.
 */
public class MediaEditorActionbarPresenter extends ActionbarPresenterBase {
    
    @Inject
    public MediaEditorActionbarPresenter(@Named("mediaeditor") EventBus subAppEventBus) {
        super(subAppEventBus);
    }

    @Override
    public void onChangeFullScreen(boolean isFullScreen) {
        
    }
    
    @Override
    public void onActionbarItemClicked(String actionToken) {
        super.onActionbarItemClicked(actionToken);
    }
    
    public void fireAction(ActionDefinition actionDef) throws ActionExecutionException {
        final Action action = getActionFactory().createAction(actionDef);
        if (action == null) {
            throw new ActionExecutionException("Could not create action from actionDefinition. Action is null.");
        }
        action.execute();
    }

}
