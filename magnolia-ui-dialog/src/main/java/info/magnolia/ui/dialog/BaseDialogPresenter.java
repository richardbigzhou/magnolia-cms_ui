/**
 * This file Copyright (c) 2012-2013 Magnolia International
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

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;

import info.magnolia.i18n.I18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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

    @Inject
    public BaseDialogPresenter(ComponentProvider componentProvider, I18nizer i18nizer) {
        this.componentProvider = componentProvider;
        this.i18nizer = i18nizer;
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
    public DialogView start(BaseDialogDefinition definition, UiContext uiContext) {
        this.view = initView();
        definition = i18nizer.decorate(definition);
        this.editorActionAreaPresenter = componentProvider.getComponent(definition.getActionArea().getPresenterClass());
        EditorActionAreaView editorActionAreaView = editorActionAreaPresenter.start(filterActions(), definition.getActionArea(), this, uiContext);

        if (definition.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME)) {
             addShortcut(BaseDialog.COMMIT_ACTION_NAME, KeyCode.S, ModifierKey.CTRL);
        }

        if (definition.getActions().containsKey(BaseDialog.CANCEL_ACTION_NAME)) {
            addShortcut(BaseDialog.CANCEL_ACTION_NAME, KeyCode.ESCAPE);
            addShortcut(BaseDialog.CANCEL_ACTION_NAME, KeyCode.C, ModifierKey.CTRL);
        }
        this.view.setActionAreaView(editorActionAreaView);
        return this.view;
    }

    protected Iterable<ActionDefinition> filterActions() {
        return getDefinition().getActions().values();
    }

    protected Object[] getActionParameters(String actionName) {
        return new Object[]{this};
    }

    public BaseDialogDefinition decorateForI18n(BaseDialogDefinition definition) {
        return i18nizer.decorate(definition);
    }
}
