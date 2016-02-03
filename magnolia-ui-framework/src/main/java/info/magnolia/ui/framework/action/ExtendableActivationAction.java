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
package info.magnolia.ui.framework.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;

/**
 * Extends the {@link ActivationAction} by the possibility to pass parameters in the constructor.
 */
public class ExtendableActivationAction extends ActivationAction {

    private Map<String, Object> parameters;

    @Inject
    public ExtendableActivationAction(ActivationActionDefinition definition, JcrItemAdapter item, Map<String, Object> parameters,  CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, SubAppContext uiContext, ModuleRegistry moduleRegistry) {
        super(definition, item, commandsManager, admincentralEventBus, uiContext, moduleRegistry);
        this.parameters = parameters;
    }

    @Override
    protected Map<String, Object> buildParams(Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.putAll(parameters);
        return params;
    }
}
