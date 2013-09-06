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

import com.vaadin.event.ShortcutListener;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.dialog.actionpresenter.ActionListener;
import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenter;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * Base implementation of {@link DialogPresenter}.
 */
public class BaseDialogPresenter implements DialogPresenter {

    private DialogView view;

    protected ComponentProvider componentProvider;

    @Inject
    public BaseDialogPresenter(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public void closeDialog() {
        view.close();
    }

    @Override
    public void addShortcut(final String actionName, final int keyCode, final int... modifiers) {
        final ShortcutListener shortcut = new ShortcutListener("", keyCode, modifiers) {
            @Override
            public void handleAction(Object sender, Object target) {
                onActionFired(actionName, new HashMap<String, Object>());
            }
        };
        view.addShortcut(shortcut);
    }

    protected void onActionFired(String action, Object... actionContextParams) {}

    public DialogView start(BaseDialogDefinition definition) {
        this.view = initView();
        DialogActionPresenter dialogActionPresenter = componentProvider.newInstance(definition.getActionPresenter().getPresenterClass());
        DialogActionView dialogActionView = dialogActionPresenter.start(definition.getActions().values(), definition.getActionPresenter(), new ActionListener() {
            @Override
            public void onActionFired(String actionName, Object... actionContextParams) {
                BaseDialogPresenter.this.onActionFired(actionName, actionContextParams);
            }
        });
        this.view.setActionView(dialogActionView);
        return this.view;
    }

    protected DialogView initView() {
        return componentProvider.getComponent(DialogView.class);
    }
}
