/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.ItemChosenListener;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

/**
 * Implementation of {@link ChooseDialogPresenterFactory}.
 */
@Singleton
public class WorkbenchChooseDialogPresenterFactory implements ChooseDialogPresenterFactory {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchChooseDialogPresenterFactory.class);

    private final AppContext appContext;
    private final ComponentProvider componentProvider;

    @Inject
    public WorkbenchChooseDialogPresenterFactory(final ComponentProvider componentProvider, AppContext appContext) {
        this.componentProvider = componentProvider;
        this.appContext = appContext;
    }

    @Override
    public WorkbenchChooseDialogPresenter createChooseDialogPresenter(String path, final ItemChosenListener listener, String selectedId) {

        SubAppDescriptor subAppContext = appContext.getDefaultSubAppDescriptor();
        if (!(subAppContext instanceof BrowserSubAppDescriptor)) {
            log.error("Cannot start workbench choose dialog since targeted app is not a content app");
            return null;
        }

        BrowserSubAppDescriptor subApp = (BrowserSubAppDescriptor) subAppContext;
        WorkbenchDefinition workbench = new Cloner().deepClone(subApp.getWorkbench());
        // mark definition as a dialog workbench so that workbench presenter can disable drag n drop
        ((ConfiguredWorkbenchDefinition) workbench).setDialogWorkbench(true);
        // Create the Choose Dialog Title
        String chooserLabel = appContext.getLabel() + " chooser";
        ((ConfiguredWorkbenchDefinition) workbench).setName(chooserLabel);
        ImageProviderDefinition imageProvider = new Cloner().deepClone(subApp.getImageProvider());

        final WorkbenchChooseDialogPresenter workbenchChooseDialogPresenter = componentProvider.newInstance(WorkbenchChooseDialogPresenter.class);
        workbenchChooseDialogPresenter.setWorkbenchDefinition(workbench);
        workbenchChooseDialogPresenter.setImageProviderDefinition(imageProvider);
        workbenchChooseDialogPresenter.setSelectedItemId(selectedId);
        workbenchChooseDialogPresenter.setImageProviderDefinition(imageProvider);
        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.COMMIT_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onItemChosen(workbenchChooseDialogPresenter.getValue());
            }
        });

        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onChooseCanceled();
            }
        });

        return workbenchChooseDialogPresenter;
    }
}
