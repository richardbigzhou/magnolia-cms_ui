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
package info.magnolia.ui.admincentral.shellapp.pulse.message.registry;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ConfiguredItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * {@link ItemViewDefinitionProvider} that instantiates a message-view definition from a configuration node.
 *
 * @deprecated since 5.4 use DefinitionProvider<ItemViewDefinition> and configuration sources.
 */
@Deprecated
public class ConfiguredMessageViewDefinitionProvider implements ItemViewDefinitionProvider {

    private final String id;

    private final ConfiguredItemViewDefinition itemViewDefinition;

    public ConfiguredMessageViewDefinitionProvider(String id, Node configNode) throws RepositoryException, Node2BeanException {
        this.id = id;
        this.itemViewDefinition = (ConfiguredItemViewDefinition) Components.getComponent(Node2BeanProcessor.class).toBean(configNode, ItemViewDefinition.class);
        if (this.itemViewDefinition != null) {
            this.itemViewDefinition.setId(id);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ItemViewDefinition getItemViewDefinition() throws RegistrationException {
        return itemViewDefinition;
    }
}
