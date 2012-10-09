/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.column.definition;

/**
 * Base implementation for all special ColumnDefinitions. Some subclass do not add additional
 * behavior but still are required because in jcr we configure ColumnDefinition to Column mappings
 * and only with specific Definitions we know what Column-Type to map to.
 */
public abstract class AbstractColumnDefinition implements ColumnDefinition {

    private String name;

    private String label;

    private int width = -1;

    private float expandRatio = 1.0f;

    private boolean sortable = false;

    private String formatterClass;

    private String propertyName;

    private boolean displayInDialog = true;

    private boolean searchable = true;

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /**
     * {@inheritDoc}
     * <p>Default value is -1, meaning no explicit width assigned.
     */
    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    @Override
    public String getFormatterClass() {
        return this.formatterClass;
    }

    public void setFormatterClass(String formatter) {
        this.formatterClass = formatter;
    }

    /**
     * The concrete type represented in this column, ie Long, Double, Date, etc. By default, it assumes a String (which should be a good match in most cases).
     * Subclasses are responsible for returning the actual type.
     */
    @Override
    public Class<?> getType() {
        return String.class;
    }

    /**
     * By default returns <code>true</code>.
     */
    @Override
    public boolean isDisplayInDialog() {
        return displayInDialog;
    }

    public void setDisplayInDialog(boolean displayInDialog) {
        this.displayInDialog = displayInDialog;
    }
    /**
     * {@inheritDoc}
     * <p>
     * Default value is 1.0.
     */
    @Override
    public float getExpandRatio() {
        return expandRatio;
    }

    public void setExpandRatio(float expandRatio) {
        this.expandRatio = expandRatio;
    }

    /**
     * {@inheritDoc}
     * <p>
     * By default returns <code>true</code>.
     */
    @Override
    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }
}
