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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.event.ActionbarClickEvent;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.app.pages.PagesAppDescriptor;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp implements SubApp, PagesEditorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private final PagesEditorView view;

    private final EventBus appEventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    private PagesAppDescriptor appDescriptor;

    private WorkbenchActionFactory actionFactory;

    @Inject
    public PagesEditorSubApp(final AppContext ctx, final PagesEditorView view, final @Named("app") EventBus appEventBus, final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, final WorkbenchActionFactory actionFactory) {
        this.view = view;
        this.appEventBus = appEventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appDescriptor = (PagesAppDescriptor)ctx.getAppDescriptor();
        this.actionFactory = actionFactory;

        bindHandlers();
    }

    private void bindHandlers() {

        appEventBus.addHandler(ActionbarClickEvent.class, new ActionbarClickEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarClickEvent event) {
                try {
                    ActionDefinition actionDefinition = event.getActionDefinition();
                    actionbarPresenter.createAndExecuteAction(actionDefinition, appDescriptor.getWorkbench().getWorkspace(), parameters.getNodePath());
                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
            }
        });

        appEventBus.addHandler(ComponentSelectedEvent.class, new ComponentSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ComponentSelectedEvent event) {
                // TODO 20120730 mgeljic, review whether presenter should be a proxy for every
                // single actionbar widget feature
                if (event.getPath() != null) {
                    actionbarPresenter.hideSection("Pages");
                    actionbarPresenter.hideSection("Areas");
                    actionbarPresenter.showSection("Components");
                }
            }
        });
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setParameters(PageEditorParameters parameters) {
        this.parameters = parameters;
        this.caption = parameters.getNodePath();
    }

    @Override
    public View start() {
        view.setListener(this);
        pageEditorPresenter.setParameters(parameters);
        view.setPageEditor(pageEditorPresenter.start());
        ActionbarDefinition actionbarDefinition = appDescriptor.getEditor().getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition, actionFactory);
        actionbarPresenter.hideSection("Areas");
        actionbarPresenter.hideSection("Components");
        actionbarPresenter.showSection("Pages");

        view.setActionbarView(actionbar);

        return view;
    }

}
