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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.field.builder.FieldTypeProvider;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.widget.dialog.Dialog;
import info.magnolia.ui.widget.dialog.DialogView;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Item;

/**
 * DialogPresenter.
 * @version $Id$
 */
public class DialogPresenter implements DialogView.Presenter {

    private final DialogBuilder dialogBuilder;
    private final FieldTypeProvider fieldTypeBuilder;
    private final DialogDefinition dialogDefinition;
    private final MagnoliaShell shell;
    private final EventBus eventBus;
    private final DialogView view;
    private final DialogActionFactory actionFactory;
    private final Map<String, ActionDefinition> actionMap = new HashMap<String, ActionDefinition>();
    private Item item;

    public DialogPresenter(final DialogView view, final DialogBuilder dialogBuilder, final FieldTypeProvider fieldTypeBuilder, final DialogDefinition dialogDefinition, final MagnoliaShell shell, final EventBus eventBus, final DialogActionFactory actionFactory) {
        this.view = view;
        this.dialogBuilder = dialogBuilder;
        this.fieldTypeBuilder = fieldTypeBuilder;
        this.dialogDefinition = dialogDefinition;
        this.shell = shell;
        this.eventBus = eventBus;
        this.actionFactory = actionFactory;

        this.view.setPresenter(this);

        initActions(dialogDefinition);
    }

    @Override
    public void editItem(final Item item) {
        this.item = item;
        dialogBuilder.build(fieldTypeBuilder, dialogDefinition, item, view);
        shell.openDialog((Dialog)view.asVaadinComponent());
    }

    @Override
    public void closeDialog() {
        shell.removeDialog(view.asVaadinComponent());
        // clear the view!
    }

    @Override
    public void executeAction(final String actionName) {

        final ActionDefinition actionDefinition = actionMap.get(actionName);
        final Action action = actionFactory.createAction(actionDefinition, this);
        try {
            action.execute();
        } catch (final ActionExecutionException e) {
            e.printStackTrace();
        }
    }

    private void initActions(final DialogDefinition dialogDefinition) {

        for (final DialogActionDefinition action : dialogDefinition.getActions()) {
            actionMap.put(action.getName(), action.getActionDefinition());
        }
    }

    @Override
    public Shell getShell() {
        return shell;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

}
