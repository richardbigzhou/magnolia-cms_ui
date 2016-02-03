/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItem;

/**
 * Abstract {@link BeanItem} used for beans in pulse. Allows passing a Map of nested beans to the constructor.
 *
 * @param <B> the bean wrapped in {@link BeanItem}.
 */
public class PulseBeanItem<B> extends BeanItem<B> {

    private static final Logger log = LoggerFactory.getLogger(PulseBeanItem.class);

    public PulseBeanItem(B bean) {
        super(bean);
    }

    public PulseBeanItem(B bean, String... propertyIds) {
        super(bean, propertyIds);
    }

    public PulseBeanItem(B bean, String[] propertyIds, Map<String, List<String>> nestedPropertyIds) {
        super(bean, propertyIds);
        for (String parentProperty : nestedPropertyIds.keySet()) {
            List<String> nestedProperties = nestedPropertyIds.get(parentProperty);
            expandProperty(parentProperty, nestedProperties.toArray(new String[nestedProperties.size()]));
        }
    }

    /**
     * Adds a nested property to the item.
     *
     * @param nestedPropertyId
     *            property id to add. This property must not exist in the item
     *            already and must of of form "field1.field2" where field2 is a
     *            field in the object referenced to by field1
     */
    @Override
    public void addNestedProperty(String nestedPropertyId) {
        try {
            addItemProperty(nestedPropertyId, new NestedMapProperty<Object>(getBean(), nestedPropertyId));
        }
        catch (IllegalArgumentException e) {
            log.debug("Could not add property '{}'", nestedPropertyId, e);
        }
    }
}
