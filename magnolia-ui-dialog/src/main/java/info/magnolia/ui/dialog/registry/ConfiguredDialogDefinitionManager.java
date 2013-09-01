/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * ObservedManager for dialogs configured in repository.
 */
@Singleton
public class ConfiguredDialogDefinitionManager extends ConfiguredBaseDialogDefinitionManager<DialogDefinition> {

    public static final String DIALOG_CONFIG_NODE_NAME = "dialogs";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public ConfiguredDialogDefinitionManager(ModuleRegistry moduleRegistry, DialogDefinitionRegistry dialogDefinitionRegistry) {
        super(DIALOG_CONFIG_NODE_NAME, moduleRegistry, dialogDefinitionRegistry);
    }

    /**
     * Check if this node can be handle as a ConfiguredDialogDefinition.
     */
    @Override
    protected boolean isDialog(Node dialogNode) throws RepositoryException {
        return dialogNode.hasNode(ConfiguredDialogDefinition.FORM_NODE_NAME)
                || dialogNode.hasNode(ConfiguredDialogDefinition.ACTIONS_NODE_NAME)
                || dialogNode.hasProperty(ConfiguredDialogDefinition.EXTEND_PROPERTY_NAME);
    }

    @Override
    protected DialogDefinitionProvider createProvider(Node dialogNode) throws RepositoryException {
        final String id = createId(dialogNode);
        try {
            return new ConfiguredDialogDefinitionProvider(id, dialogNode);
        } catch (IllegalArgumentException e) {
            // TODO dlipp - suppress stacktrace as long as SCRUM-1749 is not fixed
            log.error("Unable to create provider for dialog [" + id + "]: " + e);
        } catch (Exception e) {
            log.error("Unable to create provider for dialog [" + id + "]", e);
        }
        return null;
    }
}
