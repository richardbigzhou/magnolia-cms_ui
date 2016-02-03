/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * The SaveItemPropertyAction is a basic action for saving changes made with inplace-editing in the Configuration app.
 */
public class SaveItemPropertyAction extends AbstractAction<SaveItemPropertyActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveItemPropertyAction.class);
    private final EventBus eventBus;
    private final ContentConnector contentConnector;
    private final Object itemId;
    private final Object propertyId;
    private final Property<?> propertyDataSource;

    @Inject
    public SaveItemPropertyAction(SaveItemPropertyActionDefinition definition, @Named(SubAppEventBus.NAME) EventBus eventBus, ContentConnector contentConnector, Object... args) {
        super(definition);
        this.eventBus = eventBus;
        this.contentConnector = contentConnector;
        this.itemId = ((Set<Object>) args[0]).iterator().next();
        this.propertyId = args[1];
        this.propertyDataSource = (Property<?>) args[2];
    }

    @Override
    public void execute() throws ActionExecutionException {

        Item item = contentConnector.getItem(itemId);
        // we support only JCR item adapters
        if (!(item instanceof JcrItemAdapter)) {
            return;
        }

        Property itemProperty = item.getItemProperty(propertyId);
        itemProperty.setValue(propertyDataSource.getValue());

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
                JcrPropertyItemId newItemId = propertyAdapter.getItemId();
                eventBus.fireEvent(new SelectionChangedEvent(new HashSet<Object>(Arrays.asList(newItemId))));

            } catch (RepositoryException e) {
                log.error("Could not save changes to property", e);
            }
        }
    }

}
