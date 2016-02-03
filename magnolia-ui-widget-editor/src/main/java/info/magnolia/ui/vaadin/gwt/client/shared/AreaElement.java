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
package info.magnolia.ui.vaadin.gwt.client.shared;

/**
 * A slimmed down representation of a {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea}.
 * Used for communication between server and client.
 */
public class AreaElement extends AbstractElement {

    private String availableComponents;
    private Boolean addible = false; // add button operation permission

    private ComponentElement sourceComponent;
    private ComponentElement targetComponent;
    private String sortOrder;
    private boolean created;
    private boolean optional;

    public AreaElement() {
    }

    public AreaElement(String workspace, String path, String dialog, String availableComponents) {
        super(workspace, path, dialog);
        this.availableComponents = availableComponents;
    }

    public String getAvailableComponents() {
        return availableComponents;
    }

    public void setAvailableComponents(String availableComponents) {
        this.availableComponents = availableComponents;
    }

    public Boolean getAddible() {
        return addible;
    }

    public void setAddible(Boolean addible) {
        this.addible = addible;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public ComponentElement getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(ComponentElement sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public ComponentElement getTargetComponent() {
        return targetComponent;
    }

    public void setTargetComponent(ComponentElement targetComponent) {
        this.targetComponent = targetComponent;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
