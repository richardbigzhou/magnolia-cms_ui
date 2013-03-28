/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ImportCommand;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.model.action.CommandActionBase;

import java.util.Map;

import javax.inject.Inject;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI action that allows to import a Node in a XML format.
 * 
 */
public class ImportAction extends CommandActionBase<ImportActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ImportAction.class);

    @Inject
    public ImportAction(ImportActionDefinition definition, Node node, CommandsManager commandsManager) {
        super(definition, node, commandsManager);
    }

    @Override
    protected Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = super.buildParams(node);
        try {
            params.put(ImportCommand.IMPORT_XML_STREAM, ((BinaryImpl) node.getNode("import").getProperty(JcrConstants.JCR_DATA)).getStream());
            params.put(ImportCommand.IMPORT_IDENTIFIER_BEHAVIOR, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            params.put(ImportCommand.IMPORT_XML_FILE_NAME, node.getNode("import").getProperty(FileProperties.PROPERTY_FILENAME).getString());
        } catch (RepositoryException re) {
            log.warn("Not able to init ImportCommand Parameter for the following Node {} ", NodeUtil.getNodePathIfPossible(node));
        }
        return params;
    }
}
