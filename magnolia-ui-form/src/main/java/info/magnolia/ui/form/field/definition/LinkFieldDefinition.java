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
package info.magnolia.ui.form.field.definition;

/**
 * The link field allows you to create a link to content stored in Magnolia. You can browse any specified workspace and
 * select a content node to link to such as a page (website), file (dms) or data item (data).
 */
public class LinkFieldDefinition extends ConfiguredFieldDefinition {

    // Define the target link workspace.
    private String workspace = "website";
    // Sub Dialog Name. Used by the LinkField to create the
    // sub dialog. like 'ui-admincentral:link'
    private String dialogName;
    // Target App Name used to create the ContentView.
    // 'pages'
    private String appName;
    // Button Label displayed when no link is yet selected.
    private String buttonSelectNewLabel = "field.link.select.new";
    // Button Label displayed when a link is selected.
    private String buttonSelectOtherLabel = "field.link.select.another";
    // Define if we should store the Identifier of the selected Item
    private boolean identifier = false;
    // Name of the list property to populate.
    // If empty, and identifier is false, populate the Node path
    // otherwise, populate the Identifier.
    private String propertyName;

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public boolean isIdentifier() {
        return identifier;
    }

    public void setIdentifier(boolean identifier) {
        this.identifier = identifier;
    }

    public String getDialogName() {
        return this.dialogName;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getButtonSelectNewLabel() {
        return buttonSelectNewLabel;
    }

    public void setButtonSelectNewLabel(String buttonSelectNewLabel) {
        this.buttonSelectNewLabel = buttonSelectNewLabel;
    }

    public String getButtonSelectOtherLabel() {
        return buttonSelectOtherLabel;
    }

    public void setButtonSelectOtherLabel(String buttonSelectOtherLabel) {
        this.buttonSelectOtherLabel = buttonSelectOtherLabel;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
