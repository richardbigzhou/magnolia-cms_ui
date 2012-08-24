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
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.app.pages.PagesApp;
import info.magnolia.ui.app.pages.PagesAppDescriptor;
import info.magnolia.ui.app.pages.action.AddComponentActionDefinition;
import info.magnolia.ui.app.pages.action.EditElementActionDefinition;
import info.magnolia.ui.app.pages.action.EditPageActionDefinition;
import info.magnolia.ui.app.pages.action.PreviewPageActionDefinition;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import info.magnolia.ui.widget.editor.PageEditor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp extends AbstractSubApp implements PagesEditorView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private final PagesEditorView view;

    private final EventBus eventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    private final PagesAppDescriptor appDescriptor;

    private final WorkbenchActionFactory actionFactory;

    private final AppContext appContext;

    @Inject
    public PagesEditorSubApp(final AppContext appContext, final PagesEditorView view, final @Named("subapp") EventBus eventBus,
        final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, final WorkbenchActionFactory actionFactory) {

        this.view = view;
        this.view.setListener(this);
        this.eventBus = eventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = appContext;
        this.appDescriptor = (PagesAppDescriptor) appContext.getAppDescriptor();
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

        initParameters(location);

        ActionbarDefinition actionbarDefinition = appDescriptor.getEditor().getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition, actionFactory);
        view.setActionbarView(actionbar);

        if (parameters.isPreview()) {
            log.debug("Preview type detected is {}", parameters.isFullScreen() ? "full preview" : "normal preview");
            showPreview();
        } else {
            showEditor();
        }

        return view;
    }

    @Override
    public void locationChanged(Location location) {
        initParameters(location);
        if (parameters.isPreview()) {
            if (parameters.isFullScreen()) {
                showFullPreview(location);
            } else {
                showPreview();
            }
        } else {
            if (parameters.isFullScreen()) {
                showFullEditor(location);
            } else {
                showEditor();
            }
        }
    }

    private void initParameters(Location location) {
        String editingMode = getEditingMode(location);
        String path = getEditorPath(location);
        if (path == null) {
            path = "/";
        }
        setParameters(new PageEditorParameters(MgnlContext.getContextPath(), path, editingMode));
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
        pageEditorPresenter.setParameters(parameters);
        view.setPageEditorView(pageEditorPresenter.start());
    }

    private void showPreview() {
        hideAllSections();
        actionbarPresenter.showSection("pagePreviewActions");
        pageEditorPresenter.setParameters(parameters);
        view.setPageEditorView(pageEditorPresenter.start());
    }

    private void showFullPreview(final Location defaultLocation) {
        showPreview();
        appContext.enterFullScreenMode();
    }

    private void showFullEditor(final Location defaultLocation) {
        showEditor();
        appContext.enterFullScreenMode();
    }

    private void bindHandlers() {

        eventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
                try {
                    ActionDefinition actionDefinition = event.getActionDefinition();
                    if (actionDefinition instanceof EditElementActionDefinition) {
                        pageEditorPresenter.editComponent(
                                appDescriptor.getWorkbench().getWorkspace(),
                                pageEditorPresenter.getSelectedElement().getPath(),
                                pageEditorPresenter.getSelectedElement().getDialog());
                    } else if (actionDefinition instanceof AddComponentActionDefinition) {

                        // casting to AreaElement, because this action is only defined for areas
                        pageEditorPresenter.newComponent(
                                appDescriptor.getWorkbench().getWorkspace(),
                                pageEditorPresenter.getSelectedElement().getPath(),
                                ((PageEditor.AreaElement) pageEditorPresenter.getSelectedElement()).getAvailableComponents());
                    }
                    else if (actionDefinition instanceof PreviewPageActionDefinition || actionDefinition instanceof EditPageActionDefinition) {
                        actionbarPresenter.createAndExecuteAction(actionDefinition, appDescriptor.getWorkbench().getWorkspace(), parameters.getNodePath());
                    }
                    else {
                        actionbarPresenter.createAndExecuteAction(
                                actionDefinition,
                                appDescriptor.getWorkbench().getWorkspace(),
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
            }
        });
    }

    // Location token handling, format is editor;<editorPath>:<previewMode>

    public static boolean supportsLocation(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 1 && parts.get(0).equals("editor");
    }

    public static  DefaultLocation createLocation(String editorPath, String previewMode) {
        String token = "editor;" + editorPath;
        if (StringUtils.isNotEmpty(previewMode)) {
            token += ":" + previewMode;
        }
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", token);
    }

    public static String getSubAppId(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.get(0) + ";" + parts.get(1);
    }

    public static String getEditorPath(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 2 ? parts.get(1) : null;
    }

    public static String getEditingMode(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 3 ? parts.get(2) : null;
    }

    private static List<String> parseLocationToken(Location location) {

        ArrayList<String> parts = new ArrayList<String>();

        DefaultLocation l = (DefaultLocation) location;
        String token = l.getToken();

        // "editor"
        int i = token.indexOf(';');
        if (i == -1) {
            return new ArrayList<String>();
        }
        String subAppName = token.substring(0, i);
        if (!subAppName.equals(PagesApp.EDITOR_TOKEN)) {
            return new ArrayList<String>();
        }
        parts.add(subAppName);
        token = token.substring(i + 1);

        // editorPath
        i = token.indexOf(':');
        if (i == -1) {
            if (token.length() == 0) {
                return new ArrayList<String>();
            }
            parts.add(token);
            return parts;
        }
        parts.add(token.substring(0, i));
        token = token.substring(i + 1);

        // previewMode
        if (token.length() > 0) {
            parts.add(token);
        }

        return parts;
    }
}
