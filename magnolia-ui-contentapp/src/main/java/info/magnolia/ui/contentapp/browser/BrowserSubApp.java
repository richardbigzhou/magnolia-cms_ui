/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemRightClickedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.event.ViewTypeChangedEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.server.ExternalResource;

/**
 * Base implementation of a content subApp. A content subApp displays a collection of data represented inside a {@link info.magnolia.ui.workbench.ContentView}
 * created by the {@link info.magnolia.ui.workbench.WorkbenchPresenter}.
 * <pre>
 *  <p>
 *      This class Provides sensible implementation for services shared by all content subApps.
 *      Out-of-the-box it will handle the following:
 *  </p>
 *
 *  <ul>
 *      <li>location updates when switching views, selecting items or performing searches: see {@link #locationChanged(Location)}
 *      <li>restoring the browser app status when i.e. coming from a bookmark: see {@link #start(Location)}
 *  </ul>
 * In order to perform those tasks this class registers non-overridable handlers for the following events:
 *  <ul>
 *      <li> {@link SelectionChangedEvent}
 *      <li> {@link ViewTypeChangedEvent}
 *      <li> {@link SearchEvent}
 *  </ul>
 * Subclasses can augment the default behavior and perform additional tasks by overriding the following methods:
 *  <ul>
 *      <li>{@link #onSubAppStart()}
 *      <li>{@link #locationChanged(Location)}
 *      <li>{@link #updateActionbar(ActionbarPresenter)}
 *  </ul>
 * </pre>
 *
 * @see BrowserPresenter
 * @see info.magnolia.ui.contentapp.ContentSubAppView
 * @see info.magnolia.ui.contentapp.ContentApp
 * @see BrowserLocation
 */
public class BrowserSubApp extends BaseSubApp {

    private static final Logger log = LoggerFactory.getLogger(BrowserSubApp.class);

    private final BrowserPresenter browser;
    private final EventBus subAppEventBus;
    private ActionExecutor actionExecutor;
    private ComponentProvider componentProvider;
    private String workbenchRoot;

