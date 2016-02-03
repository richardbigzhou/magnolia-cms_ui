/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.form.field.definition;

import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Field definition for a select field.
 */
public class SelectFieldDefinition extends ConfiguredFieldDefinition {

    public static final String OPTION_VALUE_PROPERTY_NAME = "value";
    public static final String OPTION_NAME_PROPERTY_NAME = "name";
    public static final String OPTION_SELECTED_PROPERTY_NAME = "selected";
    public static final String OPTION_ICONSRC_PROPERTY_NAME = "iconSrc";
    public static final String OPTION_LABEL_PROPERTY_NAME = "label";

    public static final String DEFAULT_REPOSITORY_NAME = RepositoryConstants.CONFIG;

    private String path;

    private String repository = DEFAULT_REPOSITORY_NAME;

    private String valueProperty = OPTION_VALUE_PROPERTY_NAME;

    private String labelProperty = OPTION_LABEL_PROPERTY_NAME;

    private int filteringMode = 0;

    private boolean sortOptions = true;

    private List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();

    public List<SelectFieldOptionDefinition> getOptions() {
        return options;
    }

    public void setOptions(List<SelectFieldOptionDefinition> options) {
        this.options = options;
    }

    public void addOption(SelectFieldOptionDefinition option) {
        options.add(option);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getValueProperty() {
        return valueProperty;
    }

    public void setValueProperty(String valueProperty) {
        this.valueProperty = valueProperty;
    }

    public String getLabelProperty() {
        return labelProperty;
    }

    public void setLabelProperty(String labelProperty) {
        this.labelProperty = labelProperty;
    }

    public FilteringMode getFilteringMode() {
        switch (filteringMode) {
        case 1:
            return FilteringMode.CONTAINS;
        case 2:
            return FilteringMode.STARTSWITH;
        default:
            return FilteringMode.OFF;
        }
    }

    public void setFilteringMode(int filteringMode) {
        this.filteringMode = filteringMode;
    }

    /**
     * By default, options labels are sorted alphabetically (in ascending order) unless <code>false</code> is specified. In that case, the JCR "natural order" should be kept.
     */
    public void setSortOptions(boolean sortOptions) {
        this.sortOptions = sortOptions;
    }

    public boolean isSortOptions() {
        return sortOptions;
    }
}
