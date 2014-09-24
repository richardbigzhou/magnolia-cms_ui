/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.extension;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.pages.app.editor.extension.definition.ExtensionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@link ExtensionFactory}.
 */
public class DefaultExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultExtensionFactory.class);
    private final ComponentProvider componentProvider;

    @Inject
    public DefaultExtensionFactory(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    /**
     * Creates additional {@link Extension}s loaded into the page editor.
     * The order of loading is the same as declared at <code>/modules/pages/apps/pages/subApps/detail/{pageBar|statusBar}</code>.
     */
    @Override
    public List<Extension> createExtensions(Map<String, ExtensionDefinition> extensionDefinitions) {
        List<Extension> extensions = new ArrayList<Extension>();
        for (Map.Entry<String, ExtensionDefinition> extensionDefinition : extensionDefinitions.entrySet()) {

            log.debug("Loading extension {}", extensionDefinition.getKey());

            Class<? extends Extension> clazz = extensionDefinition.getValue().getExtensionClass();
            if (clazz == null) {
                log.error("No class extension configured for {}. Please, check your configuration at /modules/pages/apps/pages/subApps/detail/{pageBar/statusBar}/extensions", extensionDefinition.getKey());
                continue;
            }
            Extension extension = componentProvider.newInstance(clazz, extensionDefinition);
            extensions.add(extension);
        }
        return extensions;
    }

}
