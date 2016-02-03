/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.security.app.dialog.field;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.form.field.factory.TwinColSelectFieldFactory;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.TwinColSelect;

/**
 * GUI builder for the Group Management field.
 */
public class GroupManagementFieldFactory extends TwinColSelectFieldFactory<GroupManagementFieldDefinition> {

    /**
     * Internal bean to represent basic group data.
     */
    public static class Group {
        public String name;
        public String uuid;

        public Group(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(GroupManagementFieldFactory.class);

    public GroupManagementFieldFactory(GroupManagementFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
        this.definition.setOptions(getSelectFieldOptionDefinition());
    }

    @Override
    protected AbstractSelect createFieldComponent() {
        super.createFieldComponent();
        select.setMultiSelect(true);
        select.setNullSelectionAllowed(true);
        select.setImmediate(true);
        return select;
    }

    @Override
    protected AbstractSelect createSelectionField() {
        return new TwinColSelect() {

            @Override
            public String getConnectorId() {
                return super.getConnectorId();
            }
            
            /* (non-Javadoc)
             * @see com.vaadin.ui.AbstractComponent#isVisible()
             */
            @Override
            public boolean isVisible() {
                return super.isVisible();
            }
        };
    }

    /**
     * Returns the available groups with those already assigned marked selected,
     * according to the current node.
     */
    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        List<Group> allGroups = getAllGroups(); // name,uuid
        Set<String> assignedGroups = getAssignedGroups();
        String currentUUID = null;
        try {
            currentUUID = getRelatedNode(item).getIdentifier();
        } catch (RepositoryException e) {
            // nothing to do
        }
        for (Group group : allGroups) {
            SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
            option.setValue(group.uuid);
            option.setLabel(group.name);
            if (assignedGroups.contains(group.uuid)) {
                option.setSelected(true);
            }
            if (!group.uuid.equals(currentUUID)) {
                // we don't want the group to be assigned to itself, do we?
                options.add(option);
            }
        }
        return options;
    }

    private List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<Group>();
        try {
            NodeIterator ni = QueryUtil.search(RepositoryConstants.USER_GROUPS, "SELECT * FROM [" + NodeTypes.Group.NAME + "] ORDER BY name()");

            while (ni.hasNext()) {
                Node n = ni.nextNode();
                String name = n.getName();
                String uuid = n.getIdentifier();
                groups.add(new Group(name, uuid));
            }
        } catch (RepositoryException e) {
            log.error("Cannot read groups from the [" + RepositoryConstants.USER_GROUPS + "] workspace.", e);
        }
        return groups;
    }

    private Set<String> getAssignedGroups() {
        Set<String> groups = new HashSet<String>();
        try {
            Node mainNode = getRelatedNode(item);
            if (mainNode.hasNode("groups")) {
                Node groupsNode = mainNode.getNode("groups");
                if (groupsNode == null) {
                    // shouldn't happen, just in case
                    return groups;
                }
                for (PropertyIterator iter = new FilteringPropertyIterator(groupsNode.getProperties(), new JCRMgnlPropertyHidingPredicate());  iter.hasNext();) {
                    Property p = iter.nextProperty();
                    groups.add(p.getString());

                }
            }
        } catch (RepositoryException re) {
            log.error("Cannot read assigned groups.", re);
        }
        return groups;
    }

    @Override
    public com.vaadin.data.Property<?> getOrCreateProperty() {
        DefaultProperty<Set> prop = new DefaultProperty<Set>(Set.class, getAssignedGroups());
        item.addItemProperty("groups", prop);
        return prop;
    }
}
