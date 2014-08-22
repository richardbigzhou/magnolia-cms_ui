/**
 * This file Copyright (c) 2013-2014 Magnolia International
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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.pages.app.editor.pagebar.PageBarPresenter;
import info.magnolia.pages.app.editor.statusbar.StatusBarPresenter;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.ActionbarView;
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
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubApp putting together:
 * <ul>
 *   <li>{@link PageEditorPresenter}</li>
 *   <li>{@link ActionbarPresenter}</li>
 *   <li>{@link StatusBarPresenter}</li>
 *   <li>{@link PageBarPresenter}</li>
 * </ul>
 * Keeps track on changes coming from the client side of the {@link PageEditorPresenter} by registering a {@link NodeSelectedEvent.Handler}
 * to the subApp eventbus. This is triggered every time the {@link PageElement}, {@link AreaElement} or {@link info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement}
 * is selected inside the iFrame. This will result in updating the {@link ActionbarPresenter} accordingly using action
 * availability. <br />
 *
 * When a page change happens, e.g. the user browses inside the iFrame, this will also be detected and trigger a chain
 * of actions taking care of updating all necessary parts of the UI as well as the underlying framework.
 *
 */
public class PagesEditorSubApp extends BaseSubApp<PagesEditorSubAppView> implements PagesEditorSubAppView.Listener, ActionbarPresenter.Listener, PageEditorPresenter.Listener {

    private static final Logger log = LoggerFactory.getLogger(PagesEditorSubApp.class);

    protected static final String PROPERTY_TITLE = "title";

    private final ActionExecutor actionExecutor;
    private final PagesEditorSubAppView view;
    private final EventBus subAppEventBus;
    private final EventBus admincentralEventBus;
    private final PageEditorPresenter pageEditorPresenter;
    private final ActionbarPresenter actionbarPresenter;
    private final JcrContentConnector contentConnector;
    private final AvailabilityChecker availabilityChecker;
    private final StatusBarPresenter statusBar;
    private final PageBarPresenter pageBar;
    private final String workspace;
    private final AppContext appContext;
    private final VersionManager versionManager;
    private final SimpleTranslator i18n;

    private String caption;

    /**
     * @deprecated since 5.2.4 - use info.magnolia.pages.app.editor.PagesEditorSubApp#PagesEditorSubApp(info.magnolia.ui.api.action.ActionExecutor, info.magnolia.ui.api.app.SubAppContext, info.magnolia.pages.app.editor.PagesEditorSubAppView, info.magnolia.event.EventBus, info.magnolia.event.EventBus, info.magnolia.pages.app.editor.PageEditorPresenter, info.magnolia.ui.actionbar.ActionbarPresenter, info.magnolia.ui.vaadin.editor.pagebar.PageBarView, info.magnolia.ui.api.i18n.I18NAuthoringSupport, info.magnolia.cms.i18n.I18nContentSupport, info.magnolia.cms.core.version.VersionManager, info.magnolia.i18nsystem.SimpleTranslator, info.magnolia.ui.api.availability.AvailabilityChecker, info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector)
     */
    @Deprecated
    public PagesEditorSubApp(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final PagesEditorSubAppView view, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
                             final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, final PageBarView pageBarView,
                             I18NAuthoringSupport i18NAuthoringSupport, I18nContentSupport i18nContentSupport, VersionManager versionManager, final SimpleTranslator i18n, AvailabilityChecker availabilityChecker,
                             ContentConnector contentConnector) {
        this(actionExecutor, subAppContext, view, admincentralEventBus, subAppEventBus, pageEditorPresenter, actionbarPresenter, versionManager, i18n, availabilityChecker, contentConnector, Components.getComponent(StatusBarPresenter.class), Components.getComponent(PageBarPresenter.class));
    }

