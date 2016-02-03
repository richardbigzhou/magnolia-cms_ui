/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.framework.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replace the 5.0 MultiLinkFieldDefinition by a MultiValueFieldDefinition. <br>
 */
public class ReplaceMultiLinkFieldDefinitionTask extends QueryTask {

    private static final Logger log = LoggerFactory.getLogger(ReplaceMultiLinkFieldDefinitionTask.class);

    public ReplaceMultiLinkFieldDefinitionTask(String name, String description, String repositoryName, String query) {
        super(name, description, repositoryName, query);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node fieldNodeDefinition) {
        String nodePath = NodeUtil.getPathIfPossible(fieldNodeDefinition);
        try {
            if (StringUtils.contains(nodePath, "fields")) {

                // Create a field child node
                Node field = fieldNodeDefinition.addNode("field", NodeTypes.ContentNode.NAME);
                field.setProperty("class", LinkFieldDefinition.class.getName());

                // Move IdentifierToPathConverter to the field node
                if (fieldNodeDefinition.hasNode("identifierToPathConverter")) {
                    NodeUtil.moveNode(fieldNodeDefinition.getNode("identifierToPathConverter"), field);
                }

                // Move the properties to the field definition
                copyAndRemoveProperty(fieldNodeDefinition, field, "appName");
                copyAndRemoveProperty(fieldNodeDefinition, field, "buttonSelectNewLabel");
                copyAndRemoveProperty(fieldNodeDefinition, field, "buttonSelectOtherLabel");
                copyAndRemoveProperty(fieldNodeDefinition, field, "fieldEditable");
                copyAndRemoveProperty(fieldNodeDefinition, field, "targetWorkspace");
                copyAndRemoveProperty(fieldNodeDefinition, field, "type");

                // Change the class property
                fieldNodeDefinition.setProperty("class", MultiValueFieldDefinition.class.getName());

            } else {
                log.debug("The following node {} is not a field definition. ", nodePath);
            }
        } catch (RepositoryException re) {
            log.warn("Could not Migrate 'MultiLinkFieldDefinition' of the following node {}.", nodePath);
        }
    }

    private void copyAndRemoveProperty(Node sourceNode, Node targetNode, String propertyName) throws RepositoryException {
        if (sourceNode.hasProperty(propertyName)) {
            targetNode.setProperty(propertyName, sourceNode.getProperty(propertyName).getString());
            sourceNode.getProperty(propertyName).remove();
        }
    }
}
