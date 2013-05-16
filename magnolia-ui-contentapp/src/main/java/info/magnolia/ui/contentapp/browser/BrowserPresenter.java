/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.actionbar.definition.SectionRestrictionsDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBus;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;

import java.awt.Point;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.data.Item;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;


/**
 * The browser is a core component of AdminCentral. It represents the main hub through which users can interact with
 * JCR data. It is compounded by three main sub-components:
 * <ul>
 * <li>a configurable data grid.
 * <li>a configurable function toolbar on top of the data grid, providing operations such as switching from tree to list view or thumbnail view or performing searches on data.
 * <li>a configurable action bar on the right hand side, showing the available operations for the given workspace and the selected item.
 * </ul>
 * <p>
 * Its main configuration point is the {@link info.magnolia.ui.workbench.definition.WorkbenchDefinition} through which one defines the JCR workspace to connect to, the columns/properties to display, the available actions and so on.
 */
public class BrowserPresenter implements ActionbarPresenter.Listener, BrowserView.Listener {

    private static final Logger log = LoggerFactory.getLogger(BrowserPresenter.class);

    private WorkbenchPresenter workbenchPresenter;

    private final ActionExecutor actionExecutor;

    private BrowserSubAppDescriptor subAppDescriptor;

    private final BrowserView view;

    private final EventBus adminCentralEventBus;

    private final EventBus subAppEventBus;

    private final ActionbarPresenter actionbarPresenter;

    private final ImageProvider imageProvider;

    private final AppContext appContext;

    private final SubAppContext subAppContext;

