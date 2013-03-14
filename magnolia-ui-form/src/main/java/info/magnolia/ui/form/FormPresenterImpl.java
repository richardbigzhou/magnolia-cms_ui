/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.form;

import info.magnolia.event.EventBus;
import info.magnolia.ui.form.field.builder.FieldFactory;
import info.magnolia.ui.form.action.FormActionFactory;
import info.magnolia.ui.form.definition.FormActionDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;
import info.magnolia.ui.vaadin.form.FormView;

import com.vaadin.data.Item;

/**
 * Implementation of {@link FormPresenter}.
 * Delegates building of {@link FormView}s to the {@link FormBuilder}.
 * Maps the {@link Item} to the form and registers callback function created by the caller.
 * Binds the actions defined in {@link FormActionDefinition} to the form.
 * Provides methods for checking and displaying the validation of forms.
 */
public class FormPresenterImpl implements FormPresenter {

    private final FormView view;
    private final FormBuilder formBuilder;
    private final FieldFactory fieldFactory;
    private final FormDefinition formDefinition;
    private final EventBus eventBus;
    private final FormActionFactory actionFactory;
    private Item item;
    private Callback callback;

    public FormPresenterImpl(final FormView view, final FormBuilder formBuilder, final FieldFactory fieldFactory, final FormDefinition formDefinition, EventBus eventBus, final FormActionFactory actionFactory) {

        this.view = view;
        this.formBuilder = formBuilder;
        this.fieldFactory = fieldFactory;
        this.formDefinition = formDefinition;
        this.eventBus = eventBus;
        this.actionFactory = actionFactory;
        initActions(formDefinition);
    }

    private void initActions(final FormDefinition formDefinition) {
        for (final FormActionDefinition action : formDefinition.getActions()) {
            addAction(action.getName(), action.getLabel(), new EditorLikeActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {
                    final ActionDefinition actionDefinition = action.getActionDefinition();
                    final Action action1 = actionFactory.createAction(actionDefinition, FormPresenterImpl.this);
                    try {
                        action1.execute();
                    } catch (final ActionExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public Callback getCallback() {
        return callback;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void addAction(String actionName, String actionLabel, EditorLikeActionListener callback) {
        view.addAction(actionName, actionLabel, callback);

    }

    @Override
    public void showValidation(boolean isVisible) {
        view.showValidation(isVisible);
    }

    @Override
    public boolean isValid() {
        return view.isValid();
    }

    /**
     * Builds the form based on formDefinition.
     */
    @Override
    public FormView start(Item item, FormPresenter.Callback callback) {
        this.item = item;
        this.callback = callback;
        formBuilder.buildForm(fieldFactory, formDefinition, item, view, null);
        return view;
    }

    /**
     * Builds the form based on formDefinition.
     */
    @Override
    public FormView start(Item item, FormItem parent) {
        this.item = item;
        formBuilder.buildForm(fieldFactory, formDefinition, item, view, parent);
        return view;
    }

    @Override
    public Item getItemDataSource() {
        return item;
    }

    @Override
    public FormView getView() {
        return view;
    }
}
