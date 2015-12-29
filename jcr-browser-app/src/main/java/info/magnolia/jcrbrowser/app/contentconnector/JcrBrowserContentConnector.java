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
package info.magnolia.jcrbrowser.app.contentconnector;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinitionWrapper;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Simple extension of {@link JcrContentConnector}. The main differences from the parent class include:
 * <ul>
 * <li>{@link JcrContentConnectorDefinition} is substituted with a {@link JcrBrowserContentConnectorDefinition} in order to be able to modify the definition
 * on the fly without affecting an original singleton definition</li>
 * <li>{@link JcrBrowserContentConnectorDefinition#getDefaultItemId()} doesn't cache the value like the parent class - it is always re-resolved.
 * </ul>
 */
public class JcrBrowserContentConnector extends JcrContentConnector {

    private static final String URL_FRAGMENT_COLON_ESCAPE_SEQUENCE = "---";

    public static final String SYSTEM_PROPERTY_URL_FRAGMENT_TEMPLATE = ".+@(mgnl|jcr)%s.+$";

    private static final Pattern escapedSystemPropertyPattern = Pattern.compile(String.format(SYSTEM_PROPERTY_URL_FRAGMENT_TEMPLATE, URL_FRAGMENT_COLON_ESCAPE_SEQUENCE));

    private static final Pattern unEscapedSystemPropertyPattern = Pattern.compile(String.format(SYSTEM_PROPERTY_URL_FRAGMENT_TEMPLATE, ":"));

    @Inject
    public JcrBrowserContentConnector(VersionManager versionManager, JcrContentConnectorDefinition definition) {
        super(versionManager, new JcrBrowserContentConnectorDefinition(definition));
    }

    @Override
    public JcrItemId getItemIdByUrlFragment(String urlFragment) {
        if (isPropertyItemId(urlFragment) && escapedSystemPropertyPattern.matcher(urlFragment).matches()) {
            urlFragment = urlFragment.replaceFirst(URL_FRAGMENT_COLON_ESCAPE_SEQUENCE, ":");
        }
        return super.getItemIdByUrlFragment(urlFragment);
    }

    @Override
    public String getItemUrlFragment(Object itemId) {
        String urlFragment = super.getItemUrlFragment(itemId);
        if (urlFragment != null && itemId instanceof JcrPropertyItemId) {
            if (unEscapedSystemPropertyPattern.matcher(urlFragment).matches()) {
                return urlFragment.replaceFirst(":", URL_FRAGMENT_COLON_ESCAPE_SEQUENCE);
            }
        }
        return urlFragment;
    }

    @Override
    public JcrBrowserContentConnectorDefinition getContentConnectorDefinition() {
        return (JcrBrowserContentConnectorDefinition) super.getContentConnectorDefinition();
    }

    /**
     * Simple wrapper around a {@link JcrBrowserContentConnectorDefinition}, allows for overriding the {@code workspace} property.
     */
    public static class JcrBrowserContentConnectorDefinition extends JcrContentConnectorDefinitionWrapper {

        private String workspace = null;

        public JcrBrowserContentConnectorDefinition(JcrContentConnectorDefinition delegate) {
            super(delegate);
        }

        @Override
        public String getWorkspace() {
            return workspace == null ? super.getWorkspace() : workspace;
        }

        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }

    }
}
