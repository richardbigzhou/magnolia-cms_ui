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
package info.magnolia.ui.contentapp.autosuggest;

import info.magnolia.event.EventBus;
import info.magnolia.event.ResettableEventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.contentapp.autosuggest.action.AddDefinitionAction;
import info.magnolia.ui.contentapp.autosuggest.action.AddDefinitionCancelledAction;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.framework.overlay.ViewAdapter;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Inject;

import com.vaadin.ui.Button;

/**
 * Implementation of {@link AddDefinitionDialogPresenter}.
 */
public class AddDefinitionDialogPresenterImpl extends BaseDialogPresenter implements AddDefinitionDialogPresenter {

    private static final String ADD_BUTTON_NAME = "addDefinitionDialogAddButton";

    private DialogView dialogView;

    private EventBus eventBus = new ResettableEventBus(new SimpleEventBus());

    private AppContext appContext;

    private JcrItemAdapter selectedItem;

    private AddDefinitionActionCallback callback;

    private AutoSuggester autoSuggester;

    private I18nizer i18nizer;

    private ContentConnector contentConnector;

    private AddDefinitionDialogComponent addDefinitionDialogComponent;

    @Inject
    public AddDefinitionDialogPresenterImpl(ComponentProvider componentProvider, DialogView dialogView, DialogActionExecutor executor, AppContext appContext, I18nizer i18nizer, SimpleTranslator simpleTranslator, ContentConnector contentConnector) {
        super(componentProvider, executor, dialogView, i18nizer, simpleTranslator);
        this.dialogView = dialogView;
        this.appContext = appContext;
        this.i18nizer = i18nizer;
        this.contentConnector = contentConnector;
    }

    @Override
    public Object[] getActionParameters(String actionName) {
        return new Object[] { autoSuggester, selectedItem, addDefinitionDialogComponent.getSelectedNames(), callback };
    }

    @Override
    public DialogView start(JcrItemAdapter selectedItem, AutoSuggester autoSuggester, AddDefinitionActionCallback callback) {

        this.selectedItem = selectedItem;
        this.autoSuggester = autoSuggester;
        this.callback = callback;

        addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        ViewAdapter viewAdapter = new ViewAdapter(addDefinitionDialogComponent);
        viewAdapter.asVaadinComponent().addStyleName("choose-dialog");
        dialogView.setContent(viewAdapter);

        DialogDefinition dialogDefinition = prepareDialogDefinition();
        getExecutor().setDialogDefinition(dialogDefinition);
        dialogView.setCaption(dialogDefinition.getLabel());
        dialogView.addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                ((ResettableEventBus) eventBus).reset();
            }
        });

        super.start(dialogDefinition, appContext);
        Button addButton = (Button) getView().getActionAreaView().getViewForAction(ADD_BUTTON_NAME).asVaadinComponent();
        addButton.addStyleName("commit");
        addButton.focus();
        getView().setClosable(true);

        return dialogView;
    }

    private DialogDefinition prepareDialogDefinition() {
        ConfiguredDialogDefinition dialogDefinition = i18nizer.decorate(new ConfiguredDialogDefinition());
        dialogDefinition.setId("ui-contentapp:code:AddDefinitionDialogPresenterImpl.addDefinitionDialog");

        ConfiguredActionDefinition addButtonDefinition = i18nizer.decorate(new ConfiguredActionDefinition());
        addButtonDefinition.setName(ADD_BUTTON_NAME);
        addButtonDefinition.setImplementationClass(AddDefinitionAction.class);
        dialogDefinition.addAction(addButtonDefinition);

        ConfiguredActionDefinition cancelButtonDefinition = i18nizer.decorate(new ConfiguredActionDefinition());
        cancelButtonDefinition.setName("cancel");
        cancelButtonDefinition.setImplementationClass(AddDefinitionCancelledAction.class);
        dialogDefinition.addAction(cancelButtonDefinition);

        dialogDefinition.setModalityLevel(ModalityLevel.LIGHT);
        return dialogDefinition;
    }

    @Override
    protected void executeAction(String actionName, Object[] actionContextParams) {
        if (actionName != null) {
            if (BaseDialog.COMMIT_ACTION_NAME.equals(actionName)) {
                actionName = ADD_BUTTON_NAME;
            }

            super.executeAction(actionName, actionContextParams);
        }
    }

    @Override
    protected DialogActionExecutor getExecutor() {
        return (DialogActionExecutor) super.getExecutor();
    }

}
