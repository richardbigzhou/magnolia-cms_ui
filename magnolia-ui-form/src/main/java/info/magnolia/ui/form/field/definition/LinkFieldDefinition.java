/**
 * This file Copyright (c) 2011-2015 Magnolia International
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

import info.magnolia.ui.form.field.converter.IdentifierToPathConverter;

/**
 * The link field allows you to create a link to content stored in Magnolia. You can browse any specified workspace and
 * select a content node to link to such as a page (website), file (dms) or data item (data).
 */
public class LinkFieldDefinition extends ConfiguredFieldDefinition {

    private String targetTreeRootPath;
    private String appName;
    private String targetWorkspace = "website";
    private String buttonSelectNewLabel = "field.link.select.new";
    private String buttonSelectOtherLabel = "field.link.select.another";
    private IdentifierToPathConverter identifierToPathConverter;
    private ContentPreviewDefinition contentPreviewDefinition;
    private boolean fieldEditable = true;
    private String targetPropertyToPopulate;

    /**
     * @return the target App Name used to create the ContentView, like 'pages'
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @return the target property name to populate into the link field.
     */
    public String getTargetPropertyToPopulate() {
        return targetPropertyToPopulate;
    }

    /**
     * @return the workspace from which the link was retrieve.
     */
    public String getTargetWorkspace() {
        return targetWorkspace;
    }

    /**
     * If not define, no translation will be performed.
     * 
     * @return the implemented class used to perform the translation between a path and an Identifier.
     */
    public IdentifierToPathConverter getIdentifierToPathConverter() {
        return identifierToPathConverter;
    }

    /**
     * If not define, no Content preview component will added to the selection field.
     * 
     * @return the implemented class used to display the File preview.
     */
    public ContentPreviewDefinition getContentPreviewDefinition() {
        return contentPreviewDefinition;
    }

    /**
     * @return the root of the target tree.
     */
    public String getTargetTreeRootPath() {
        return this.targetTreeRootPath;
    }

    /**
     * @return the Button Label displayed when no link is yet selected.
     */
    public String getButtonSelectNewLabel() {
        return buttonSelectNewLabel;
    }

    /**
     * @return the Button Label displayed when a link is selected.
     */
    public String getButtonSelectOtherLabel() {
        return buttonSelectOtherLabel;
    }

    /**
     * @return if true, the select link field is editable, else it is defined as readOnly.
     */
    public boolean isFieldEditable() {
        return fieldEditable;
    }

    public void setTargetPropertyToPopulate(String targetPropertyToPopulate) {
        this.targetPropertyToPopulate = targetPropertyToPopulate;
    }

    public void setTargetWorkspace(String targetWorkspace) {
        this.targetWorkspace = targetWorkspace;
    }


    public void setTargetTreeRootPath(String targetTreeRootPath) {
        this.targetTreeRootPath = targetTreeRootPath;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setButtonSelectNewLabel(String buttonSelectNewLabel) {
        this.buttonSelectNewLabel = buttonSelectNewLabel;
    }

    public void setButtonSelectOtherLabel(String buttonSelectOtherLabel) {
        this.buttonSelectOtherLabel = buttonSelectOtherLabel;
    }

    public void setIdentifierToPathConverter(IdentifierToPathConverter identifierToPathConverter) {
        this.identifierToPathConverter = identifierToPathConverter;
    }

    public void setContentPreviewDefinition(ContentPreviewDefinition contentPreviewDefinition) {
        this.contentPreviewDefinition = contentPreviewDefinition;
    }

    public void setFieldEditable(boolean fieldEditable) {
        this.fieldEditable = fieldEditable;
    }
}
