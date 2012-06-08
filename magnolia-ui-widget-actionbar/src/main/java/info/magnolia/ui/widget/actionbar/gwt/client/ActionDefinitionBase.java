/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.widget.actionbar.gwt.client;

/**
 * The Class ActionDefinitionBase for dummy tests.
 */
public class ActionDefinitionBase {

    private String i18nBasename;

    private String name;

    private String label;

    private String icon;

    private String description;

    private String sectionTitle;

    private String groupName;

    /**
     * Sets the label.
     * 
     * @param label the new label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the label.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the icon.
     * 
     * @param icon the new icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Gets the icon.
     * 
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gets the i18n basename.
     * 
     * @return the i18n basename
     */
    public String getI18nBasename() {
        return i18nBasename;
    }

    /**
     * Sets the i18n basename.
     * 
     * @param i18nBasename the new i18n basename
     */
    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    /**
     * Sets the description.
     * 
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the section title.
     * 
     * @return the section title
     */
    public String getSectionTitle() {
        return sectionTitle;
    }

    /**
     * Sets the section title.
     * 
     * @param sectionTitle the new section title
     */
    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    /**
     * Gets the group name.
     * 
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the group name.
     * 
     * @param groupName the new group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
