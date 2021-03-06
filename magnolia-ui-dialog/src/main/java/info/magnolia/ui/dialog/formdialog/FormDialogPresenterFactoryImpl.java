/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.dialog.formdialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link FormDialogPresenterFactory}. Uses {@link DialogDefinitionRegistry} to fetch dialog definition.
 */
@Singleton
public class FormDialogPresenterFactoryImpl implements FormDialogPresenterFactory {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DialogDefinitionRegistry registry;

    private ComponentProvider componentProvider;

    @Inject
    public FormDialogPresenterFactoryImpl(DialogDefinitionRegistry registry, ComponentProvider componentProvider) {
        this.registry = registry;
        this.componentProvider = componentProvider;
    }

    @Override
    public FormDialogPresenter createFormDialogPresenter(String dialogId) {
        DialogDefinition dialogDefinition = registry.getProvider(dialogId).get();
        if (dialogDefinition instanceof FormDialogDefinition) {
            return createFormDialogPresenter((FormDialogDefinition) dialogDefinition);
        } else if (dialogDefinition == null) {
            log.error("Could not create FormDialogPresenter: dialogId {{}} was not found in registry.", dialogId);
        } else {
            log.error("Could not create FormDialogPresenter: dialog's presenterClass was not a FormDialogPresenter.");
        }
        return null;
    }

    @Override
    public FormDialogPresenter createFormDialogPresenter(FormDialogDefinition definition) {
        return componentProvider.newInstance(definition.getPresenterClass(), definition);
    }
}
