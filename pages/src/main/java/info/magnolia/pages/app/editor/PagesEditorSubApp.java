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
package info.magnolia.pages.app.editor;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.pages.app.editor.event.ComponentMoveEvent;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp extends BaseSubApp implements PagesEditorSubAppView.Listener, ActionbarPresenter.Listener, PageBarView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    private final ActionExecutor actionExecutor;
    private final PagesEditorSubAppView view;
    private final EventBus subAppEventBus;
    private final EventBus admincentralEventBus;
    private final PageEditorPresenter pageEditorPresenter;
    private final ActionbarPresenter actionbarPresenter;
    private final PageBarView pageBarView;
    private final I18NAuthoringSupport i18NAuthoringSupport;
    private final I18nContentSupport i18nContentSupport;
    private final EditorDefinition editorDefinition;
    private final String workspace;
    private final AppContext appContext;

    private PageEditorParameters parameters;
    private PlatformType targetPreviewPlatform = PlatformType.DESKTOP;
    private Locale currentLocale;
    private String caption;

    @Inject
    public PagesEditorSubApp(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final PagesEditorSubAppView view, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, final PageBarView pageBarView, I18NAuthoringSupport i18NAuthoringSupport, I18nContentSupport i18nContentSupport) {
        super(subAppContext, view);
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.admincentralEventBus = admincentralEventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.pageBarView = pageBarView;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
        this.i18nContentSupport = i18nContentSupport;
        this.editorDefinition = ((DetailSubAppDescriptor) subAppContext.getSubAppDescriptor()).getEditor();
        this.workspace = editorDefinition.getWorkspace();
        this.appContext = subAppContext.getAppContext();
        this.currentLocale = i18nContentSupport.getLocale();
        view.setListener(this);
        bindHandlers();
    }

    @Override
    public String getCaption() {
        return caption;
    }

    public void updateCaption(String path) {
        this.caption = getPageTitle(path);
        pageBarView.setPageName(caption, path);
    }

    @Override
    public View start(Location location) {
        DetailLocation detailLocation = DetailLocation.wrap(location);
        super.start(detailLocation);

        actionbarPresenter.setListener(this);
        pageBarView.setListener(this);
        ActionbarDefinition actionbarDefinition = ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getActionbar();
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition);
        view.setActionbarView(actionbar);
        view.setPageBarView(pageBarView);
        view.setPageEditorView(pageEditorPresenter.start());
        goToLocation(detailLocation);
        pageBarView.setCurrentLanguage(i18nContentSupport.getLocale());
        return view;
    }

    private void updateActions() {
        updateActionsAccordingToOperationPermissions();
        // actions currently always disabled
        actionbarPresenter.disable(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT, "copyComponent", "pasteComponent", "undo", "redo");
    }

    /**
     * Informs the app framework when navigating pages inside the page editor.
     * Updates the shell fragment, caption and current location.
     */
    private void updateNodePath(String path) {
        DetailLocation detailLocation = getCurrentLocation();
        detailLocation.updateNodePath(path);
        setPageEditorParameters(detailLocation);
        getAppContext().updateSubAppLocation(getSubAppContext(), detailLocation);
        pageEditorPresenter.updateParameters(parameters);

    }

    /**
     * Show/Hide actions buttons according to operation permissions.
     */
    private void updateActionsAccordingToOperationPermissions() {
        AbstractElement element = pageEditorPresenter.getSelectedElement();

        if (element instanceof ComponentElement) {
            ComponentElement componentElement = (ComponentElement) element;

            if (componentElement.getDeletable() != null && !componentElement.getDeletable()) {
                actionbarPresenter.disable(PageEditorListener.ACTION_DELETE_COMPONENT);
            } else {
                actionbarPresenter.enable(PageEditorListener.ACTION_DELETE_COMPONENT);
            }

            if (componentElement.getMoveable() != null && !componentElement.getMoveable()) {
                actionbarPresenter.disable(PageEditorListener.ACTION_START_MOVE_COMPONENT);
            } else {
                actionbarPresenter.enable(PageEditorListener.ACTION_START_MOVE_COMPONENT);
            }

            if (componentElement.getWritable() != null && !componentElement.getWritable()) {
                actionbarPresenter.disable(PageEditorListener.ACTION_EDIT_COMPONENT);
            } else {
                actionbarPresenter.enable(PageEditorListener.ACTION_EDIT_COMPONENT);
            }

        } else if (element instanceof AreaElement) {
            AreaElement areaElement = (AreaElement) element;

            if (areaElement.getAddible() != null && !areaElement.getAddible()) {
                actionbarPresenter.disable(PageEditorListener.ACTION_ADD_COMPONENT);
            } else {
                actionbarPresenter.enable(PageEditorListener.ACTION_ADD_COMPONENT);
            }
        }
    }

    @Override
    public boolean supportsLocation(Location location) {
        return getCurrentLocation().getNodePath().equals(DetailLocation.wrap(location).getNodePath());
    }

    /**
     * Wraps the current DefaultLocation in a DetailLocation. Providing getter and setters for used parameters.
     */
    @Override
    public DetailLocation getCurrentLocation() {
        return DetailLocation.wrap(super.getCurrentLocation());
    }

    @Override
    public void locationChanged(Location location) {
        DetailLocation itemLocation = DetailLocation.wrap(location);
        super.locationChanged(itemLocation);
        goToLocation(itemLocation);
    }

    private void goToLocation(DetailLocation location) {
        if (isLocationChanged(location)) {
            doGoToLocation(location);
        }
    }

    private void doGoToLocation(DetailLocation location) {
        setPageEditorParameters(location);
        hideAllSections();
        pageEditorPresenter.loadPageEditor(parameters);
    }

    private void setPageEditorParameters(DetailLocation location) {
        DetailView.ViewType action = location.getViewType();
        String path = location.getNodePath();
        boolean isPreview = DetailView.ViewType.VIEW.getText().equals(action.getText());
        this.parameters = new PageEditorParameters(MgnlContext.getContextPath(), path, isPreview);
        this.parameters.setPlatformType(targetPreviewPlatform);
        try {
            Node node = MgnlContext.getJCRSession(workspace).getNode(path);
            String uri = i18NAuthoringSupport.createI18NURI(node, currentLocale);
            StringBuffer sb = new StringBuffer(uri);

            if (isPreview) {
                LinkUtil.addParameter(sb, "mgnlPreview", "true");
            } else {
                // reset channel
                this.targetPreviewPlatform = PlatformType.DESKTOP;
                this.parameters.setPlatformType(targetPreviewPlatform);
                pageBarView.setPlatFormType(targetPreviewPlatform);

                LinkUtil.addParameter(sb, "mgnlPreview", "false");
            }
            LinkUtil.addParameter(sb, "mgnlChannel", targetPreviewPlatform.getId());

            if (location.hasVersion()) {
                LinkUtil.addParameter(sb, "mgnlVersion", location.getVersion());
            }
            uri = sb.toString();
            this.parameters.setUrl(uri);
            updateCaption(path);
            pageBarView.togglePreviewMode(isPreview);
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean isLocationChanged(DetailLocation location) {
        DetailView.ViewType action = location.getViewType();
        String path = location.getNodePath();

        if (parameters != null && (parameters.getNodePath().equals(path) && parameters.isPreview() == DetailView.ViewType.VIEW.getText().equals(action.getText())) && !location.hasVersion()) {
            return false;
        }
        return true;
    }

    private String getPageTitle(String path) {
        String caption = null;
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            Node node = session.getNode(path);
            caption = PropertyUtil.getString(node, "title", node.getName());
        } catch (RepositoryException e) {
            log.error("Exception caught: {}", e.getMessage(), e);
        }
        return caption;
    }

    private void hideAllSections() {
        actionbarPresenter.hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions", "pageDeleteActions");
    }

    private void bindHandlers() {

        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getWorkspace().equals(RepositoryConstants.WEBSITE)) {
                    // Check if the node still exist
                    try {
                        String currentNodePath = getCurrentLocation().getNodePath();
                        if (!MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).nodeExists(currentNodePath)) {
                            getSubAppContext().close();
                        }
                    } catch (RepositoryException e) {
                        log.warn("Could not determine if currently edited page exists", e);
                    }
                }
            }
        });

        subAppEventBus.addHandler(ComponentMoveEvent.class, new ComponentMoveEvent.Handler() {
            @Override
            public void onMove(ComponentMoveEvent event) {
                if (event.isStart()) {
                    actionbarPresenter.disable(PageEditorListener.ACTION_START_MOVE_COMPONENT);
                    actionbarPresenter.enable(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT);
                } else {
                    actionbarPresenter.enable(PageEditorListener.ACTION_START_MOVE_COMPONENT);
                    actionbarPresenter.disable(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT);
                }
            }
        });

        subAppEventBus.addHandler(NodeSelectedEvent.class, new NodeSelectedEvent.Handler() {

            @Override
            public void onItemSelected(NodeSelectedEvent event) {
                AbstractElement element = event.getElement();
                updateActionbar(element);
            }
        });
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        AbstractElement selectedElement = pageEditorPresenter.getSelectedElement();
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            final javax.jcr.Item item = session.getItem(selectedElement.getPath());
            actionExecutor.execute(actionName, new JcrNodeAdapter((Node) item), selectedElement, pageEditorPresenter);

        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }

    }

    @Override
    public String getLabel(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return (actionDefinition != null) ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return (actionDefinition != null) ? actionDefinition.getIcon() : null;
    }

    @Override
    public void languageSelected(Locale locale) {
        this.currentLocale = locale;
        doGoToLocation(getCurrentLocation());
    }

    @Override
    public void platformSelected(PlatformType platformType) {
        this.targetPreviewPlatform = platformType;
        doGoToLocation(getCurrentLocation());
    }

    /**
     * This method has package visibility for testing purposes only.
     */
    final PageEditorParameters getParameters() {
        return parameters;
    }

    private boolean isDeletedNode(String workspace, String path) {
        Node node = SessionUtil.getNode(workspace, path);
        try {
            if (node != null) {
                return NodeUtil.hasMixin(node, NodeTypes.Deleted.NAME);
            } else {
                return false;
            }
        } catch (RepositoryException re) {
            log.warn("Not able to check if node has MixIn");
            return false;
        }
    }

    private void updateActionbar(final AbstractElement element) {
        String path = element.getPath();
        String dialog = element.getDialog();
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        hideAllSections();

        if (isDeletedNode(workspace, path)) {
            actionbarPresenter.showSection("pageDeleteActions");

            if (!getCurrentLocation().hasVersion()) {
                actionbarPresenter.enable("showPreviousVersion");
            } else {
                actionbarPresenter.disable("showPreviousVersion");
            }
            actionbarPresenter.enable("restorePreviousVersion");
            actionbarPresenter.enable("activateDelete");

        } else {

            if (element instanceof PageElement) {
                updateCaption(path);

                if (!path.equals(parameters.getNodePath())) {
                    updateNodePath(path);
                }

                if (parameters.isPreview()) {
                    actionbarPresenter.showSection("pagePreviewActions");
                    ActionbarSectionDefinition def = getActionbarSectionDefinitionByName("pagePreviewActions");
                    if (def != null) {
                        enableOrDisableActions(def, path);
                    }

                } else {
                    actionbarPresenter.showSection("pageActions");
                    ActionbarSectionDefinition def = getActionbarSectionDefinitionByName("pageActions");
                    if (def != null) {
                        enableOrDisableActions(def, path);
                    }
                }
            } else if (element instanceof AreaElement) {
                if (dialog == null) {
                    actionbarPresenter.showSection("areaActions");
                } else {
                    actionbarPresenter.showSection("editableAreaActions");
                }
            } else if (element instanceof ComponentElement) {
                actionbarPresenter.showSection("componentActions");
            }
            updateActions();
        }
    }

    private void enableOrDisableActions(final ActionbarSectionDefinition def, final String path) {
        // Evaluate availability of each action within the section
        Node node = SessionUtil.getNode(workspace, path);
        for (ActionbarGroupDefinition groupDefinition : def.getGroups()) {
            for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                String actionName = itemDefinition.getName();
                if (actionExecutor.isAvailable(actionName, node)) {
                    actionbarPresenter.enable(actionName);
                } else {
                    actionbarPresenter.disable(actionName);
                }
            }
        }
    }

    private ActionbarSectionDefinition getActionbarSectionDefinitionByName(final String sectionName) {
        ActionbarDefinition actionbarDefinition = ((DetailSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getActionbar();
        if (actionbarDefinition == null) {
            log.warn("No actionbar definition found, returning null");
            return null;
        }

        for (ActionbarSectionDefinition section : actionbarDefinition.getSections()) {
            if (section.getName().equals(sectionName)) {
                return section;
            }
        }
        log.warn("No section named [{}] found, returning null", sectionName);
        return null;
    }

    @Override
    public void onEscape() {
        if (pageEditorPresenter.isMoving()) {
            pageEditorPresenter.onAction(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT);
        }
    }
}
