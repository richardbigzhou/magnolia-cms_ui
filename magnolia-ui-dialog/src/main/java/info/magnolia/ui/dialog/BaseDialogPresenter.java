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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.ActionParameterProvider;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.view.EditorActionView;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;

import javax.inject.Inject;

/**
 * Base implementation of {@link DialogPresenter}.
 */
public class BaseDialogPresenter implements DialogPresenter, ActionParameterProvider {

    private DialogView view;

    protected ComponentProvider componentProvider;

    private EditorActionAreaPresenter editorActionAreaPresenter;

    @Inject
    public BaseDialogPresenter(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public ActionAreaPresenter getActionPresenter() {
        return editorActionAreaPresenter;
    }

    @Override
    public void closeDialog() {
        view.close();
    }

    @Override
    public void addShortcut(final String actionName, final int keyCode, final int... modifiers) {
        view.addShortcut(this.editorActionAreaPresenter.bindShortcut(actionName, keyCode, modifiers));
    }

    public DialogView start(BaseDialogDefinition definition, UiContext uiContext) {
        this.view = initView();
        this.editorActionAreaPresenter = componentProvider.getComponent(definition.getActionArea().getPresenterClass());
        EditorActionView editorActionView = editorActionAreaPresenter.start(definition.getActions().values(), definition.getActionArea(), this, uiContext);

        if (definition.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME)) {
             addShortcut(BaseDialog.COMMIT_ACTION_NAME, KeyCode.S, ModifierKey.CTRL);
        }

        if (definition.getActions().containsKey(BaseDialog.CANCEL_ACTION_NAME)) {
            addShortcut(BaseDialog.CANCEL_ACTION_NAME, KeyCode.ESCAPE);
            addShortcut(BaseDialog.CANCEL_ACTION_NAME, KeyCode.C, ModifierKey.CTRL);
        }
        this.view.setActionView(editorActionView);
        return this.view;
    }

    protected DialogView initView() {
        return componentProvider.getComponent(DialogView.class);
    }

    @Override
    public Object[] getActionParameters(String actionName) {
        return new Object[]{this};
    }
}
