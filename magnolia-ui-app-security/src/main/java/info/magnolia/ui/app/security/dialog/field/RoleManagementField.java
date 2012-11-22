/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.security.dialog.field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.TwinColSelect;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.admincentral.field.builder.SelectFieldBuilder;
import info.magnolia.ui.model.field.definition.SelectFieldOptionDefinition;

/**
 * GUI builder for the Role Management field.
 */
public class RoleManagementField extends SelectFieldBuilder<RoleManagementFieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(RoleManagementField.class);

    public RoleManagementField(RoleManagementFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
        definition.setOptions(getSelectFieldOptionDefinition());
    }


    @Override
    protected AbstractSelect buildField() {
        super.buildField();
        select.setMultiSelect(true);
        select.setNullSelectionAllowed(true);
        return select;
    }

    @Override
    protected AbstractSelect createSelectionField() {
        return new TwinColSelect();
    }

    /**
     * Returns the available roles with those already assigned marked selected, according to the current node.
     */
    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition(){
        List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        Map<String,String> allRoles = getAllRoles();  // name,uuid
        List<String> assignedGroups = getAssignedRoles();
        for (String name : allRoles.keySet()) {
            String uuid = allRoles.get(name);
            SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
            option.setValue(uuid);
            option.setLabel(name);
            if (assignedGroups.contains(uuid)) {
                option.setSelected(true);
            }
            options.add(option);
        }
        return options;
    }

    private Map<String,String> getAllRoles() {
        Map<String,String> roles = new HashMap<String,String>();
        try {
            NodeIterator ni = QueryUtil.search(RepositoryConstants.USER_ROLES, "SELECT * FROM ["+MgnlNodeType.ROLE+"]");
            while (ni.hasNext()) {
                Node n = ni.nextNode();
                String name = n.getName();
                String uuid = n.getIdentifier();
                roles.put(name, uuid);
            }
        } catch (Exception e) {
            log.error("Cannot read roles from the ["+RepositoryConstants.USER_ROLES+"] workspace: "+e.getMessage());
            log.debug("Cannot read roles from the ["+RepositoryConstants.USER_ROLES+"] workspace.", e);
        }
        return roles;
    }

    private List<String> getAssignedRoles() {
        List<String> roles = new ArrayList<String>();
        Node mainNode = getRelatedNode(item);
        try {
            Node groupsNode = mainNode.getNode("roles");
            if (groupsNode == null) {
                // shouldn't happen, just in case
                return roles;
            }
            PropertyIterator pi = groupsNode.getProperties();
            while (pi.hasNext()) {
                Property p = pi.nextProperty();
                if (!p.getName().startsWith("jcr:")) {
                    roles.add(p.getString());
                }
            }
        } catch (PathNotFoundException pnfe) {
            // subnode does not exist, so just return (an empty) list
            return roles;
        } catch (RepositoryException re) {
            log.error("Cannot read assigned roles of the node ["+mainNode+"]: "+re.getMessage());
            log.debug("Cannot read assigned roles of the node ["+mainNode+"].", re);
        }
        return roles;
    }

}
