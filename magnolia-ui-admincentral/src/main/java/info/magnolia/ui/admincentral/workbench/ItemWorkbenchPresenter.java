/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentSubAppDescriptor;
import info.magnolia.ui.admincentral.content.item.ItemPresenter;
import info.magnolia.ui.admincentral.content.item.ItemView;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.FormPresenterFactory;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * ItemWorkbenchPresenter.
 */
public class ItemWorkbenchPresenter implements ItemWorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ItemWorkbenchPresenter.class);

    private final SubAppContext subAppContext;
    private final ItemWorkbenchView view;
    private final EventBus eventBus;
    private EventBus subAppEventBus;
    private final ItemPresenter itemPresenter;
    private WorkbenchActionFactory actionFactory;
    private final ActionbarPresenter actionbarPresenter;
    private FormPresenterFactory formPresenterFactory;
    private WorkbenchDefinition workbenchDefinition;
    private String nodePath;

    @Inject
    public ItemWorkbenchPresenter(final SubAppContext subAppContext, final ItemWorkbenchView view, final @Named("admincentral") EventBus eventBus, final @Named("subapp") EventBus subAppEventBus,
                                  final ItemPresenter itemPresenter, final WorkbenchActionFactory actionFactory, final ActionbarPresenter actionbarPresenter, final FormPresenterFactory formPresenterFactory) {
        this.subAppContext = subAppContext;
        this.view = view;
        this.eventBus = eventBus;
        this.subAppEventBus = subAppEventBus;
        this.itemPresenter = itemPresenter;
        this.actionFactory = actionFactory;
        this.actionbarPresenter = actionbarPresenter;
        this.formPresenterFactory = formPresenterFactory;
        this.workbenchDefinition = ((ContentSubAppDescriptor) subAppContext.getSubAppDescriptor()).getWorkbench();

    }

    public View start(String nodePath) {
        view.setListener(this);
        //final FormPresenter formPresenter = formPresenterFactory.createFormPresenterByName(workbenchDefinition.getFormName());
        final FormPresenter formPresenter = formPresenterFactory.createFormPresenterByDefinition(workbenchDefinition.getFormDefinition());
        this.nodePath = nodePath;
        final JcrNodeAdapter item = new JcrNodeAdapter(SessionUtil.getNode(workbenchDefinition.getWorkspace(), nodePath));
        final FormView formView = formPresenter.start(item, new FormPresenter.Callback() {

            @Override
            public void onCancel() {
                // should switch to ViewType.VIEW
            }

            @Override
            public void onSuccess(String actionName) {
                eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
            }
        });

        view.setItemView(formView);

        ActionbarView actionbar = actionbarPresenter.start(workbenchDefinition.getActionbar(), actionFactory);
        view.setActionbarView(actionbar);

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        subAppEventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
                try {
                    final ActionDefinition actionDefinition = event.getActionDefinition();
                    actionbarPresenter.createAndExecuteAction(actionDefinition, workbenchDefinition.getWorkspace(), nodePath);
                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
            }
        });
    }

    @Override
    public void onViewTypeChanged(final ItemView.ViewType viewType) {
        //eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }
}