    @Inject
    public PagesEditorSubApp(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final PagesEditorSubAppView view, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
                             final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, final PageEditorPresenter pageEditorPresenter, final ActionbarPresenter actionbarPresenter, VersionManager versionManager, final SimpleTranslator i18n, AvailabilityChecker availabilityChecker,
                             ContentConnector contentConnector, StatusBarPresenter statusBar, PageBarPresenter pageBar) {
        super(subAppContext, view);
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.admincentralEventBus = admincentralEventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.availabilityChecker = availabilityChecker;
        this.statusBar = statusBar;
        this.pageBar = pageBar;
        this.contentConnector = (JcrContentConnector) contentConnector;
        this.workspace = this.contentConnector.getContentConnectorDefinition().getWorkspace();
        this.appContext = subAppContext.getAppContext();
        this.versionManager = versionManager;
        this.i18n = i18n;
        bindHandlers();
    }

    @Override
    public String getCaption() {
        return caption;
    }

    public void updateCaption(DetailLocation location) {
        this.caption = getPageTitle(location);
        pageBar.setPageName(caption, location.getNodePath());
    }

    @Override
    public PagesEditorSubAppView start(Location location) {
        view.setListener(this);

        DetailLocation detailLocation = DetailLocation.wrap(location);
        super.start(detailLocation);

        ActionbarDefinition actionbarDefinition = ((ContentSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getActionbar();
        Map<String, ActionDefinition> actionDefinitions = getSubAppContext().getSubAppDescriptor().getActions();
        actionbarPresenter.setListener(this);
        ActionbarView actionbar = actionbarPresenter.start(actionbarDefinition, actionDefinitions);
        hideActionbarSections();

        pageBar.setListener(this);
        pageEditorPresenter.setListener(this);
        statusBar.setListener(this);
        view.setActionbarView(actionbar);
        view.setPageBarView(pageBar.start());
        view.setPageEditorView(pageEditorPresenter.start(detailLocation));
        view.setStatusBarView(statusBar.start(detailLocation));

        updateLocationDependentComponents(detailLocation);

        return view;
    }

    /**
     * Informs the app framework when navigating pages inside the page editor.
     * Updates the shell fragment, caption and current location.
     */
    protected void updateNodePath(String path) {
        DetailLocation detailLocation = getCurrentLocation();
        detailLocation.updateNodePath(path);
        updateLocationDependentComponents(detailLocation);
        getAppContext().updateSubAppLocation(getSubAppContext(), detailLocation);
        pageEditorPresenter.getStatus().setNodePath(path);
        pageEditorPresenter.updateParameters();
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

        if (pageEditorPresenter.getStatus().isLocationChanged(itemLocation)) {
            pageEditorPresenter.reload(itemLocation);
            updateLocationDependentComponents(itemLocation);
        }
    }

    private void updateLocationDependentComponents(DetailLocation location) {
        pageBar.onLocationUpdate(location);
        updateCaption(location);
    }

    private String getPageTitle(DetailLocation location) {
        String caption = StringUtils.EMPTY;
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            Node node = session.getNode(location.getNodePath());
            if (StringUtils.isNotBlank(location.getVersion())) {
                node = versionManager.getVersion(node, location.getVersion());
                caption = i18n.translate("subapp.versioned_page", PropertyUtil.getString(node, PROPERTY_TITLE, node.getName()), location.getVersion());
            } else {
                caption = PropertyUtil.getString(node, PROPERTY_TITLE, node.getName());
            }

        } catch (RepositoryException e) {
            log.warn("Could not set page Tab Title for item : {}", location.getNodePath(), e);
        }
        return caption;
    }

    private void bindHandlers() {

        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (contentConnector.canHandleItem(event.getItemId())) {
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

        subAppEventBus.addHandler(NodeSelectedEvent.class, new NodeSelectedEvent.Handler() {

            @Override
            public void onItemSelected(NodeSelectedEvent event) {
                AbstractElement element = event.getElement();
                if (element instanceof PageElement) {
                    String path = element.getPath();
                    if (StringUtils.isEmpty(path)) {
                        path = "/";
                    }
                    if (!path.equals(pageEditorPresenter.getStatus().getNodePath())) {
                        updateNodePath(path);
                    }
                }
                updateActionbar();
            }
        });

    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        prepareAndExecutePagesEditorAction(actionName);
    }

    protected void prepareAndExecutePagesEditorAction(String actionName) {
        AbstractElement selectedElement = pageEditorPresenter.getSelectedElement();
        if (selectedElement == null) {
            log.warn("Trying to execute action [{}] but no element was selected. Was the page actually loaded?", actionName);
            return;
        }
        try {
            Object itemId = getItemId(selectedElement);
            actionExecutor.execute(actionName, contentConnector.getItem(itemId), selectedElement, pageEditorPresenter);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, i18n.translate("pages.pagesEditorSubapp.actionExecutionException.message"), e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }
    }

    public void updateActionbar() {
        Object itemId = getItemId(pageEditorPresenter.getSelectedElement());


        // Figure out which section to show, only one
        List<ActionbarSectionDefinition> sections = getSections();
        ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, itemId);

        // Hide all other sections
        for (ActionbarSectionDefinition section : sections) {
            actionbarPresenter.hideSection(section.getName());
        }

        if (sectionDefinition != null) {
            // Show our section
            actionbarPresenter.showSection(sectionDefinition.getName());

            // Evaluate availability of each action within the section
            for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
                for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                    String actionName = itemDefinition.getName();
                    ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
                    if (actionDefinition != null) {
                        AvailabilityDefinition availability = actionDefinition.getAvailability();
                        if (availabilityChecker.isAvailable(availability, Arrays.asList(itemId))) {
                            actionbarPresenter.enable(actionName);
                        } else {
                            actionbarPresenter.disable(actionName);
                        }
                    }
                }
            }
        }
    }

    private Object getItemId(AbstractElement element) {
        if (element == null) {
            return null;
        }
        else if (element instanceof AreaElement && ((AreaElement) element).isOptional() && !((AreaElement) element).isCreated()) {

            try {
                int index = element.getPath().lastIndexOf("/");
                String parentPath = element.getPath().substring(0, index);
                String relPath = element.getPath().substring(index + 1);

                Node parent = MgnlContext.getJCRSession(getWorkspace()).getNode(parentPath);

                JcrNewNodeItemId jcrNewNodeItemId = new JcrNewNodeItemId(parent.getIdentifier(), getWorkspace(), NodeTypes.Area.NAME);
                jcrNewNodeItemId.setName(Path.getUniqueLabel(parent, relPath));
                return jcrNewNodeItemId;
            } catch (RepositoryException e) {
                log.error("Failed to create new jcr node item id: " + e.getMessage(), e);
            }
            return null;
        }
        else {
            return contentConnector.getItemIdByUrlFragment(element.getPath());
        }
    }

    private List<ActionbarSectionDefinition> getSections() {
        DetailSubAppDescriptor subAppDescriptor = (DetailSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return Collections.EMPTY_LIST;
        }
        return actionbarDefinition.getSections();
    }

    private void hideActionbarSections() {
        for (ActionbarSectionDefinition sectionDefinition : getSections()) {
            actionbarPresenter.hideSection(sectionDefinition.getName());
        }
    }

    private ActionbarSectionDefinition getVisibleSection(List<ActionbarSectionDefinition> sections, Object itemId) {
        for (ActionbarSectionDefinition section : sections) {
            if (availabilityChecker.isAvailable(section.getAvailability(), Arrays.asList(itemId)))
                return section;
        }
        return null;
    }

    @Override
    public void onEscape() {
        if (pageEditorPresenter.isMoving()) {
            pageEditorPresenter.onAction(PageEditorListener.ACTION_CANCEL_MOVE_COMPONENT);
        } else {
            // Toggle preview and edit mode.
            if (getCurrentLocation().getViewType().equals(DetailView.ViewType.EDIT)) {
                prepareAndExecutePagesEditorAction(PageEditorListener.ACTION_VIEW_PREVIEW);
            } else {
                prepareAndExecutePagesEditorAction(PageEditorListener.ACTION_VIEW_EDIT);
            }
        }
    }

    protected String getWorkspace() {
        return workspace;
    }

    @Override
    public void onMove() {
        updateActionbar();
    }

}
