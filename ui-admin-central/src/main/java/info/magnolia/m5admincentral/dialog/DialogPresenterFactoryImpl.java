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
package info.magnolia.m5admincentral.dialog;

import info.magnolia.m5admincentral.dialog.builder.DialogBuilder;
import info.magnolia.m5vaadin.shell.MagnoliaShell;
import info.magnolia.objectfactory.annotation.SessionScoped;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;

import javax.inject.Inject;

/**
 * Implementation of {@link DialogPresenterFactory}.
 *
 * @version $Id$
 */
@SessionScoped
public class DialogPresenterFactoryImpl implements DialogPresenterFactory {

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private MagnoliaShell shell;
    private DialogBuilder dialogBuilder;

    @Inject
    public DialogPresenterFactoryImpl(DialogDefinitionRegistry dialogDefinitionRegistry, DialogBuilder dialogBuilder, MagnoliaShell shell) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.dialogBuilder = dialogBuilder;
        this.shell = shell;
    }

    @Override
    public DialogPresenter createDialog(String dialogName) {

        DialogDefinition dialogDefinition;
        try {
            dialogDefinition = dialogDefinitionRegistry.get(dialogName);
        } catch (RegistrationException e1) {
            throw new RuntimeException(e1);
        }

        if (dialogDefinition == null) {
            throw new IllegalArgumentException("No dialog definition registered for name [" + dialogName + "]");
        }

        return new DialogPresenter(dialogBuilder, dialogDefinition, shell);
    }
}
