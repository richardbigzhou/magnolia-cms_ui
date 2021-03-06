/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.ui.api.action.ConfiguredActionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an action which delegates to another action by specified node type.
 */
public class DelegateByNodeTypeActionDefinition extends ConfiguredActionDefinition {

    private List<NodeTypeToActionMapping> nodeTypeToActionMappings = new ArrayList<>();

    public DelegateByNodeTypeActionDefinition() {
        setImplementationClass(DelegateByNodeTypeAction.class);
    }

    public List<NodeTypeToActionMapping> getNodeTypeToActionMappings() {
        return nodeTypeToActionMappings;
    }

    public void setNodeTypeToActionMappings(List<NodeTypeToActionMapping> nodeTypeToActionMappings) {
        this.nodeTypeToActionMappings = nodeTypeToActionMappings;
    }

    /**
     * Bean which maps node type to action.
     */
    public static class NodeTypeToActionMapping {

        private String nodeType;
        private String action;

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

    }

}