    @Inject
    public BrowserPresenter(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final BrowserView view, @Named(AdmincentralEventBus.NAME) final EventBus adminCentralEventBus,
            final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            final ActionbarPresenter actionbarPresenter, final ComponentProvider componentProvider, WorkbenchPresenter workbenchPresenter) {
        this.workbenchPresenter = workbenchPresenter;
        this.actionExecutor = actionExecutor;
        this.subAppContext = subAppContext;
        this.view = view;
        this.adminCentralEventBus = adminCentralEventBus;
        this.subAppEventBus = subAppEventBus;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = subAppContext.getAppContext();
        this.subAppDescriptor = (BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor();

        ImageProviderDefinition imageProviderDefinition = subAppDescriptor.getImageProvider();
        if (imageProviderDefinition == null) {
            this.imageProvider = null;
        } else {
            this.imageProvider = componentProvider.newInstance(imageProviderDefinition.getImageProviderClass(), imageProviderDefinition);
        }
    }

    public BrowserView start() {
        actionbarPresenter.setListener(this);

        WorkbenchView workbenchView = workbenchPresenter.start(subAppDescriptor.getWorkbench(), subAppDescriptor.getImageProvider(), subAppEventBus);
        ActionbarView actionbar = actionbarPresenter.start(subAppDescriptor.getActionbar());

        view.setWorkbenchView(workbenchView);
        view.setActionbarView(actionbar);
        view.setListener(this);

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        adminCentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getWorkspace().equals(getWorkspace())) {
                    refreshActionbarPreviewImage(event.getPath(), event.getWorkspace());
                    workbenchPresenter.selectPath(event.getPath());
                    workbenchPresenter.refresh();
                }
            }
        });

        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                refreshActionbarPreviewImage(event.getPath(), event.getWorkspace());
            }
        });

        subAppEventBus.addHandler(ItemDoubleClickedEvent.class, new ItemDoubleClickedEvent.Handler() {

            @Override
            public void onItemDoubleClicked(ItemDoubleClickedEvent event) {
                executeDefaultAction();
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                workbenchPresenter.doSearch(event.getSearchExpression());
            }
        });

        subAppEventBus.addHandler(ItemEditedEvent.class, new ItemEditedEvent.Handler() {

            @Override
            public void onItemEdited(ItemEditedEvent event) {
                editItem(event);
            }
        });
    }

    public String getSelectedItemId() {
        return workbenchPresenter.getSelectedId();
    }

    /**
     * @return The configured default view Type.<br>
     * If non define, return the first Content Definition as default.
     */
    public ViewType getDefaultViewType() {
        return workbenchPresenter.getDefaultViewType();
    }

    public BrowserView getView() {
        return view;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    public String getWorkspace() {
        return workbenchPresenter.getWorkspace();
    }

    /**
     * Synchronizes the underlying view to reflect the status extracted from the Location token, i.e. selected path,
     * view type and optional query (in case of a search view).
     */
    public void resync(final String path, final ViewType viewType, final String query) {
        workbenchPresenter.resynch(path, viewType, query);
    }

    private void refreshActionbarPreviewImage(final String path, final String workspace) {
        if (StringUtils.isBlank(path)) {
            actionbarPresenter.setPreview(null);
        } else {
            if (imageProvider != null) {
                Object previewResource = imageProvider.getThumbnailResourceByPath(workspace, path, ImageProvider.PORTRAIT_GENERATOR);
                if (previewResource instanceof Resource) {
                    actionbarPresenter.setPreview((Resource) previewResource);
                } else {
                    actionbarPresenter.setPreview(null);
                }
            }
        }
    }

    private void editItem(ItemEditedEvent event) {
        Item item = event.getItem();
        // don't save if no value change occurred on adapter
        if (!(item instanceof AbstractJcrAdapter) || !((AbstractJcrAdapter) item).hasChangedProperties()) {
            return;
        }

        if (item instanceof JcrItemNodeAdapter) {
            // Saving JCR Node, getting updated node first
            Node node = ((JcrItemNodeAdapter) item).getNode();
            try {
                LastModified.update(node);
                node.getSession().save();
            } catch (RepositoryException e) {
                log.error("Could not save changes to node.", e);
            }

        } else if (item instanceof JcrPropertyAdapter) {
            // Saving JCR Property, update it first
            try {
                // get parent first because once property is updated, it won't exist anymore.
                Property property = ((JcrPropertyAdapter) item).getProperty();
                Node parent = property.getParent();
                ((JcrPropertyAdapter) item).updateProperties();
                LastModified.update(parent);
                parent.getSession().save();
            } catch (RepositoryException e) {
                log.error("Could not save changes to node.", e);
            }
        }
    }

    @Override
    public void onActionbarItemClicked(String itemName) {
        executeAction(itemName);
    }

    @Override
    public void onActionBarSelection(String actionName) {
        executeAction(actionName);
    }

    @Override
    public String getLabel(String itemName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(itemName);
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String itemName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(itemName);
        return actionDefinition != null ? actionDefinition.getIcon() : null;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            appContext.enterFullScreenMode();
        } else {
            appContext.exitFullScreenMode();
        }
    }

    /**
     * Executes the default action, as configured in the {@link info.magnolia.ui.actionbar.definition.ActionbarDefinition}.
     */
    private void executeDefaultAction() {
        String defaultAction = subAppDescriptor.getActionbar().getDefaultAction();
        if (StringUtils.isNotEmpty(defaultAction)) {
            executeAction(defaultAction);
        } else {
            log.warn("Default action is null. Please check actionbar definition.");
        }
    }

    private void executeAction(String actionName) {
        try {
            Session session = MgnlContext.getJCRSession(getWorkspace());
            if (session.itemExists(getSelectedItemId())) {
                javax.jcr.Item item = session.getItem(getSelectedItemId());
                if (item.isNode()) {
                    actionExecutor.execute(actionName, new JcrNodeAdapter((Node) item));
                } else {
                    actionExecutor.execute(actionName, new JcrPropertyAdapter((Property) item));
                }
            } else {
                Message error = new Message(MessageType.ERROR, "Could not get item ", "Following Item not found :" + getSelectedItemId());
                appContext.sendLocalMessage(error);
            }
        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, "Could not get item: " + getSelectedItemId(), e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }
    }

    /**
     * Show the actionPopup for the specified item at the specified coordinates.
     */
    public void showActionPopup(String absItemPath, Point clickCoordinates) {
        ActionPopup actionPopupView = view.getActionPopupView();

        updateActionPopup(actionPopupView, absItemPath);
        int x;
        int y;
        if (clickCoordinates != null) {
            x = clickCoordinates.x;
            y = clickCoordinates.y;
        } else {
            // Center it if we don't have coordinates.
            x = Page.getCurrent().getBrowserWindowWidth() / 2;
            y = Page.getCurrent().getBrowserWindowHeight() / 2;
        }
        actionPopupView.open(x, y);
    }

    /**
     * TODO: Eliminate redundancy with BrowserSubApp.updateActionBar (MGNLUI-1367) Christopher Zimmermann.
     * Update the items in the actionPopup based on the specified item and the ActionPopup availability configuration.
     */
    private void updateActionPopup(ActionPopup actionPopupView, String absItemPath) {

        actionPopupView.removeAllItems();

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor();
        WorkbenchDefinition workbench = subAppDescriptor.getWorkbench();
        List<ActionbarSectionDefinition> sections = subAppDescriptor.getActionbar().getSections();

        try {

            javax.jcr.Item item = null;
            if (absItemPath != null && !absItemPath.equals(workbench.getPath())) {
                final Session session = MgnlContext.getJCRSession(workbench.getWorkspace());
                item = session.getItem(absItemPath);
            }

            // Figure out which section to show, only one
            ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, item);

            // If there no section matched the selection we just hide everything
            if (sectionDefinition == null) {
                return;
            }

            // Evaluate availability of each action within the section
            ContextMenuItem menuItem = null;
            for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
                for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                    String actionName = itemDefinition.getName();
                    if (actionExecutor.isAvailable(actionName, item)) {
                        ActionDefinition action = subAppDescriptor.getActions().get(actionName);
                        String label = action.getLabel();
                        String iconFontCode = ActionPopup.ICON_FONT_CODE + action.getIcon();
                        ExternalResource iconFontResource = new ExternalResource(iconFontCode);
                        menuItem = actionPopupView.addItem(label, iconFontResource);
                        // Set data so that the event handler can determine which action to launch.
                        menuItem.setData(actionName);
                    }
                }
                // Add group separator.
                if (menuItem != null) {
                    menuItem.setSeparatorVisible(true);
                }
            }
            if (menuItem != null) {
                menuItem.setSeparatorVisible(false);
            }
        } catch (RepositoryException e) {
            log.error("Failed to updated actionbar", e);
        }

    }

    /**
     * TODO: Eliminate redundancy with BrowserSubApp.updateActionBar (MGNLUI-1367) Christopher Zimmermann.
     */
    private ActionbarSectionDefinition getVisibleSection(List<ActionbarSectionDefinition> sections, javax.jcr.Item item) throws RepositoryException {
        for (ActionbarSectionDefinition section : sections) {
            if (isSectionVisible(section, item))
                return section;
        }
        return null;
    }

    /**
     * TODO: Eliminate redundancy with BrowserSubApp.updateActionBar (MGNLUI-1367) Christopher Zimmermann.
     */
    private boolean isSectionVisible(ActionbarSectionDefinition section, javax.jcr.Item item) throws RepositoryException {
        SectionRestrictionsDefinition restrictions = section.getRestrictions();

        // If this is the root item we display the section only if the root property is set
        if (item == null)
            return restrictions.isRoot();

        // If its a property we display it only if the properties property is set
        if (!item.isNode())
            return restrictions.isProperties();

        // The node must match at least one of the configured node types
        for (String nodeType : restrictions.getNodeTypes()) {
            if (NodeUtil.isNodeType((Node) item, nodeType))
                return true;
        }
        return false;
    }

}
