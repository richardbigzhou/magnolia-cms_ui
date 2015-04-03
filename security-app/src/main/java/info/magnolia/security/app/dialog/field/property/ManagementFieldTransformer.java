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
package info.magnolia.security.app.dialog.field.property;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.vaadin.data.Item;

/**
 * {@link info.magnolia.ui.form.field.property.PropertyHandler} implementation used for {@link info.magnolia.security.app.dialog.field.RoleManagementFieldFactory} and {@link info.magnolia.security.app.dialog.field.GroupManagementFieldFactory}.
 * 
 * @param <T>
 */
public class ManagementFieldTransformer<T> extends BasicTransformer<T> {
    private Set<String> assignedEntity;
    private String entityName;

    @Inject
    public ManagementFieldTransformer(Item parent, ConfiguredFieldDefinition definition, Class<?> type, Set<String> assignedEntity, String entityName) {
        super(parent, definition, (Class<T>) type);
        this.assignedEntity = assignedEntity;
        this.entityName = entityName;
    }


    /**
     * Do nothing yet.
     * Save is currently handled by an embedded logic.
     */
    @Override
    public void writeToItem(T newValue) {
        if (newValue == null) {
            newValue = (T) new HashSet();
        }
        super.writeToItem(newValue);
    }

    @Override
    public T readFromItem() {
        DefaultProperty<Set> prop = new DefaultProperty<Set>(Set.class, this.assignedEntity);
        relatedFormItem.addItemProperty(this.entityName, prop);
        return (T) prop.getValue();
    }

}
