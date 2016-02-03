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
package info.magnolia.ui.contentapp.contentconnector;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.DefaultContentConnector;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides {@link info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector} to the sub-apps.
 */
@Singleton
public class ContentConnectorProvider implements Provider<ContentConnector> {

    private static final Logger log = LoggerFactory.getLogger(ContentConnectorProvider.class);

    private ComponentProvider provider;

    private EventBus subAppEventBus;

    private ContentConnector contentConnector;

    private SubAppContext ctx;

    @Inject
    public ContentConnectorProvider(SubAppContext ctx, final ComponentProvider provider, @Named(SubAppEventBus.NAME) EventBus subAppEventBus) {
        this.ctx = ctx;
        this.provider = provider;
        this.subAppEventBus = subAppEventBus;
    }

    @Override
    public ContentConnector get() {
        if (contentConnector == null) {
            SubAppDescriptor subAppDescriptor = ctx.getSubAppDescriptor();
            if (subAppDescriptor instanceof ContentSubAppDescriptor) {
                ContentConnectorDefinition contentConnectorDefinition = ((ContentSubAppDescriptor) subAppDescriptor).getContentConnector();
                if (contentConnectorDefinition != null) {
                    contentConnector = provider.newInstance(contentConnectorDefinition.getImplementationClass(), ctx, subAppEventBus, contentConnectorDefinition);
                } else {
                    log.warn("Sub-app descriptor {} ({}) expected a ContentConnectorDefinition, but no contentConnector is currently configured.", subAppDescriptor.getName(), subAppDescriptor.getClass().getSimpleName());
                }
            }
        }
        if (contentConnector == null) {
            contentConnector = new DefaultContentConnector(); // Null implementation
        }
        return contentConnector;
    }
}
