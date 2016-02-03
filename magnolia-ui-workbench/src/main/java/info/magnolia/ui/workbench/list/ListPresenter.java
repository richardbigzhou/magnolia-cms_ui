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
package info.magnolia.ui.workbench.list;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.workbench.AbstractContentPresenter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.container.Refreshable;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Container;

/**
 * The ListPresenter is responsible for creating, configuring and updating a list of items according to the workbench definition.
 */
public class ListPresenter extends AbstractContentPresenter implements ListView.Listener {

    protected final ListView view;

    @Inject
    public ListPresenter(final ListView view, final ComponentProvider componentProvider) {
        super(componentProvider);
        this.view = view;
    }

    @Override
    public ListView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName, ContentConnector contentConnector) {
        super.start(workbenchDefinition, eventBus, viewTypeName, contentConnector);
        this.container = initializeContainer();
        view.setListener(this);
        view.setContainer(container);
        view.clearColumns();

        // build columns
        Iterator<ColumnDefinition> it = getColumnsIterator();

        while (it.hasNext()) {
            ColumnDefinition column = it.next();

            if (workbenchDefinition.isDialogWorkbench() && !column.isDisplayInChooseDialog()) {
                continue;
            }

            String propertyId = column.getPropertyName();
            String title = column.getLabel();

            if (column.getWidth() > 0) {
                view.addColumn(propertyId, title, column.getWidth());
            } else if (column.getExpandRatio() > 0) {
                view.addColumn(propertyId, title, column.getExpandRatio());
            } else {
                view.addColumn(propertyId, column.getLabel());
            }

            if (column.getFormatterClass() != null) {
                view.setColumnFormatter(propertyId, getComponentProvider().newInstance(column.getFormatterClass(), column));
            }
        }

        return view;
    }

    @Override
    public void select(List<Object> itemIds) {
        List<Object> objectIds = new ArrayList<Object>();
        for (Object itemId : itemIds) {
            // the view (Vaadin table) is not interested in default selection
            if (!itemId.equals(contentConnector.getDefaultItemId())) {
                objectIds.add(itemId);
            }
        }
        view.select(objectIds);
    }

    @Override
    public void refresh() {
        super.refresh();
        // This will update the row count and display the newly created items.
        if (container instanceof Refreshable) {
            ((Refreshable) container).refresh();
        }
    }

    @Override
    protected Container initializeContainer() {
        Container container = createContainer();
        configureContainer(getPresenterDefinition(), container);
        return container;
    }

    protected Container createContainer() {
        return new FlatJcrContainer(((JcrContentConnector) contentConnector).getContentConnectorDefinition());
    }

    protected void configureContainer(ContentPresenterDefinition presenterDefinition, Container container) {
        for (ColumnDefinition column : presenterDefinition.getColumns()) {
            String propertyId = column.getPropertyName();
            container.addContainerProperty(propertyId, column.getType(), null);
            // TODO Rely on Container.Sortable instead.
            // Containers thus need to be created with their propertyIds and sortablePropertyIds (Container.Sortable only exposes #getSortableContainerPropertyIds())
            if (container instanceof AbstractJcrContainer) {
                ((AbstractJcrContainer) container).addSortableProperty(propertyId);
            }
        }
    }
}
