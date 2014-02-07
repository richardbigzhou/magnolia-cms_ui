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

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.event.ItemEditedEvent;

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

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 31/01/14
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class BrowserPresenter extends BrowserPresenterBase {

    private Logger log = LoggerFactory.getLogger(getClass());

    private final ImageProvider imageProvider;

    @Inject
    public BrowserPresenter(ActionExecutor actionExecutor, SubAppContext subAppContext, BrowserView view, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, @Named(SubAppEventBus.NAME) EventBus subAppEventBus, ActionbarPresenter actionbarPresenter, ComponentProvider componentProvider, WorkbenchPresenter workbenchPresenter) {
        super(actionExecutor, subAppContext, view, admincentralEventBus, subAppEventBus, actionbarPresenter, workbenchPresenter);
        ImageProviderDefinition imageProviderDefinition = ((BrowserSubAppDescriptor) subAppContext.getSubAppDescriptor()).getImageProvider();
        if (imageProviderDefinition == null) {
            this.imageProvider = null;
        } else {
            this.imageProvider = componentProvider.newInstance(imageProviderDefinition.getImageProviderClass(), imageProviderDefinition);
        }
    }

    @Override
    protected void editItem(ItemEditedEvent event) {
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
                parent.getSession().save();

                // update workbench selection in case the property changed name
                List<String> ids = new ArrayList<String>();
                ids.add(JcrItemUtil.getItemId(propertyAdapter.getJcrItem()));
                getWorkbenchPresenter().select(ids);

            } catch (RepositoryException e) {
                log.error("Could not save changes to property", e);
            }
        }
    }

    @Override
    public List<Item> getSelectedItems() {
        List<Item> items = new ArrayList<Item>(getSelectedItemIds().size());
        List<javax.jcr.Item> jcrItems = new ArrayList<javax.jcr.Item>(getSelectedItemIds().size());
        for (Object itemId : getSelectedItemIds()) {
            try {
                javax.jcr.Item item = JcrItemUtil.getJcrItem(getWorkspace(), String.valueOf(itemId));
                jcrItems.add(item);
                JcrItemAdapter adapter = item.isNode() ? new JcrNodeAdapter((Node) item) : new JcrPropertyAdapter((Property) item);
                items.add(adapter);
            } catch (PathNotFoundException p) {
                Message error = new Message(MessageType.ERROR, "Could not get item ", "Following Item not found :" + getSelectedItemIds().get(0));
                getAppContext().sendLocalMessage(error);
            } catch (RepositoryException e) {
                Message error = new Message(MessageType.ERROR, "Could not get item: " + getSelectedItemIds().get(0), e.getMessage());
                //log.error("An error occurred while executing action [{}]", actionName, e);
                getAppContext().sendLocalMessage(error);
            }
        }
        return items;
    }

    @Override
    protected boolean verifyItemExists(Object itemId) {
        try {
            return JcrItemUtil.itemExists(getWorkspace(), String.valueOf(itemId));
        } catch (RepositoryException e) {
            log.warn("Unable to get node or property [{}] for preview image", itemId, e);
            return false;
        }
    }

    @Override
    protected Object getPreviewImageForId(Object itemId) {
        if (StringUtils.isBlank(String.valueOf(itemId))) {
            return null;
        } else {
            if (imageProvider != null) {
                return imageProvider.getThumbnailResourceById(getWorkbenchPresenter().getWorkspace(), String.valueOf(itemId), ImageProvider.PORTRAIT_GENERATOR);
            }
        }
        return null;
    }

}


