/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.api.app.registry;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation for {@link SubAppDescriptor}.
 */
public class ConfiguredSubAppDescriptor implements SubAppDescriptor {

    /**
     * unique identifier.
     */
    private String name;

    private String label;

    private boolean closable = true;

    private String icon;

    private Class<? extends SubApp> subAppClass;

    private Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    @Override
    public String getIcon() {
        return icon;
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

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setSubAppClass(Class<? extends SubApp> subAppClass) {
        this.subAppClass = subAppClass;
    }

    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    public void setActions(Map<String, ActionDefinition> actions) {
        this.actions = actions;
    }
}
