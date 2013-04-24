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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes.LastModified;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBus;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
public class BrowserPresenter implements ActionbarPresenter.Listener {

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

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                refreshActionbarPreviewImage(event.getPath(), event.getWorkspace());
                workbenchPresenter.selectPath(event.getPath());
                workbenchPresenter.refresh();
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
                actionbarPresenter.executeDefaultAction();
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

    /**
     * @see info.magnolia.ui.workbench.ContentPresenter#getSelectedItemPath()
     */
    public String getSelectedItemId() {
        return workbenchPresenter.getSelectedId();
    }

    /**
     * @return The configured default view Type.<br>
     *         If non define, return the first Content Definition as default.
     * 
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
    public void onExecute(String actionName) {
        try {
            Session session = MgnlContext.getJCRSession(getWorkspace());
            javax.jcr.Item item = session.getItem(getSelectedItemId());
            if (item.isNode()) {
                actionExecutor.execute(actionName, new JcrNodeAdapter((Node) item));
            } else {
                actionExecutor.execute(actionName, new JcrPropertyAdapter((Property) item));
            }
        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, "Could not get item: " + getSelectedItemId(), e.getMessage());
            log.error("", e);
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
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
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

}
