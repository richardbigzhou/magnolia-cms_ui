/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.contentapp.autosuggest.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionActionCallback;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a dialog containing suggested sub-nodes and sub-properties to add to the selected selectedNode.
 */
public class OpenAddDefinitionDialogAction extends AbstractAction<OpenAddDefinitionDialogActionDefinition> {

    private static Logger log = LoggerFactory.getLogger(OpenAddDefinitionDialogAction.class);

    private OpenAddDefinitionDialogActionDefinition definition;

    private AppContext appContext;

    private JcrItemAdapter selectedItem;

    private AddDefinitionDialogPresenter addDefinitionDialogPresenter;

    private AutoSuggester autoSuggester;

    private OverlayCloser closeHandle;

    public OpenAddDefinitionDialogAction(
            OpenAddDefinitionDialogActionDefinition definition,
            AppContext appContext,
            JcrItemAdapter selectedItem,
            AddDefinitionDialogPresenter addDefinitionDialogPresenter) {

        super(definition);
        this.definition = definition;
        this.appContext = appContext;
        this.selectedItem = selectedItem;
        this.addDefinitionDialogPresenter = addDefinitionDialogPresenter;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (autoSuggester == null && definition != null) {
            try {
                Class<? extends AutoSuggester> autoSuggesterClass = definition.getAutoSuggesterClass();
                if (autoSuggesterClass != null) {
                    this.autoSuggester = autoSuggesterClass.newInstance();
                }
            } catch (InstantiationException ex) {
                log.warn("Could not instantiate AutoSuggester class " + definition.getAutoSuggesterClass() + ": " + ex);
            } catch (IllegalAccessException ex) {
                log.warn("Could not instantiate AutoSuggester class " + definition.getAutoSuggesterClass() + ": " + ex);
            }
        }

        if (autoSuggester != null && selectedItem != null) {
            DialogView addDefinitionDialog = addDefinitionDialogPresenter.start(selectedItem, autoSuggester, new AddDefinitionActionCallback() {
                @Override
                public void onAddDefinitionCancelled() {
                    closeHandle.close();
                }

                @Override
                public void onAddDefinitionPerformed() {
                    closeHandle.close();
                }
            });

            closeHandle = appContext.openOverlay(addDefinitionDialog, addDefinitionDialog.getModalityLevel());
            addDefinitionDialog.addDialogCloseHandler(new DialogCloseHandler() {
                @Override
                public void onDialogClose(DialogView dialogView) {
                    closeHandle.close();
                }
            });
        }
        else {
            log.warn("Could not create dialog because autoSuggester or selectedItem is null.");
        }
    }
}
