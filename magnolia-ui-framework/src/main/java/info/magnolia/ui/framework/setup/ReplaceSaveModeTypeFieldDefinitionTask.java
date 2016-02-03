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

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodeTransformer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rename or remove the 5.0 node field definition 'saveModeType'.<br>
 * In 5.1, this definition do not exist anymore and is replaced by 'transformerClass' definition.
 */
public class ReplaceSaveModeTypeFieldDefinitionTask extends QueryTask {

    private static final Logger log = LoggerFactory.getLogger(ReplaceSaveModeTypeFieldDefinitionTask.class);

    public ReplaceSaveModeTypeFieldDefinitionTask(String name, String description, String repositoryName, String query) {
        super(name, description, repositoryName, query);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        String nodePath = NodeUtil.getPathIfPossible(node);
        try {
            if (StringUtils.contains(nodePath, "fields")) {
                if (node.hasProperty("multiValueHandlerClass")) {
                    String multiValueHandlerClass = node.getProperty("multiValueHandlerClass").getString();
                    if (StringUtils.equals("info.magnolia.ui.form.field.property.MultiValuesHandler", multiValueHandlerClass)) {
                        // Simply remove the node. The field definition already contains the default transformerClass.
                        node.remove();
                        log.debug("The following node will be removed {}. The field definition already contain the definition of the default transformerClass", nodePath);
                    } else if (StringUtils.equals("info.magnolia.ui.form.field.property.SubNodesValueHandler", multiValueHandlerClass)) {
                        Node parent = node.getParent();
                        parent.setProperty("transformerClass", MultiValueSubChildrenNodeTransformer.class.getName());
                        node.remove();

                    } else if (StringUtils.equals("info.magnolia.ui.form.field.property.CommaSeparatedValueHandler", multiValueHandlerClass)) {
                        Node parent = node.getParent();
                        parent.setProperty("transformerClass", MultiValueJSONTransformer.class.getName());
                        node.remove();
                    } else {
                        log.warn("Unknown value for property 'multiValueHandlerClass' : {}. This node {} will not be handled", multiValueHandlerClass, nodePath);
                    }
                }
            } else {
                log.debug("The following node {} is not a field configuration. The child 'saveModeType' will not be handled. ", nodePath);
            }
        } catch (RepositoryException re) {
            log.warn("Could not Migrate 'saveModeType' child node of the following node {}.", nodePath);
        }
    }

}
