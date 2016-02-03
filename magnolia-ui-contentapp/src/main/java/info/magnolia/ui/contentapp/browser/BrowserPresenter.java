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
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
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

    private final EventBus admincentralEventBus;

    private final EventBus subAppEventBus;

    private final ActionbarPresenter actionbarPresenter;

    private final ImageProvider imageProvider;

    private final AppContext appContext;

    @Inject
    public BrowserPresenter(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final BrowserView view, @Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus,
            final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            final ActionbarPresenter actionbarPresenter, final ComponentProvider componentProvider, WorkbenchPresenter workbenchPresenter) {
        this.workbenchPresenter = workbenchPresenter;
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.admincentralEventBus = admincentralEventBus;
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
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (event.getWorkspace().equals(getWorkspace())) {

                    workbenchPresenter.refresh();
                    workbenchPresenter.select(getSelectedItemIds());

                    // use just the first item to show the preview image
                    String itemId = getSelectedItemIds().get(0);
                    try {
                        if (JcrItemUtil.itemExists(getWorkspace(), itemId)) {
                            refreshActionbarPreviewImage(itemId, event.getWorkspace());
                        }
                    } catch (RepositoryException e) {
                        log.warn("Unable to get node or property [{}] for preview image", itemId, e);
                    }
                }
            }
        });

        subAppEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                // if exactly one node is selected, use it for preview
                refreshActionbarPreviewImage(event.getFirstItemId(), event.getWorkspace());
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

    public List<String> getSelectedItemIds() {
        return workbenchPresenter.getSelectedIds();
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
     * Synchronizes the underlying view to reflect the status extracted from the Location token, i.e. selected itemId,
     * view type and optional query (in case of a search view).
     */
    public void resync(final List<String> itemIds, final ViewType viewType, final String query) {
        workbenchPresenter.resynch(itemIds, viewType, query);
    }

    private void refreshActionbarPreviewImage(final String itemId, final String workspace) {
        if (StringUtils.isBlank(itemId)) {
            actionbarPresenter.setPreview(null);
        } else {
            if (imageProvider != null) {
                Object previewResource = imageProvider.getThumbnailResourceById(workspace, itemId, ImageProvider.PORTRAIT_GENERATOR);
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

        // we support only JCR item adapters
        if (!(item instanceof JcrItemAdapter)) {
            return;
        }

        // don't save if no value changes occurred on adapter
        if (!((JcrItemAdapter) item).hasChangedProperties()) {
            return;
        }

        if (item instanceof AbstractJcrNodeAdapter) {
            // Saving JCR Node, getting updated node first
            AbstractJcrNodeAdapter nodeAdapter = (AbstractJcrNodeAdapter) item;
            try {
                // get modifications
                Node node = nodeAdapter.applyChanges();

                LastModified.update(node);
                node.getSession().save();
            } catch (RepositoryException e) {
                log.error("Could not save changes to node", e);
            }

        } else if (item instanceof JcrPropertyAdapter) {
            // Saving JCR Property, update it first
            JcrPropertyAdapter propertyAdapter = (JcrPropertyAdapter) item;
            try {
                // get parent first because once property is updated, it won't exist anymore if the name changes
                Node parent = propertyAdapter.getJcrItem().getParent();

                // get modifications
                propertyAdapter.applyChanges();

                LastModified.update(parent);
                parent.getSession().save();

                // update workbench selection in case the property changed name
                List<String> ids = new ArrayList<String>();
                ids.add(JcrItemUtil.getItemId(propertyAdapter.getJcrItem()));
                workbenchPresenter.select(ids);

            } catch (RepositoryException e) {
                log.error("Could not save changes to property", e);
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

    private void executeAction(String actionName) {
        try {
            // TODO MGNLUI-1515 support actions on more items
            // now, use just the first item
            javax.jcr.Item item = JcrItemUtil.getJcrItem(getWorkspace(), getSelectedItemIds().get(0));
            actionExecutor.execute(actionName, item.isNode() ? new JcrNodeAdapter((Node) item) : new JcrPropertyAdapter((Property) item));
        } catch (PathNotFoundException p) {
            Message error = new Message(MessageType.ERROR, "Could not get item ", "Following Item not found :" + getSelectedItemIds().get(0));
            appContext.sendLocalMessage(error);
        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, "Could not get item: " + getSelectedItemIds().get(0), e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            appContext.sendLocalMessage(error);
        }
    }



    // /**
    // * TODO: Eliminate redundancy with BrowserSubApp.updateActionBar (MGNLUI-1367) Christopher Zimmermann.
    // */
    // private ActionbarSectionDefinition getVisibleSection(List<ActionbarSectionDefinition> sections, javax.jcr.Item item) throws RepositoryException {
    // for (ActionbarSectionDefinition section : sections) {
    // if (isSectionVisible(section, item))
    // return section;
    // }
    // return null;
    // }
    //
    // /**
    // * TODO: Eliminate redundancy with BrowserSubApp.updateActionBar (MGNLUI-1367) Christopher Zimmermann.
    // */
    // private boolean isSectionVisible(ActionbarSectionDefinition section, javax.jcr.Item item) throws RepositoryException {
    // SectionRestrictionsDefinition restrictions = section.getRestrictions();
    //
    // // If this is the root item we display the section only if the root property is set
    // if (item == null)
    // return restrictions.isRoot();
    //
    // // If its a property we display it only if the properties property is set
    // if (!item.isNode())
    // return restrictions.isProperties();
    //
    // // The node must match at least one of the configured node types
    // for (String nodeType : restrictions.getNodeTypes()) {
    // if (NodeUtil.isNodeType((Node) item, nodeType))
    // return true;
    // }
    // return false;
    // }

}