    @Inject
    public BrowserSubApp(ActionExecutor actionExecutor, final SubAppContext subAppContext, final ContentSubAppView view, final BrowserPresenter browser, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, ComponentProvider componentProvider) {
        super(subAppContext, view);
        if (subAppContext == null || view == null || browser == null || subAppEventBus == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found SubAppContext = " + subAppContext + ", ContentSubAppView = " + view + ", BrowserPresenter = " + browser + ", EventBus = " + subAppEventBus);
        }
        this.browser = browser;
        this.subAppEventBus = subAppEventBus;
        this.actionExecutor = actionExecutor;
        this.componentProvider = componentProvider;
        this.workbenchRoot = ((BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor()).getWorkbench().getPath();
    }

    /**
     * Performs some routine tasks needed by all content subapps before the view is displayed.
     * The tasks are:
     * <ul>
     * <li>setting the current location
     * <li>setting the browser view
     * <li>restoring the browser status: see {@link #restoreBrowser(BrowserLocation)}
     * <li>calling {@link #onSubAppStart()} a hook-up method subclasses can override to perform additional work.
     * </ul>
     */
    @Override
    public final View start(final Location location) {
        BrowserLocation l = BrowserLocation.wrap(location);
        super.start(l);
        getView().setContentView(browser.start());
        restoreBrowser(l);
        registerSubAppEventsHandlers(subAppEventBus, this);

        return getView();
    }

    /**
     * Restores the browser status based on the information available in the location object. This is used e.g. when starting a subapp based on a
     * bookmark. I.e. given a bookmark containing the following URI fragment
     * <p>
     * {@code
     * #app:myapp:browser;/foo/bar:list
     * }
     * <p>
     * this method will select the path <code>/foo/bar</code> in the workspace used by the app, set the view type as <code>list</code> and finally update the available actions.
     * <p>
     * In case of a search view the URI fragment will look similar to the following one {@code
     * #app:myapp:browser;/:search:qux
     * }
     * <p>
     * then this method will select the root path, set the view type as <code>search</code>, perform a search for "qux" in the workspace used by the app and finally update the available actions.
     *
     * @see BrowserSubApp#updateActionbar(ActionbarPresenter)
     * @see BrowserSubApp#start(Location)
     * @see Location
     */
    protected final void restoreBrowser(final BrowserLocation location) {
        String path = ("/".equals(workbenchRoot) ? "" : workbenchRoot) + location.getNodePath();
        ViewType viewType = location.getViewType();
        if (viewType == null) {
            log.warn("ViewType did not match, returning default viewType.");

            viewType = getBrowser().getDefaultViewType();
            location.updateViewType(viewType);
            getAppContext().updateSubAppLocation(getSubAppContext(), location);
        }
        String query = location.getQuery();

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        final String workspaceName = subAppDescriptor.getWorkbench().getWorkspace();

        String itemId = null;
        try {
            itemId = JcrItemUtil.getItemId(SessionUtil.getNode(workspaceName, path));

            // MGNLUI-1475: item might have not been found if path doesn't exist
            if (itemId == null) {
                itemId = JcrItemUtil.getItemId(SessionUtil.getNode(workspaceName, workbenchRoot));
                BrowserLocation newLocation = getCurrentLocation();
                newLocation.updateNodePath("/");

                getAppContext().updateSubAppLocation(getSubAppContext(), newLocation);
            }
        } catch (RepositoryException e) {
            log.warn("Could not retrieve item at path {} in workspace {}", path, workspaceName);
        }
        List<String> ids = new ArrayList<String>();
        ids.add(itemId);
        getBrowser().resync(ids, viewType, query);
        updateActionbar(getBrowser().getActionbarPresenter());
    }


    /**
     * Show the actionPopup for the specified item at the specified coordinates.
     */
    public void showActionPopup(String absItemPath, int x, int y) {

        // If there's no actionbar configured we don't want to show an empty action popup
        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }

        ActionPopup actionPopup = browser.getView().getActionPopup();

        updateActionPopup(actionPopup);
        actionPopup.open(x, y);
    }

