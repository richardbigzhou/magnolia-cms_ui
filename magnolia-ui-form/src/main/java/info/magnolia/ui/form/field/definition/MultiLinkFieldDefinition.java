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
package info.magnolia.ui.form.field.definition;



/**
 * Field definition for a Multi-Link field.
 */
public class MultiLinkFieldDefinition extends LinkFieldDefinition {

    private SaveModeType saveModeType;
    private String buttonSelectAddLabel;

    /**
     * @return i18n property used to configure Button Label.
     */
    public String getButtonSelectAddLabel() {
        return buttonSelectAddLabel;
    }

    /**
     * @return SaveModeType defining the specific {@link info.magnolia.ui.form.field.property.MultiValueHandler} used to retrieve or store the values in various format (single property, multivalue property or sub nodes).
     */
    public SaveModeType getSaveModeType() {
        return saveModeType;
    }

    public void setButtonSelectAddLabel(String buttonSelectAddLabel) {
        this.buttonSelectAddLabel = buttonSelectAddLabel;
    }

    public void setSaveModeType(SaveModeType saveModeType) {
        this.saveModeType = saveModeType;
    }
}
