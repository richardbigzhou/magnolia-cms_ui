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

import info.magnolia.event.EventBus;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.ActionbarView;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.event.ActionEvent;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemShortcutKeyEvent;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction;
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

    private final Predicate<Object> itemExistsPredicate = new Predicate<Object>() {
        @Override
        public boolean evaluate(final Object object) {
            return verifyItemExists(object);
        }
    };

    private final WorkbenchPresenter workbenchPresenter;

    private final ActionExecutor actionExecutor;

    private final BrowserSubAppDescriptor subAppDescriptor;

    private final BrowserView view;

    private final EventBus admincentralEventBus;

    private final EventBus subAppEventBus;

    private final ActionbarPresenter actionbarPresenter;

    private final AvailabilityChecker availabilityChecker;

    private final AppContext appContext;

    private final ContentConnector contentConnector;

    private final ImageProvider imageProvider;

    @Inject
    public BrowserPresenter(
            BrowserView view,
            SubAppContext subAppContext,
            ActionExecutor actionExecutor,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
            @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            ContentConnector contentConnector,
            ImageProvider imageProvider,
            WorkbenchPresenter workbenchPresenter,
            ActionbarPresenter actionbarPresenter,
            AvailabilityChecker availabilityChecker) {
        this.view = view;
        this.appContext = subAppContext.getAppContext();
        this.subAppDescriptor = (BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor();
        this.actionExecutor = actionExecutor;
        this.admincentralEventBus = admincentralEventBus;
        this.subAppEventBus = subAppEventBus;
        this.contentConnector = contentConnector;
        this.imageProvider = imageProvider;
        this.workbenchPresenter = workbenchPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.availabilityChecker = availabilityChecker;
    }

    public BrowserView start() {
        actionbarPresenter.setListener(this);

        WorkbenchView workbenchView = workbenchPresenter.start(subAppDescriptor.getWorkbench(), subAppDescriptor.getImageProvider(), subAppEventBus);
        ActionbarView actionbar = actionbarPresenter.start(subAppDescriptor.getActionbar(), subAppDescriptor.getActions());

        view.setWorkbenchView(workbenchView);
        view.setActionbarView(actionbar);
        view.setListener(this);

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                List<Object> itemIds = new ArrayList<>();
                if (event.getItemId() instanceof Collection) {
                    for (Object itemId : (Collection<?>) event.getItemId()) {
                        itemIds.add(itemId);
                    }
                } else {
                    itemIds.add(event.getItemId());
                }
                workbenchPresenter.refresh();
                // filter out items that can't be handled or doesn't exist
                // if an item passed in the event exists, mark it as selected (see MGNLUI-2919)
                // otherwise preserve previous selection
                List<Object> existingSelectedItemIds = ListUtils.select(itemIds, itemExistsPredicate);
                if (existingSelectedItemIds.isEmpty()) {
                    existingSelectedItemIds = ListUtils.select(getSelectedItemIds(), itemExistsPredicate);
                }

                workbenchPresenter.select(existingSelectedItemIds);

                if (event.isItemContentChanged() && !existingSelectedItemIds.isEmpty()) {
                    workbenchPresenter.expand(existingSelectedItemIds.get(0));
                }

                // use just the first selected item to show the preview image
                if (!existingSelectedItemIds.isEmpty()) {
                    refreshActionbarPreviewImage(existingSelectedItemIds.get(0));
                }
            }
        });

        subAppEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                // if exactly one node is selected, use it for preview
                refreshActionbarPreviewImage(event.getFirstItemId());
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

        subAppEventBus.addHandler(ActionEvent.class, new ActionEvent.Handler() {

            @Override
            public void onAction(ActionEvent event) {
                executeAction(event.getActionName(), event.getItemIds(), event.getParameters());
            }
        });

        subAppEventBus.addHandler(ItemShortcutKeyEvent.class, new ItemShortcutKeyEvent.Handler() {

            @Override
            public void onItemShortcutKeyEvent(ItemShortcutKeyEvent event) {
                int keyCode = event.getKeyCode();
                switch (keyCode) {
                case ShortcutAction.KeyCode.ENTER:
                    executeDefaultAction();
                    break;
                case ShortcutAction.KeyCode.DELETE:
                    executeDeleteAction();
                    break;
                }

            }
        });
    }

    private void refreshActionbarPreviewImage(Object itemId) {
        Object previewResource = getPreviewImageForId(itemId);
        if (previewResource instanceof Resource) {
            getActionbarPresenter().setPreview((Resource) previewResource);
        } else {
            getActionbarPresenter().setPreview(null);
        }
    }

    protected boolean verifyItemExists(Object itemId) {
        return contentConnector.canHandleItem(itemId) && contentConnector.getItem(itemId) != null;
    }

    public List<Object> getSelectedItemIds() {
        return workbenchPresenter.getSelectedIds();
    }

    /**
     * @return The configured default view Type.<br>
     * If non define, return the first Content Definition as default.
     */
    public String getDefaultViewType() {
        return workbenchPresenter.getDefaultViewType();
    }

    public boolean hasViewType(String viewType) {
        return workbenchPresenter.hasViewType(viewType);
    }

    public BrowserView getView() {
        return view;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    /**
     * Synchronizes the underlying view to reflect the status extracted from the Location token, i.e. selected itemId,
     * view type and optional query (in case of a search view).
     */
    public void resync(final List<Object> itemIds, final String viewType, final String query) {
        workbenchPresenter.resynch(itemIds, viewType, query);
    }

    protected Object getPreviewImageForId(Object itemId) {
        if (imageProvider != null) {
            return imageProvider.getThumbnailResource(itemId, ImageProvider.PORTRAIT_GENERATOR);
        }
        return null;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        executeAction(actionName);
    }

    @Override
    public void onActionBarSelection(String actionName) {
        executeAction(actionName);
    }

    /**
     * Executes the default action, as configured in the {@link info.magnolia.ui.actionbar.definition.ActionbarDefinition}.
     */
    private void executeDefaultAction() {
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        String defaultAction = actionbarDefinition.getDefaultAction();
        if (StringUtils.isNotEmpty(defaultAction)) {
            executeAction(defaultAction);
        }
    }

    /**
     * Executes the default delete action, as configured in the {@link info.magnolia.ui.actionbar.definition.ActionbarDefinition}.
     */
    private void executeDeleteAction() {
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        String deleteAction = actionbarDefinition.getDeleteAction();
        if (StringUtils.isNotEmpty(deleteAction)) {
            executeAction(deleteAction);
        }
    }

    private void executeAction(String actionName) {
        try {
            AvailabilityDefinition availability = actionExecutor.getActionDefinition(actionName).getAvailability();
            if (availabilityChecker.isAvailable(availability, getSelectedItemIds())) {
                Object[] args = prepareActionArgs();
                actionExecutor.execute(actionName, args);
            }
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }
    }

    private void executeAction(String actionName, Set<Object> itemIds, Object... parameters) {
        try {
            AvailabilityDefinition availability = actionExecutor.getActionDefinition(actionName).getAvailability();
            if (availabilityChecker.isAvailable(availability, getSelectedItemIds())) {
                List<Object> args = new ArrayList<Object>();
                args.add(itemIds);
                args.addAll(Arrays.asList(parameters));
                actionExecutor.execute(actionName, new Object[] { args.toArray(new Object[args.size()]) });
            }
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }
    }

    protected Object[] prepareActionArgs() {
        List<Object> argList = new ArrayList<Object>();
        List<Item> selectedItems = new ArrayList<Item>();

        List<Object> selectedIds = getSelectedItemIds();
        if (selectedIds.isEmpty()) {
            selectedIds.add(contentConnector.getDefaultItemId());
        }
        Iterator<Object> idIt = selectedIds.iterator();
        while (idIt.hasNext()) {
            selectedItems.add(contentConnector.getItem(idIt.next()));
        }

        if (selectedItems.size() <= 1) {
            // we have a simple selection; action implementation may expect either an item, either a list parameter, so we have to support both.
            argList.add(selectedItems.isEmpty() ? new NullItem() : selectedItems.get(0));
            argList.add(selectedItems);
        } else {
            // we have a multiple selection; action implementation must support a list parameter, no way around that.
            argList.add(selectedItems);
        }
        return argList.toArray(new Object[argList.size()]);
    }
}
