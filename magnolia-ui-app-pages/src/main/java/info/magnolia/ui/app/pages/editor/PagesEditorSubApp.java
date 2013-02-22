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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentActionExecutor;
import info.magnolia.ui.admincentral.app.content.ContentSubAppDescriptor;
import info.magnolia.ui.admincentral.app.content.location.ItemLocation;
import info.magnolia.ui.admincentral.content.item.ItemView;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBusConfigurer;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.view.View;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp extends BaseSubApp implements PagesEditorSubAppView.Listener, ActionExecutor.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private ContentActionExecutor actionExecutor;
    private final PagesEditorSubAppView view;

    private final EventBus eventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    private WorkbenchDefinition workbenchDefinition;

    @Inject
    public PagesEditorSubApp(final SubAppContext subAppContext, final PagesEditorSubAppView view, final @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) EventBus eventBus, final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter) {
        super(subAppContext, view);
        this.view = view;
        this.view.setListener(this);
        this.eventBus = eventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.workbenchDefinition = ((ContentSubAppDescriptor) subAppContext.getSubAppDescriptor()).getWorkbench();

        bindHandlers();
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public View start(Location location) {
        ItemLocation itemLocation = ItemLocation.wrap(location);
        super.start(itemLocation);

        actionbarPresenter.setListener(this);
        ActionbarDefinition actionbarDefinition = workbenchDefinition.getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition);
        view.setActionbarView(actionbar);
        view.setPageEditorView(pageEditorPresenter.start());

        goToLocation(itemLocation);
        updateActions();
        return view;
    }

    private void updateActions() {
        // actions currently always disabled
        actionbarPresenter.disable("moveComponent", "copyComponent", "pasteComponent", "undo", "redo");
    }

    @Override
    public boolean supportsLocation(Location location) {
        return getCurrentLocation().getNodePath().equals(ItemLocation.wrap(location).getNodePath());
    }

    /**
     * Wraps the current DefaultLocation in a ContentLocation. Providing getter and setters for used parameters.
     */
    @Override
    public ItemLocation getCurrentLocation() {
        return ItemLocation.wrap(super.getCurrentLocation());
    }

    @Override
    public void locationChanged(Location location) {
        ItemLocation itemLocation = ItemLocation.wrap(location);
        super.locationChanged(itemLocation);
        goToLocation(itemLocation);
        updateActions();
    }

    private void goToLocation(ItemLocation location) {

        if (isLocationChanged(location)) {
            setPageEditorParameters(location);
            switch (location.getViewType()) {
            case VIEW:
                showPreview();
                break;
            case EDIT:
            default:
                showEditor();
                break;
            }
        }
    }

    private void setPageEditorParameters(ItemLocation location) {
        ItemView.ViewType action = location.getViewType();
        String path = location.getNodePath();

        this.parameters = new PageEditorParameters(MgnlContext.getContextPath(), path, ItemView.ViewType.VIEW.getText().equals(action.getText()));
        this.caption = getPageTitle(path);
    }

    private boolean isLocationChanged(ItemLocation location) {
        ItemView.ViewType action = location.getViewType();
        String path = location.getNodePath();

        if (parameters != null && (parameters.getNodePath().equals(path) && parameters.isPreview() == ItemView.ViewType.VIEW.getText().equals(action.getText()))) {
            return false;
        }
        return true;
    }

    private String getPageTitle(String path) {
        String caption = null;
        try {
            Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
            Node node = session.getNode(path);
            caption = node.getProperty("title").getString();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
        return caption;
    }

    private void hideAllSections() {
        actionbarPresenter.hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions");
    }

    private void showEditor() {
        hideAllSections();
        actionbarPresenter.showSection("pageActions");
        pageEditorPresenter.loadPageEditor(parameters);
    }

    private void showPreview() {
        hideAllSections();
        actionbarPresenter.showSection("pagePreviewActions");
        pageEditorPresenter.loadPageEditor(parameters);
    }

    private void bindHandlers() {

        eventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
/*                ActionDefinition actionDefinition = event.getActionDefinition();
                if (actionDefinition instanceof EditElementActionDefinition) {
                    pageEditorPresenter.editComponent(
                            ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath(),
                            pageEditorPresenter.getSelectedElement().getDialog());
                } else if (actionDefinition instanceof AddComponentActionDefinition) {
                    // casting to AreaElement, because this action is only defined for areas
                    pageEditorPresenter.newComponent(
                            ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath(),
                            ((AreaElement) pageEditorPresenter.getSelectedElement()).getAvailableComponents());
                } else if (actionDefinition instanceof DeleteItemActionDefinition) {
                    pageEditorPresenter.deleteComponent(((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(), pageEditorPresenter
                            .getSelectedElement()
                            .getPath());
                } else if (actionDefinition instanceof PreviewPageActionDefinition || actionDefinition instanceof EditPageActionDefinition) {
                    actionbarPresenter.createAndExecuteAction(
                            actionDefinition,
                            ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                            parameters.getNodePath());
                } else {
                    actionbarPresenter.createAndExecuteAction(
                            actionDefinition,
                            ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath());
                }*/

            }
        });

        eventBus.addHandler(NodeSelectedEvent.class, new NodeSelectedEvent.Handler() {

            @Override
            public void onItemSelected(NodeSelectedEvent event) {
                String workspace = event.getWorkspace();
                String path = event.getPath();
                String dialog = pageEditorPresenter.getSelectedElement().getDialog();

                try {
                    Session session = MgnlContext.getJCRSession(workspace);

                    if (path == null || !session.itemExists(path)) {
                        path = "/";
                    }
                    Node node = session.getNode(path);

                    hideAllSections();
                    if (node.isNodeType(NodeTypes.Page.NAME)) {
                        actionbarPresenter.showSection("pageActions");
                    } else if (node.isNodeType(NodeTypes.Area.NAME)) {
                        if (dialog == null) {
                            actionbarPresenter.showSection("areaActions");
                        } else {
                            actionbarPresenter.showSection("editableAreaActions");
                        }
                    } else if (node.isNodeType(NodeTypes.Component.NAME)) {
                        actionbarPresenter.showSection("componentActions");
                    }
                } catch (RepositoryException e) {
                    log.error("Exception caught: {}", e.getMessage(), e);
                }

                updateActions();
            }
        });
    }

    @Override
    public void onExecute(String actionName) {
        String workspace = ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace();

        if (actionName.equals("editElement")) {
            pageEditorPresenter.editComponent(
                    ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                    pageEditorPresenter.getSelectedElement().getPath(),
                    pageEditorPresenter.getSelectedElement().getDialog());
        }
        else if (actionName.equals("addComponent")) {
            pageEditorPresenter.editComponent(
                    ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(),
                    pageEditorPresenter.getSelectedElement().getPath(),
                    pageEditorPresenter.getSelectedElement().getDialog());
        }
        else if (actionName.equals("deleteItem")) {
            pageEditorPresenter.deleteComponent(((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench().getWorkspace(), pageEditorPresenter
                    .getSelectedElement()
                    .getPath());
        }

        else {
            try {
                actionExecutor.execute(actionName, workspace, parameters.getNodePath());
            } catch (ActionExecutionException e) {
                throw new RuntimeException("Could not execute the action: " + actionName, e);
            }
        }
    }
}
