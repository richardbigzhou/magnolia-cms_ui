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
package info.magnolia.pages.app.action;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

/**
 * Action for sorting components inside an area.
 */
public class SortComponentAction extends AbstractAction<SortComponentActionDefinition> {

    private AreaElement areaElement;
    private EventBus eventBus;

    @Inject
    public SortComponentAction(SortComponentActionDefinition definition, AreaElement areaElement,  @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.areaElement = areaElement;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            String order = "before";
            if (StringUtils.isNotBlank(areaElement.getSortOrder())) {
                order = areaElement.getSortOrder();
            }

            Session session = MgnlContext.getJCRSession(areaElement.getWorkspace());

            Node parent = session.getNode(areaElement.getPath());
            Node sourceComponent = session.getNode(areaElement.getSourceComponent().getPath());
            Node targetComponent = session.getNode(areaElement.getTargetComponent().getPath());

            if ("before".equals(order)) {
                NodeUtil.orderBefore(sourceComponent, targetComponent.getName());
            } else {
                NodeUtil.orderAfter(sourceComponent, targetComponent.getName());
            }

            NodeTypes.LastModified.update(parent);
            session.save();
            eventBus.fireEvent(new ContentChangedEvent(areaElement.getWorkspace(), areaElement.getPath()));
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }

    }
}
