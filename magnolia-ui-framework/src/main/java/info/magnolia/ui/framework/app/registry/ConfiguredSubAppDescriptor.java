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
package info.magnolia.ui.framework.app.registry;

import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * ConfiguredSubAppDescriptor.
 */
public class ConfiguredSubAppDescriptor implements SubAppDescriptor {

    /**
     * unique identifier.
     */
    private String name;

    private String label;

    private boolean enabled = true;

    private String icon;

    private Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

    private ActionbarDefinition actionbar;

    private ImageProviderDefinition imageProvider;

    private Class<? extends SubApp> subAppClass;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public ActionbarDefinition getActionbar() {
        return actionbar;
    }

    @Override
    public ImageProviderDefinition getImageProvider() {
        return imageProvider;
    }

    @Override
    public Class<? extends SubApp> getSubAppClass() {
        return subAppClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setActions(Map<String, ActionDefinition> actions) {
        this.actions = actions;
    }

    public void setActionbar(ActionbarDefinition actionbar) {
        this.actionbar = actionbar;
    }

    public void setImageProvider(ImageProviderDefinition imageProvider) {
        this.imageProvider = imageProvider;
    }

    public void setSubAppClass(Class<? extends SubApp> subAppClass) {
        this.subAppClass = subAppClass;
    }
}
