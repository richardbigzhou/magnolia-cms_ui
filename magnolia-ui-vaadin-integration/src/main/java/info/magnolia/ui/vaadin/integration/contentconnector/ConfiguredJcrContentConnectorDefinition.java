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
package info.magnolia.ui.vaadin.integration.contentconnector;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition} for JCR-specific sub-apps.
 */
public class ConfiguredJcrContentConnectorDefinition extends ConfiguredContentConnectorDefinition implements JcrContentConnectorDefinition {

    private String defaultOrder;

    private List<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();

    private boolean includeProperties = false;

    private boolean includeSystemNodes;

    private String workspace;

    private String rootPath = "/";

    public ConfiguredJcrContentConnectorDefinition() {
        setImplementationClass(JcrContentConnector.class);
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String path) {
        this.rootPath = path;
    }

    @Override
    public List<NodeTypeDefinition> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<NodeTypeDefinition> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void addNodeType(NodeTypeDefinition nodeTypeDefinition) {
        nodeTypes.add(nodeTypeDefinition);
    }

    @Override
    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    @Override
    public boolean isIncludeSystemNodes() {
        return includeSystemNodes;
    }

    public void setIncludeSystemNodes(boolean includeSystemNodes) {
        this.includeSystemNodes = includeSystemNodes;
    }

    @Override
    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }
}
