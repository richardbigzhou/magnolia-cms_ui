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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.pages.PagesAppDescriptor;
import info.magnolia.ui.app.pages.action.AddComponentActionDefinition;
import info.magnolia.ui.app.pages.action.EditElementActionDefinition;
import info.magnolia.ui.app.pages.action.EditPageActionDefinition;
import info.magnolia.ui.app.pages.action.PreviewPageActionDefinition;
import info.magnolia.ui.app.pages.editor.location.PagesLocation;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.editor.PageEditor;
import info.magnolia.ui.vaadin.editor.PageEditorParameters;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp extends AbstractSubApp implements PagesEditorSubAppView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private final PagesEditorSubAppView view;

    private final EventBus eventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    private final WorkbenchActionFactory actionFactory;

    @Inject
    public PagesEditorSubApp(final SubAppContext subAppContext, final PagesEditorSubAppView view, final @Named("subapp") EventBus eventBus,
        final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, final WorkbenchActionFactory actionFactory) {
        super(subAppContext, view);

        this.view = view;
        this.view.setListener(this);
        this.eventBus = eventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.actionFactory = actionFactory;

        bindHandlers();
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public View start(Location location) {
        PagesLocation pagesLocation = PagesLocation.wrap(location);
        super.start(pagesLocation);

        ActionbarDefinition actionbarDefinition = ((PagesAppDescriptor) getAppContext().getAppDescriptor()).getEditor().getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition, actionFactory);
        view.setActionbarView(actionbar);
        view.setPageEditorView(pageEditorPresenter.start());

        goToLocation(pagesLocation);
        updateActions();
        return view;
    }

    private void updateActions() {

        // actions currently always disabled
        actionbarPresenter.disable("moveComponent");
        actionbarPresenter.disable("copyComponent");
        actionbarPresenter.disable("pasteComponent");
        actionbarPresenter.disable("undo");
        actionbarPresenter.disable("redo");

    }

    @Override
    public boolean supportsLocation(Location location) {
        return getCurrentLocation().getNodePath().equals(PagesLocation.wrap(location).getNodePath());
    }

    @Override
    protected PagesLocation getCurrentLocation() {
        return PagesLocation.wrap(currentLocation);
    }

    @Override
    public void locationChanged(Location location) {
        PagesLocation pagesLocation = PagesLocation.wrap(location);
        super.locationChanged(pagesLocation);
        goToLocation(pagesLocation);
        updateActions();
    }

    private void goToLocation(PagesLocation location) {

        if (isLocationChanged(location)) {
            setPageEditorParameters(location);
            if (parameters.isPreview()) {
                showPreview();
            } else {
                showEditor();
            }
        }
    }

    private void setPageEditorParameters(PagesLocation location) {
        boolean isPreview = location.getMode().equals("preview");
        String path = location.getNodePath();

        this.parameters = new PageEditorParameters(MgnlContext.getContextPath(), path, isPreview);
        this.caption = getPageTitle(path);
    }

    private boolean isLocationChanged(PagesLocation location) {
        boolean isPreview = location.getMode().equals("preview");
        String path = location.getNodePath();

        if (parameters != null && (parameters.getNodePath().equals(path) && parameters.isPreview() == isPreview)) {
            return false;
        }
        return true;
    }

    private String getPageTitle(String path) {
        String caption = null;
        try {
            Session session = MgnlContext.getJCRSession(((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace());
            Node node = session.getNode(path);
            caption = node.getProperty("title").getString();
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
        return caption;
    }

    private void hideAllSections() {
        actionbarPresenter.hideSection("pagePreviewActions");
        actionbarPresenter.hideSection("pageActions");
        actionbarPresenter.hideSection("areaActions");
        actionbarPresenter.hideSection("optionalAreaActions");
        actionbarPresenter.hideSection("editableAreaActions");
        actionbarPresenter.hideSection("optionalEditableAreaActions");
        actionbarPresenter.hideSection("componentActions");
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
                try {
                    ActionDefinition actionDefinition = event.getActionDefinition();
                    if (actionDefinition instanceof EditElementActionDefinition) {
                        pageEditorPresenter.editComponent(
                                ((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath(),
                            pageEditorPresenter.getSelectedElement().getDialog());
                    } else if (actionDefinition instanceof AddComponentActionDefinition) {
                        // casting to AreaElement, because this action is only defined for areas
                        pageEditorPresenter.newComponent(
                                ((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath(),
                            ((PageEditor.AreaElement) pageEditorPresenter.getSelectedElement()).getAvailableComponents());
                    } else if (actionDefinition instanceof DeleteItemActionDefinition) {
                        pageEditorPresenter.deleteComponent(((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace(), pageEditorPresenter
                            .getSelectedElement()
                            .getPath());
                    } else if (actionDefinition instanceof PreviewPageActionDefinition || actionDefinition instanceof EditPageActionDefinition) {
                        actionbarPresenter.createAndExecuteAction(
                            actionDefinition,
                                ((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace(),
                            parameters.getNodePath());
                    } else {
                        actionbarPresenter.createAndExecuteAction(
                            actionDefinition,
                                ((PagesAppDescriptor) getAppContext().getAppDescriptor()).getWorkbench().getWorkspace(),
                            pageEditorPresenter.getSelectedElement().getPath());
                    }

                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
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
                    if (node.isNodeType(MgnlNodeType.NT_PAGE)) {
                        actionbarPresenter.showSection("pageActions");
                    } else if (node.isNodeType(MgnlNodeType.NT_AREA)) {
                        if (dialog == null) {
                            actionbarPresenter.showSection("areaActions");
                        } else {
                            actionbarPresenter.showSection("editableAreaActions");
                        }
                    } else if (node.isNodeType(MgnlNodeType.NT_COMPONENT)) {
                        actionbarPresenter.showSection("componentActions");
                    }
                } catch (RepositoryException e) {
                    log.error("Exception caught: {}", e.getMessage(), e);
                }

                updateActions();
            }
        });
    }


    public static DefaultLocation createLocation(String editorPath, String previewToken) {
        String token = editorPath ;
        if (StringUtils.isNotEmpty(previewToken)) {
            token += ":" + previewToken;
        }

        return new PagesLocation(token);
    }

}
