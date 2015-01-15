/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import info.magnolia.task.Task;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.PulseBeanItem;

import java.util.List;
import java.util.Map;

/**
 * Represents a generic Task object as a Vaadin BeanItem so to be consumed by the UI.
 * Adds the possibility to read information from the {@link TaskDefinition}, in case this would be needed.
 * 
 * @param <T> task.
 * @param <D> definition.
 */
public class TaskItem<T extends Task, D extends TaskDefinition> extends PulseBeanItem {

    private D definition;

    public TaskItem(T bean, D definition) {
        super(bean);
        this.definition = definition;
    }

    public TaskItem(T bean, D definition, String[] propertyIds) {
        super(bean, propertyIds);
        this.definition = definition;
    }

    public TaskItem(T bean, D definition, String[] propertyIds, Map<String, List<String>> nestedPropertyIds) {
        super(bean, propertyIds, nestedPropertyIds);
        this.definition = definition;
    }

    public D getDefinition() {
        return definition;
    }
}