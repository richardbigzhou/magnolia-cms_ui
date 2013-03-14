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
package info.magnolia.ui.workbench.config;

import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;

/**
 * Builder for building a column definition.
 *
 * @param <T> type of field definition
 * @see WorkbenchBuilder
 * @see ColumnConfig
 */
public class ColumnBuilder<T extends AbstractColumnDefinition> {

    private T definition;

    public ColumnBuilder(T definition) {
        this.definition = definition;
    }

    public ColumnBuilder<T> name(String name) {
        definition.setName(name);
        return this;
    }

    public ColumnBuilder<T> label(String label) {
        definition.setLabel(label);
        return this;
    }

    public ColumnBuilder<T> expandRatio(float expandRatio) {
        definition.setExpandRatio(expandRatio);
        return this;
    }

    public ColumnBuilder<T> width(int width) {
        definition.setWidth(width);
        return this;
    }

    public ColumnBuilder<T> sortable(boolean sortable) {
        definition.setSortable(sortable);
        return this;
    }

    public ColumnBuilder<T> formatterClass(Class<? extends ColumnFormatter> formatterClass) {
        definition.setFormatterClass(formatterClass);
        return this;
    }

    public ColumnBuilder<T> propertyName(String propertyName) {
        definition.setPropertyName(propertyName);
        return this;
    }

    public ColumnBuilder<T> displayInDialog(boolean displayInDialog) {
        definition.setDisplayInDialog(displayInDialog);
        return this;
    }

    public T exec() {
        return definition;
    }
}