    /**
     * Update the items in the actionPopup based on the selected item and the ActionPopup availability configuration.
     * This method can be overriden to implement custom conditions diverging from {@link #updateActionbar(info.magnolia.ui.actionbar.ActionbarPresenter)}.
     */
    private void updateActionPopup(ActionPopup actionPopup) {

        actionPopup.removeAllItems();

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        WorkbenchDefinition workbench = subAppDescriptor.getWorkbench();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        List<ActionbarSectionDefinition> sections = actionbarDefinition.getSections();

        try {
            String workbenchRootItemId = JcrItemUtil.getItemId(workbench.getWorkspace(), workbench.getPath());
            List<String> selectedItemIds = getBrowser().getSelectedItemIds();
            List<Item> items = getJcrItemsExceptOne(workbench.getWorkspace(), selectedItemIds, workbenchRootItemId);

            // Figure out which section to show, only one
            ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, items);

            // If there no section matched the selection we just hide everything
            if (sectionDefinition == null) {
                return;
            }

            // Evaluate availability of each action within the section
            ContextMenu.ContextMenuItem menuItem = null;
            for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
                for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                    String actionName = itemDefinition.getName();
                    menuItem = addActionPopupItem(subAppDescriptor, actionPopup, itemDefinition, items);
                    menuItem.setEnabled(actionExecutor.isAvailable(actionName, items.toArray(new Item[items.size()])));
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
     * Add an additional menu item on the actionPopup.
     * TODO: Move to BrowserPresenter. Christopher Zimmermann
     */
    private ContextMenu.ContextMenuItem addActionPopupItem(BrowserSubAppDescriptor subAppDescriptor, ActionPopup actionPopup, ActionbarItemDefinition itemDefinition, List<javax.jcr.Item> items) {
        String actionName = itemDefinition.getName();

        ActionDefinition action = subAppDescriptor.getActions().get(actionName);
        String label = action.getLabel();
        String iconFontCode = ActionPopup.ICON_FONT_CODE + action.getIcon();
        ExternalResource iconFontResource = new ExternalResource(iconFontCode);
        ContextMenu.ContextMenuItem menuItem = actionPopup.addItem(label, iconFontResource);
        // Set data variable so that the event handler can determine which action to launch.
        menuItem.setData(actionName);

        return menuItem;
    }

    /**
     * Update the items in the actionbar based on the selected item and the action availability configuration.
     * This method can be overriden to implement custom conditions diverging from {@link #updateActionPopup(info.magnolia.ui.vaadin.actionbar.ActionPopup)}.
     *
     * @see #restoreBrowser(BrowserLocation)
     * @see #locationChanged(Location)
     * @see ActionbarPresenter
     */
    public void updateActionbar(ActionbarPresenter actionbar) {

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        WorkbenchDefinition workbench = subAppDescriptor.getWorkbench();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        List<ActionbarSectionDefinition> sections = actionbarDefinition.getSections();

        try {
            String workbenchRootItemId = JcrItemUtil.getItemId(workbench.getWorkspace(), workbench.getPath());
            List<String> selectedItemIds = getBrowser().getSelectedItemIds();
            List<Item> items = getJcrItemsExceptOne(workbench.getWorkspace(), selectedItemIds, workbenchRootItemId);

            // Figure out which section to show, only one
            ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, items);

            // If there no section matched the selection we just hide everything
            if (sectionDefinition == null) {
                for (ActionbarSectionDefinition section : sections) {
                    actionbar.hideSection(section.getName());
                }
                return;
            }

            // Hide all other sections
            for (ActionbarSectionDefinition section : sections) {
                if (section != sectionDefinition) {
                    actionbar.hideSection(section.getName());
                }
            }

            // Show our section
            actionbar.showSection(sectionDefinition.getName());

            // Evaluate availability of each action within the section
            for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
                for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                    String actionName = itemDefinition.getName();
                    if (actionExecutor.isAvailable(actionName, items.toArray(new Item[items.size()]))) {
                        actionbar.enable(actionName);
                    } else {
                        actionbar.disable(actionName);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Failed to updated actionbar", e);
            for (ActionbarSectionDefinition section : sections) {
                actionbar.hideSection(section.getName());
            }
        }
    }

    private ActionbarSectionDefinition getVisibleSection(List<ActionbarSectionDefinition> sections, List<Item> items) throws RepositoryException {
        for (ActionbarSectionDefinition section : sections) {
            if (isSectionVisible(section, items))
                return section;
        }
        return null;
    }

    private boolean isSectionVisible(ActionbarSectionDefinition section, List<Item> items) throws RepositoryException {
        AvailabilityDefinition availability = section.getAvailability();

        // Validate that the user has all required roles - only once
        if (!availability.getAccess().hasAccess(MgnlContext.getUser())) {
            return false;
        }

        // section must be visible for all items
        for (Item item : items) {
            if (!isSectionVisible(section, item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSectionVisible(ActionbarSectionDefinition section, Item item) throws RepositoryException {
        AvailabilityDefinition availability = section.getAvailability();

        // if a rule class is set, verify it first
        if ((availability.getRuleClass() != null)) {
            // if the rule class cannot be instantiated, or the rule returns false
            AvailabilityRule rule = componentProvider.newInstance(availability.getRuleClass());
            if (rule == null || !rule.isAvailable(item)) {
                return false;
            }
        }

        // If this is the root item we display the section only if the root property is set
        if (item == null) {
            return availability.isRoot();
        }

        // If its a property we display it only if the properties property is set
        if (!item.isNode()) {
            return availability.isProperties();
        }

        // If node is selected and the section is available for nodes
        if (availability.isNodes()) {
            // if no node type defined, the for all node types
            if (availability.getNodeTypes().isEmpty()) {
                return true;
            }
            // else the node must match at least one of the configured node types
            for (String nodeType : availability.getNodeTypes()) {
                if (NodeUtil.isNodeType((Node) item, nodeType)) {
                    return true;
                }
            }

        }
        return false;
    }

    protected final BrowserPresenter getBrowser() {
        return browser;
    }

    @Override
    public final ContentSubAppView getView() {
        return (ContentSubAppView) super.getView();
    }

    /**
     * The default implementation selects the path in the current workspace and updates the available actions in the actionbar.
     */
    @Override
    public void locationChanged(final Location location) {
        super.locationChanged(location);
        restoreBrowser(getCurrentLocation());
    }

    /**
     * Wraps the current DefaultLocation in a {@link BrowserLocation}. Providing getter and setters for used parameters.
     */
    @Override
    public BrowserLocation getCurrentLocation() {
        return BrowserLocation.wrap(super.getCurrentLocation());
    }

    /*
     * Registers general purpose handlers for the following events:
     * <ul>
     * <li> {@link ItemSelectedEvent}
     * <li> {@link ViewTypeChangedEvent}
     * <li> {@link SearchEvent}
     * </ul>
     */
    private void registerSubAppEventsHandlers(final EventBus subAppEventBus, final BrowserSubApp subApp) {
        final ActionbarPresenter actionbar = subApp.getBrowser().getActionbarPresenter();
        subAppEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                BrowserLocation location = getCurrentLocation();
                try {
                    Item selected = JcrItemUtil.getJcrItem(event.getWorkspace(), JcrItemUtil.parseNodeIdentifier(event.getFirstItemId()));
                    location.updateNodePath(StringUtils.removeStart(selected.getPath(), "/".equals(workbenchRoot) ? "" : workbenchRoot));
                } catch (RepositoryException e) {
                    log.warn("Could not get jcrItem with itemId " + event.getFirstItemId() + " from workspace " + event.getWorkspace(), e);
                }
                getAppContext().updateSubAppLocation(getSubAppContext(), location);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(ItemRightClickedEvent.class, new ItemRightClickedEvent.Handler() {

            @Override
            public void onItemRightClicked(ItemRightClickedEvent event) {
                String absItemPath;
                try {
                    absItemPath = event.getItem().getJcrItem().getPath();

                    showActionPopup(absItemPath, event.getClickX(), event.getClickY());
                } catch (RepositoryException e) {
                    log.warn("Could not get jcrItem with itemId " + event.getItemId() + " from workspace " + event.getWorkspace(), e);
                }

            }
        });

        subAppEventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {

            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                BrowserLocation location = getCurrentLocation();
                // remove search term from fragment when switching back
                if (location.getViewType() == ViewType.SEARCH && event.getViewType() != ViewType.SEARCH) {
                    location.updateQuery("");
                }
                location.updateViewType(event.getViewType());
                getAppContext().updateSubAppLocation(getSubAppContext(), location);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                BrowserLocation location = getCurrentLocation();
                if (StringUtils.isNotBlank(event.getSearchExpression())) {
                    location.updateViewType(ViewType.SEARCH);
                }
                location.updateQuery(event.getSearchExpression());
                getAppContext().updateSubAppLocation(getSubAppContext(), location);
                updateActionbar(actionbar);
            }
        });
    }

    public static List<Item> getJcrItemsExceptOne(final String workspaceName, List<String> ids, String itemIdToExclude) {
        List<Item> items = JcrItemUtil.getJcrItems(workspaceName, ids);
        if (itemIdToExclude == null) {
            return items;
        }
        for (int i = 0; i < items.size(); i++) {
            try {
                if (itemIdToExclude.equals(JcrItemUtil.getItemId(items.get(i)))) {
                    items.set(i, null);
                }
            } catch (RepositoryException e) {
                log.debug("Cannot get item ID for item [{}].", items.get(i));
            }
        }
        return items;
    }
}
