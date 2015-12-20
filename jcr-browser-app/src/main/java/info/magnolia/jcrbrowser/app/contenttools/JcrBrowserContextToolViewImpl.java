/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.jcrbrowser.app.contenttools;

import info.magnolia.i18nsystem.SimpleTranslator;

import javax.inject.Inject;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Simple Vaadin {@link JcrBrowserContextToolView} implementation which is controlled by {@link JcrBrowserContextTool}.
 */
@StyleSheet("vaadin://jcr-browser-context-tool.css")
public class JcrBrowserContextToolViewImpl extends HorizontalLayout implements JcrBrowserContextToolView {

    private final ComboBox workspaceSelector;
    private final CheckBox includeSystemPropertiesCheckbox;
    private final Label workspaceSelectorCaption;

    @Inject
    public JcrBrowserContextToolViewImpl(SimpleTranslator i18n) {
        addStyleName("jcr-browser-context-tool");

        setDefaultComponentAlignment(Alignment.TOP_LEFT);
        setSpacing(true);

        workspaceSelector = new ComboBox(null);
        workspaceSelector.addStyleName("workspace-selector");
        workspaceSelector.setImmediate(true);
        workspaceSelector.setNullSelectionAllowed(false);

        workspaceSelectorCaption = new Label(i18n.translate("jcr-browser.tools.browsing-context.selector.label"));
        workspaceSelectorCaption.addStyleName("workspace-selector-label");
        workspaceSelectorCaption.setSizeUndefined();

        addComponent(workspaceSelectorCaption);
        addComponent(workspaceSelector);

        includeSystemPropertiesCheckbox = new CheckBox(i18n.translate("jcr-browser.tools.browsing-context.system-properties-toggle.label"));
        addComponent(includeSystemPropertiesCheckbox);
    }

    @Override
    public void setEnabled(boolean enabled) {
        workspaceSelectorCaption.setEnabled(enabled);
        workspaceSelector.setEnabled(enabled);
        includeSystemPropertiesCheckbox.setEnabled(enabled);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setWorkspaceOptions(Container options) {
        workspaceSelector.setContainerDataSource(options);
    }

    @Override
    public void setSystemPropertiesInclusionProperty(Property<Boolean> systemPropertiesInclusionProperty) {
        includeSystemPropertiesCheckbox.setPropertyDataSource(systemPropertiesInclusionProperty);
    }

    @Override
    public void setWorkspaceNameProperty(Property<String> workspaceNameProperty) {
        workspaceSelector.setPropertyDataSource(workspaceNameProperty);
    }
}
