/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
import java.util.Comparator;
import java.util.List;

import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Basic definition class for various types of select fields.
 * <p>
 * Offers two distinct ways of populating selection options:
 * <ul>
 * <li>an explicit set of {@link SelectFieldOptionDefinition option definitions} can be provided via {@link #setOptions(List)}</li>
 * <li><strong>if no explicit options are specified</strong> - option set can be resolved from a remote path in JCR repository by means of 
 * {@link #setPath(String)}, {@link #setLabelProperty(String)} and {@link #setValueProperty(String)} methods</li>
 * </ul>
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

    private Class<? extends Comparator<SelectFieldOptionDefinition>> comparatorClass;

    private List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();

    private boolean textInputAllowed = false;

    private int pageLength = 0;

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

    public Class<? extends Comparator<SelectFieldOptionDefinition>> getComparatorClass() {
        return comparatorClass;
    }

    public void setComparatorClass(Class<? extends Comparator<SelectFieldOptionDefinition>> comparatorClass) {
        this.comparatorClass = comparatorClass;
    }

    public boolean isTextInputAllowed() {
        return textInputAllowed;
    }

    public void setTextInputAllowed(boolean textInputAllowed) {
        this.textInputAllowed = textInputAllowed;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

}
