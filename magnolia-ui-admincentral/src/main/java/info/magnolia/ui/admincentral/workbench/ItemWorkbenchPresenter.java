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
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentSubAppDescriptor;
import info.magnolia.ui.admincentral.content.item.ItemPresenter;
import info.magnolia.ui.admincentral.content.item.ItemView;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBusConfigurer;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.view.View;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the workbench displayed in the {@link info.magnolia.ui.admincentral.app.content.ItemSubApp}.
 * Contains the {@link ActionbarPresenter} for handling action events and the {@link ItemPresenter} for displaying the actual item.
 */
public class ItemWorkbenchPresenter implements ItemWorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ItemWorkbenchPresenter.class);

    private final ItemWorkbenchView view;
    private final EventBus subAppEventBus;
    private final ItemPresenter itemPresenter;
    private final ActionbarPresenter actionbarPresenter;
    private final WorkbenchDefinition workbenchDefinition;
    private String nodePath;

    @Inject
    public ItemWorkbenchPresenter(final SubAppContext subAppContext, final ItemWorkbenchView view, final @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) EventBus subAppEventBus, final ItemPresenter itemPresenter, final ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.itemPresenter = itemPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.workbenchDefinition = ((ContentSubAppDescriptor) subAppContext.getSubAppDescriptor()).getWorkbench();

    }

    public View start(String nodePath, ItemView.ViewType viewType) {
        view.setListener(this);
        this.nodePath = nodePath;
        final JcrNodeAdapter item = new JcrNodeAdapter(SessionUtil.getNode(workbenchDefinition.getWorkspace(), nodePath));
        ItemView itemView = itemPresenter.start(workbenchDefinition.getFormDefinition(), item, viewType);

        view.setItemView(itemView);

        ActionbarView actionbar = actionbarPresenter.start(workbenchDefinition.getActionbar());
        view.setActionbarView(actionbar);

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        subAppEventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
                final ActionDefinition actionDefinition = event.getActionDefinition();
                actionbarPresenter.createAndExecuteAction(actionDefinition, workbenchDefinition.getWorkspace(), nodePath);

            }
        });
    }

    public String getNodePath() {
        return nodePath;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    @Override
    public void onViewTypeChanged(final ItemView.ViewType viewType) {
        // eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

}
