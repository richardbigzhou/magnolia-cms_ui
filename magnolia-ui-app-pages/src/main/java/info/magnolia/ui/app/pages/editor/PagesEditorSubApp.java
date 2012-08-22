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


import static info.magnolia.ui.app.pages.PagesApp.PREVIEW_FULL_TOKEN;
import static info.magnolia.ui.app.pages.PagesApp.PREVIEW_TOKEN;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.app.pages.PagesApp;
import info.magnolia.ui.app.pages.PagesAppDescriptor;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class PagesEditorSubApp extends AbstractSubApp implements PagesEditorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private final PagesEditorView view;

    private final EventBus appEventBus;

    private final EventBus subAppEventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    private final PagesAppDescriptor appDescriptor;

    private final WorkbenchActionFactory actionFactory;

    private boolean fullPreview;

    private boolean preview;

    private AppContext appContext;

    private boolean editorPreview;

    @Inject
    public PagesEditorSubApp(final AppContext appContext, final PagesEditorView view, final @Named("app") EventBus appEventBus, final @Named("subapp") EventBus subAppEventBus,
            final PageEditorPresenter pageEditorPresenter, final LocationController locationController, final ActionbarPresenter actionbarPresenter, final WorkbenchActionFactory actionFactory) {

        final String token = DefaultLocation.extractToken(locationController.getWhere().toString());

        this.preview =  token.contains(PagesApp.PREVIEW_FULL_TOKEN) || token.contains(PagesApp.PREVIEW_TOKEN);
        this.fullPreview = isPreview() && token.contains(PagesApp.PREVIEW_FULL_TOKEN);
        this.editorPreview = isPreview() && !token.contains(PagesApp.PREVIEW_FULL_TOKEN);

        if(isPreview()) {
            log.debug("Preview type detected is {}", isFullPreview() ? "full preview" : "editor preview");
        }
        this.view = view;
        this.view.setListener(this);

        this.appEventBus = appEventBus;
        this.subAppEventBus = subAppEventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = appContext;
        this.appDescriptor = (PagesAppDescriptor)appContext.getAppDescriptor();
        this.actionFactory = actionFactory;

        bindHandlers();
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
    public View start(Location location) {
        if (isFullPreview()) {
            return view;
        }

        String path = getEditorPath(location);
        if (path == null)
            path = "/";

        setParameters(new PageEditorParameters(MgnlContext.getContextPath(), path));

        ActionbarDefinition actionbarDefinition = appDescriptor.getEditor().getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition, actionFactory);
        view.setActionbarView(actionbar);

        if (isEdit()) {
            showEditor();
        } else if (isEditorPreview()) {
            showEditorPreview();
        }

        return view;
    }


    @Override
    public void locationChanged(Location location) {
        DefaultLocation defaultLocation = (DefaultLocation) location;

        List<String> pathParams = parsePathParamsFromToken(defaultLocation.getToken());

        String previewMode = "";
        if(pathParams.size() == 3) {
            previewMode = pathParams.get(2);
        }

        if (PREVIEW_TOKEN.equals(previewMode)) {
            showEditorPreview();
        } else if(PREVIEW_FULL_TOKEN.equals(previewMode)) {
            showFullPreview(defaultLocation);
        } else {
            showEditor();
        }
    }

    /**
     * @return <code>true</code> if we are in preview mode, either editor preview or fullscreen preview.
     * <code>false</code> otherwise.
     */
    public boolean isPreview() {
        return preview;
    }

    public boolean isEdit() {
        return !preview;
    }

    public boolean isFullPreview() {
        return fullPreview;
    }

    public boolean isEditorPreview() {
        return editorPreview;
    }

    private void hideAllActions(final ActionbarPresenter presenter) {
        presenter.hideSection("pagePreviewActions");
        presenter.hideSection("pageActions");
        presenter.hideSection("areaActions");
        presenter.hideSection("optionalAreaActions");
        presenter.hideSection("editableAreaActions");
        presenter.hideSection("optionalEditableAreaActions");
        presenter.hideSection("componentActions");
    }

    private void resetActionbar() {
        //view.hideActionbar(false);
        hideAllActions(actionbarPresenter);
    }

    private void showEditor() {
        resetActionbar();
        actionbarPresenter.hideSection("pagePreviewActions");
        actionbarPresenter.showSection("pageActions");
        pageEditorPresenter.setParameters(parameters, false);
        view.setPageEditorView(pageEditorPresenter.start());
    }

    private void showFullPreview(final Location defaultLocation) {
        showEditorPreview();
        appContext.openSubAppFullScreen(PagesApp.EDITOR_TOKEN, PagesEditorSubApp.class, defaultLocation);
    }

    private void showEditorPreview() {
        resetActionbar();
        actionbarPresenter.hideSection("pageActions");
        actionbarPresenter.showSection("pagePreviewActions");
        pageEditorPresenter.setParameters(parameters, true);
        view.setPageEditorView(pageEditorPresenter.start());
    }

    private List<String> parsePathParamsFromToken(String token) {
        return new ArrayList<String>(Arrays.asList(token.split(";")));
    }

    private void showPageActions() {
        actionbarPresenter.showSection("pageActions");
        actionbarPresenter.hideSection("areaActions");
        actionbarPresenter.hideSection("componentActions");
    }

    private void showAreaActions() {
        actionbarPresenter.showSection("areaActions");
        actionbarPresenter.hideSection("pageActions");
        actionbarPresenter.hideSection("componentActions");
    }

    private void showComponentActions() {
        actionbarPresenter.showSection("componentActions");
        actionbarPresenter.hideSection("areaActions");
        actionbarPresenter.hideSection("pageActions");
    }

    private String getEditorPath(Location location) {
        String token = ((DefaultLocation) location).getToken();
        String[] parts = token.split(";");
        if (parts.length < 2) {
            return null;
        }
        if (!parts[0].contains(PagesApp.EDITOR_TOKEN)) {
            return null;
        }
        return parts[1];
    }

    private void bindHandlers() {

        subAppEventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
                try {
                    ActionDefinition actionDefinition = event.getActionDefinition();
                    actionbarPresenter.createAndExecuteAction(actionDefinition, appDescriptor.getWorkbench().getWorkspace(), parameters.getNodePath());
                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
            }
        });

        appEventBus.addHandler(NodeSelectedEvent.class, new NodeSelectedEvent.Handler() {

            @Override
            public void onItemSelected(NodeSelectedEvent event) {
                // TODO 20120730 mgeljic, review whether presenter should be a proxy for every
                // single actionbar widget feature
                String workspace = event.getWorkspace();
                String path = event.getPath();
                try {
                    Session session = MgnlContext.getJCRSession(workspace);

                    if (path == null || !session.itemExists(path)) {
                        path = "/";
                    }
                    Node node = session.getNode(path);

                    if (node.isNodeType(MgnlNodeType.NT_PAGE)) {
                        showPageActions();
                    } else if (node.isNodeType(MgnlNodeType.NT_AREA)) {
                        showAreaActions();
                    } else if (node.isNodeType(MgnlNodeType.NT_COMPONENT)) {
                        showComponentActions();
                    } else {
                        hideAllActions(actionbarPresenter);
                    }
                } catch (RepositoryException e) {
                    log.error("Exception caught: {}", e.getMessage(), e);
                }
            }
        });
    }

}
