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
package info.magnolia.ui.vaadin.integration.datasource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration object used by {@link DataSource} for {@link com.vaadin.data.Container} construction.
 */
public class ContainerConfiguration {

    private List<Object> sortableProperties = new ArrayList<Object>();

    private List<Object> containerPropertyIds = new ArrayList<Object>();

    private Map<Object, Object> propertyTypes = new LinkedHashMap<Object, Object>();

    private String viewTypeId;

    public List<Object> getContainerPropertyIds() {
        return containerPropertyIds;
    }

    public void setContainerPropertyIds(List<Object> containerPropertyIds) {
        this.containerPropertyIds = containerPropertyIds;
    }

    public Map<Object, Object> getPropertyTypes() {
        return propertyTypes;
    }

    public void setPropertyTypes(Map<Object, Object> propertyTypes) {
        this.propertyTypes = propertyTypes;
    }

    public String getViewTypeId() {
        return viewTypeId;
    }

    public void setViewTypeId(String viewTypeId) {
        this.viewTypeId = viewTypeId;
    }

    public void addProperty(Object propertyId, Object propertyType) {
        propertyTypes.put(propertyId, propertyType);
        containerPropertyIds.add(propertyId);
    }

    public void addSortableProperty(Object propertyId) {
        sortableProperties.add(propertyId);
    }

    public List<Object> getSortableProperties() {
        return sortableProperties;
    }

    public void setSortableProperties(List<Object> sortableProperties) {
        this.sortableProperties = sortableProperties;
    }
}
