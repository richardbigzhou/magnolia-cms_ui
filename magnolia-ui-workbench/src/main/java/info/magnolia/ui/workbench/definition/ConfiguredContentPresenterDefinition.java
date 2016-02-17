/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.workbench.definition;

import info.magnolia.ui.workbench.ContentPresenter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Default configured implementation for {@link ContentPresenterDefinition}.
 */
public class ConfiguredContentPresenterDefinition implements ContentPresenterDefinition {

    private String name;

    private String viewType;

    private Class<? extends ContentPresenter> implementationClass;

    private String icon;

    private boolean active;

    private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

    @Override
    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    @Override
    public Class<? extends ContentPresenter> getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(Class<? extends ContentPresenter> implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void addColumn(ColumnDefinition columnDefinition) {
        columns.add(columnDefinition);
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
