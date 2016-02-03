/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.dialog;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

import net.sf.cglib.proxy.Enhancer;

/**
 * Base implementation of {@link DialogPresenter}.
 */
public class BaseDialogPresenter implements DialogPresenter, ActionListener {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DialogView view;

    protected ComponentProvider componentProvider;

    private ActionExecutor executor;

    private EditorActionAreaPresenter editorActionAreaPresenter;

    private final I18nizer i18nizer;

    private final SimpleTranslator i18n;

    private UiContext uiContext;

    private DialogDefinition definition;

    private boolean isExecutingAction;

    @Inject
    public BaseDialogPresenter(ComponentProvider componentProvider, ActionExecutor executor, DialogView view, I18nizer i18nizer, SimpleTranslator i18n) {
        this.componentProvider = componentProvider;
        this.executor = executor;
        this.view = view;
        this.i18nizer = i18nizer;
        this.i18n = i18n;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public ActionAreaPresenter getActionArea() {
        return editorActionAreaPresenter;
    }

    @Override
    public void closeDialog() {
        view.close();
    }

    @Override
    public void addShortcut(final String actionName, final int keyCode, final int... modifiers) {

        view.addShortcut(new ShortcutListener(actionName, keyCode, modifiers) {
            @Override
            public void handleAction(Object sender, Object target) {
                executeAction(actionName, new Object[0]);
            }
        });
    }

    @Override
    public DialogView start(DialogDefinition dialogDefinition, UiContext uiContext) {
        // Additional close handler to check for cases when the dialog isn't closed from an action (shortcut, dialog close icon), and process callback accordingly
        getView().addDialogCloseHandler(new DialogCloseHandler() {

            @Override
            public void onDialogClose(DialogView dialogView) {
                if (!isExecutingAction) {
                    onCancel();
                }
            }
        });

        this.uiContext = uiContext;
        // ChooseDialogDefinition is already enhanced as it is obtained via ContentAppDescriptor.getChooseDialog() at ContentApp.openChooseDialog(..)
        if (Enhancer.isEnhanced(dialogDefinition.getClass())) {
            this.definition = dialogDefinition;
        } else {
            this.definition = i18nizer.decorate(dialogDefinition);
        }

        this.editorActionAreaPresenter = componentProvider.newInstance(definition.getActionArea().getPresenterClass());
        EditorActionAreaView editorActionAreaView = editorActionAreaPresenter.start(filterActions(), definition.getActionArea(), this, uiContext);

        // Set modifier key based on OS.
        int osSpecificModifierKey;
        UI ui = UI.getCurrent();
        if (ui != null) {
            WebBrowser browser = ui.getPage().getWebBrowser();
            if (browser.isWindows()) {
                osSpecificModifierKey = ModifierKey.CTRL;
            } else {
                // osx and linux
                osSpecificModifierKey = ModifierKey.META;
            }

            if (definition.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME)) {
                addShortcut(BaseDialog.COMMIT_ACTION_NAME, KeyCode.S, osSpecificModifierKey);
            }
            if (definition.getActions().containsKey(BaseDialog.CANCEL_ACTION_NAME)) {
                addShortcut(BaseDialog.CANCEL_ACTION_NAME, KeyCode.W, osSpecificModifierKey);
            }
            if (definition.getModalityLevel() == ModalityLevel.LIGHT) {
                view.addShortcut(new CloseDialogShortcutListener(KeyCode.ESCAPE));
            }

        } else {
            log.warn("The current Vaadin UI was null when starting {}, as a result dialog keyboard shortcuts will not work.", this);
        }

        view.addShortcut(new CloseDialogAfterConfirmationShortcutListener(KeyCode.ESCAPE));
        view.addShortcut(new CommitDialogShortcutListener(KeyCode.ENTER));

        this.view.setActionAreaView(editorActionAreaView);
        this.view.setModalityLevel(definition.getModalityLevel());
        if (definition.isWide()){
            this.view.setWide(true);
        }
        return this.view;
    }

    protected Iterable<ActionDefinition> filterActions() {
        return getDefinition().getActions().values();
    }

    protected Object[] getActionParameters(String actionName) {
        return new Object[] { this };
    }

    @Override
    public void onActionFired(String actionName, Object... actionContextParams) {
        executeAction(actionName, actionContextParams);
    }

    protected void executeAction(String actionName, Object[] actionContextParams) {
        isExecutingAction = true;
        Object[] providedParameters = getActionParameters(actionName);
        Object[] combinedParameters = new Object[providedParameters.length + actionContextParams.length];
        System.arraycopy(providedParameters, 0, combinedParameters, 0, providedParameters.length);
        System.arraycopy(actionContextParams, 0, combinedParameters, providedParameters.length, actionContextParams.length);
        try {
            executor.execute(actionName, combinedParameters);
        } catch (ActionExecutionException e) {
            String exceptionStatement = i18n.translate("ui-dialog.actionexecutionerror.basemessage");
            Message error = new Message(MessageType.ERROR, exceptionStatement, e.getMessage());
            log.error(exceptionStatement, e);
            if (uiContext instanceof AppContext) {
                ((AppContext) uiContext).sendLocalMessage(error);
            } else if (uiContext instanceof SubAppContext) {
                ((SubAppContext) uiContext).getAppContext().sendLocalMessage(error);
            }
        } finally {
            isExecutingAction = false;
        }
    }

    protected DialogDefinition getDefinition() {
        return definition;
    }

    protected ActionExecutor getExecutor() {
        return executor;
    }

    protected I18nizer getI18nizer() {
        return i18nizer;
    }

    /**
     * A shortcut listener used to close the dialog.
     */
    protected class CloseDialogShortcutListener extends ShortcutListener {

        public CloseDialogShortcutListener(int keyCode, int... modifierKey) {
            super("", keyCode, modifierKey);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            closeDialog();
        }
    }

    /**
     * A shortcut listener which opens a confirmation to confirm closing the dialog.
     */
    protected class CloseDialogAfterConfirmationShortcutListener extends ShortcutListener {

        public CloseDialogAfterConfirmationShortcutListener(int keyCode, int... modifierKey) {
            super("", keyCode, modifierKey);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            uiContext.openConfirmation(
                    MessageStyleTypeEnum.WARNING, i18n.translate("ui-dialog.closeConfirmation.title"), i18n.translate("ui-dialog.closeConfirmation.body"), i18n.translate("ui-dialog.closeConfirmation.confirmButton"), i18n.translate("ui-dialog.cancelButton"), false,
                    new ConfirmationCallback() {
                        @Override
                        public void onSuccess() {
                            closeDialog();
                        }

                        @Override
                        public void onCancel() {
                            if (getView() instanceof Panel) {
                                ((Panel) getView()).focus();
                            }
                        }
                    });
        }
    }

    /**
     * A shortcut listener used to commit the dialog.
     */
    protected class CommitDialogShortcutListener extends ShortcutListener {

        public CommitDialogShortcutListener(int keyCode, int... modifierKey) {
            super("", keyCode, modifierKey);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            // textareas are excluded on the client-side, see 'EnterFriendlyShortcutActionHandler', used in PanelConnector
            executeAction(BaseDialog.COMMIT_ACTION_NAME, new Object[0]);
        }
    }

    protected void onCancel() {
    }
}
