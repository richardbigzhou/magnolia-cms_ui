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
package info.magnolia.ui.contentapp.movedialog.action;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.contentapp.movedialog.MoveDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.tree.MoveLocation;

import java.util.Arrays;
import java.util.List;

import com.vaadin.data.Item;

/**
 * Opens a move dialog for a collections of node adapters that need to be transferred.
 */
public class OpenMoveDialogAction extends AbstractAction<OpenMoveDialogActionDefinition> {

    private AppContext appContext;

    private ComponentProvider componentProvider;

    private JcrNodeAdapter sourceNodeAdapter;

    private List<Item> sourceNodeAdapters;

    private MoveDialogPresenter moveDialogPresenter;

    private OverlayCloser closeHandle;

    public OpenMoveDialogAction(
            OpenMoveDialogActionDefinition definition,
            AppContext appContext,
            ComponentProvider componentProvider,
            Item sourceNodeAdapter,
            MoveDialogPresenter moveDialogPresenter) {
        this(definition, appContext, componentProvider, moveDialogPresenter, Arrays.asList(sourceNodeAdapter));
    }

    public OpenMoveDialogAction(
            OpenMoveDialogActionDefinition definition,
            AppContext appContext,
            ComponentProvider componentProvider,
            MoveDialogPresenter moveDialogPresenter,
            List<Item> sourceNodeAdapters) {
        super(definition);
        this.appContext = appContext;
        this.componentProvider = componentProvider;
        this.sourceNodeAdapters = sourceNodeAdapters;
        this.moveDialogPresenter = moveDialogPresenter;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (appContext.getActiveSubAppContext().getSubAppDescriptor() instanceof BrowserSubAppDescriptor) {
            BrowserSubAppDescriptor descriptor = (BrowserSubAppDescriptor) appContext.getActiveSubAppContext().getSubAppDescriptor();
            DialogView moveDialog = moveDialogPresenter.start(descriptor, sourceNodeAdapters, new MoveActionCallback() {
                @Override
                public void onMoveCancelled() {
                    closeHandle.close();
                }

                @Override
                public void onMovePerformed(Item newHost, MoveLocation moveLocation) {
                    closeHandle.close();
                }
            });

            this.closeHandle = appContext.openOverlay(moveDialog, moveDialog.getModalityLevel());
            moveDialog.addDialogCloseHandler(new DialogCloseHandler() {
                @Override
                public void onDialogClose(DialogView dialogView) {
                    closeHandle.close();
                }
            });
        }
    }
}
