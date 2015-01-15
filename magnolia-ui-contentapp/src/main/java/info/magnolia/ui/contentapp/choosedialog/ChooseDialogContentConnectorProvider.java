/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.DefaultContentConnector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;


/**
 * {@link ContentConnector} provider for choose dialogs.
 */
@Singleton
public class ChooseDialogContentConnectorProvider implements Provider<ContentConnector> {

    private final ComponentProvider componentProvider;
    private final ChooseDialogDefinition chooseDialogDefinition;

    @Inject
    public ChooseDialogContentConnectorProvider(ComponentProvider componentProvider, ChooseDialogDefinition chooseDialogDefinition) {
        this.componentProvider = componentProvider;
        this.chooseDialogDefinition = chooseDialogDefinition;
    }

    @Override
    public ContentConnector get() {
        ContentConnectorDefinition contentConnector = chooseDialogDefinition.getContentConnector();
        if (contentConnector != null) {
            return componentProvider.newInstance(contentConnector.getImplementationClass(), contentConnector);
        }
        return new DefaultContentConnector();
    }
}
