/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.jcrbrowser.app.contentviews;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcrbrowser.app.SystemPropertiesVisibilityToggledEvent;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.framework.availability.IsNotSystemProperty;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.TreePresenter;
import info.magnolia.ui.workbench.tree.TreeView;

import java.util.Collections;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property;

/**
 * {@link TreePresenter} extension which is capable of listening to the {@link SystemPropertiesVisibilityToggledEvent} and
 * then altering the underlying {@link HierarchicalJcrContainer} state via {@link HierarchicalJcrContainer#setIncludeSystemProperties(boolean)}.
 * <p>
 * Besides {@link JcrBrowserTreePresenter} prevents system properties modifications from being persisted by means of {@link IsNotSystemProperty} rule.
 * </p>
 */
public class JcrBrowserTreePresenter extends TreePresenter {

    private static final Logger log = LoggerFactory.getLogger(JcrBrowserTreePresenter.class);

    private final SubAppContext ctx;
    private final SimpleTranslator i18n;

    private final IsNotSystemProperty propertyEditingRestrictionRule = new IsNotSystemProperty();

    @Inject
    public JcrBrowserTreePresenter(TreeView view, ComponentProvider componentProvider, SubAppContext ctx, SimpleTranslator i18n) {
        super(view, componentProvider);
        this.ctx = ctx;
        this.i18n = i18n;
    }

    @Override
    public TreeView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName, ContentConnector contentConnector) {
        eventBus.addHandler(SystemPropertiesVisibilityToggledEvent.class, new SystemPropertiesVisibilityToggledEvent.Handler() {
            @Override
            public void onSystemPropertiesVisibilityToggled(SystemPropertiesVisibilityToggledEvent event) {
                if (container instanceof HierarchicalJcrContainer) {
                    ((HierarchicalJcrContainer)container).setIncludeSystemProperties(event.isDisplaySystemProperties());
                }
            }
        });

        return super.start(workbenchDefinition, eventBus, viewTypeName, contentConnector);
    }

    @Override
    public void onItemEdited(Object itemId, Object propertyId, Property<?> propertyDataSource) {
        if (!(itemId instanceof JcrPropertyItemId) || propertyEditingRestrictionRule.isAvailable(Collections.singletonList(itemId))) {
            super.onItemEdited(itemId, propertyId, propertyDataSource);
        } else {
            ctx.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("jcr-browser.inline.editing.restriction.message"));
        }
    }

    @Override
    protected Container.Hierarchical createContainer() {
        Container.Hierarchical container = super.createContainer();
        if (!(container instanceof HierarchicalJcrContainer)) {
            String warningStatement = String.format(
                    "JcrBrowserTreePresenter of [%s/%s] is supplied not with HierarchicalJcrContainer but with [%s], the content view will react on the JCR browser context changes accordingly",
                    ctx.getAppContext().getName(), ctx.getSubAppDescriptor().getName(), container.getClass().getName());
            log.warn(warningStatement);
        }
        return container;
    }
}
