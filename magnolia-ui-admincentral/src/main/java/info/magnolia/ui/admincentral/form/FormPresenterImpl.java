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
package info.magnolia.ui.admincentral.form;

import com.vaadin.data.Item;
import info.magnolia.ui.admincentral.field.builder.FieldFactory;
import info.magnolia.ui.admincentral.form.action.FormActionFactory;
import info.magnolia.ui.admincentral.form.builder.FormBuilder;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.form.FormView;

/**
 * FormPresenterImpl.
 */
public class FormPresenterImpl implements FormPresenter {

    private final FormView view;
    private final FormBuilder formBuilder;
    private final FieldFactory fieldFactory;
    private final FormDefinition dialogDefinition;
    private final Shell shell;
    private final EventBus eventBus;
    private final FormActionFactory actionFactory;

    public FormPresenterImpl(final FormView view, final FormBuilder formBuilder, final FieldFactory fieldFactory,
                             final FormDefinition dialogDefinition, final Shell shell, EventBus eventBus, final FormActionFactory actionFactory) {


        this.view = view;
        this.formBuilder = formBuilder;
        this.fieldFactory = fieldFactory;
        this.dialogDefinition = dialogDefinition;
        this.shell = shell;
        this.eventBus = eventBus;
        this.actionFactory = actionFactory;
    }

    @Override
    public Callback getCallback() {
        return null;
    }

    @Override
    public EventBus getEventBus() {
        return null;
    }

    @Override
    public void addAction(String actionName, String actionLabel, DialogView.DialogActionListener callback) {

    }

    @Override
    public void addActionCallback(String actionName, DialogView.DialogActionListener callback) {

    }

    @Override
    public void showValidation(boolean isVisible) {

    }

    @Override
    public FormView start(Item item, FormPresenter.Callback callback) {
        return null;
    }

    @Override
    public Item getItemDataSource() {
        return null;
    }

    @Override
    public FormView getView() {
        return null;
    }
}
