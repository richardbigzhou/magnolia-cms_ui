/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.field.builder.DialogFieldFactory;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.widget.dialog.MagnoliaDialogView;
import info.magnolia.ui.widget.dialog.MagnoloaDialogPresenter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Implementation of {@link DialogPresenterFactory}.
 */
@Singleton
public class DialogPresenterFactoryImpl implements DialogPresenterFactory {

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private Shell shell;
    private DialogBuilder dialogBuilder;
    private EventBus eventBus;
    private DialogActionFactory actionFactory;
    private DialogFieldFactory dialogFieldFactory;
    private ComponentProvider componentProvider;

    @Inject
    public DialogPresenterFactoryImpl(ComponentProvider componentProvider, DialogDefinitionRegistry dialogDefinitionRegistry, DialogBuilder dialogBuilder, DialogFieldFactory dialogFieldFactory, Shell shell, @Named("admincentral") EventBus eventBus, final DialogActionFactory actionFactory) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.dialogBuilder = dialogBuilder;
        this.dialogFieldFactory = dialogFieldFactory;
        this.shell = shell;
        this.eventBus = eventBus;
        this.actionFactory = actionFactory;
        this.componentProvider = componentProvider;
    }

    @Override
    public MagnoloaDialogPresenter.Presenter createDialog(String dialogName) {

        DialogDefinition definition = getDialogDefinition(dialogName);
        return getDialogPresenter(definition);

    }

    @Override
    public DialogDefinition getDialogDefinition(String dialogName) throws RuntimeException {
        DialogDefinition dialogDefinition;
        try {
            dialogDefinition = dialogDefinitionRegistry.get(dialogName);
        } catch (RegistrationException e1) {
            throw new RuntimeException(e1);
        }

        if (dialogDefinition == null) {
            throw new IllegalArgumentException("No dialog definition registered for name [" + dialogName + "]");
        }
        return dialogDefinition;
    }

    @Override
    public MagnoloaDialogPresenter.Presenter getDialogPresenter(DialogDefinition definition) {
        MagnoliaDialogView view = componentProvider.getComponent(MagnoliaDialogView.class);
        return new DialogPresenter(view, dialogBuilder, dialogFieldFactory, definition, shell, eventBus, actionFactory);


    }
}
