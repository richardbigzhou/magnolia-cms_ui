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
package info.magnolia.ui.dialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.vaadin.view.ModalLayer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.vaadin.data.Item;

/**
 * Implementation of {@link FormDialogPresenterFactory}.
 */
@Singleton
public class FormDialogPresenterFactoryImpl implements FormDialogPresenterFactory {

    private final ComponentProvider componentProvider;
    private final DialogDefinitionRegistry dialogDefinitionRegistry;

    @Inject
    public FormDialogPresenterFactoryImpl(ComponentProvider componentProvider, DialogDefinitionRegistry dialogDefinitionRegistry) {

        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.componentProvider = componentProvider;
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
    public FormDialogPresenter openDialog(String dialogName, Item item, ModalLayer modalLayer) {
        final DialogDefinition definition = getDialogDefinition(dialogName);
        FormDialogPresenter formDialogPresenter = componentProvider.getComponent(FormDialogPresenter.class);

        formDialogPresenter.start(item, definition, modalLayer);
        return formDialogPresenter;
    }

}
