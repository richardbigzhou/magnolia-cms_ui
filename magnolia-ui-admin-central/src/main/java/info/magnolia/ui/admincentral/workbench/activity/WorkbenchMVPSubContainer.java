/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.workbench.activity;


import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.ConfiguredJcrViewBuilder;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.admincentral.list.activity.ListActivity;
import info.magnolia.ui.admincentral.tree.activity.TreeActivity;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.workbench.place.WorkbenchPlace;
import info.magnolia.ui.framework.activity.AbstractMVPSubContainer;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchDefinitionRegistry;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The isolated MVP container for workspace editing.
 */
public class WorkbenchMVPSubContainer extends AbstractMVPSubContainer<WorkbenchActivity>{

    private WorkbenchPlace place;
    private WorkbenchDefinitionRegistry workbenchRegistry;
    private ConfiguredJcrViewBuilder configuredJcrViewBuilder;
    private PlaceController placeController;
    private Shell shell;
    private JcrViewBuilderProvider jcrViewBuilderProvider;

    public WorkbenchMVPSubContainer(WorkbenchPlace place, WorkbenchDefinitionRegistry workbenchRegistry, Shell shell, ComponentProvider componentProvider) {
        super("workbench-" + place.getWorkbenchName(), shell, componentProvider);
        this.place = place;
        this.workbenchRegistry = workbenchRegistry;
        this.jcrViewBuilderProvider =  componentProvider.getComponent(JcrViewBuilderProvider.class);
        this.configuredJcrViewBuilder = (ConfiguredJcrViewBuilder) jcrViewBuilderProvider.getBuilder();
        this.placeController = componentProvider.getComponent(PlaceController.class);
        this.shell = shell;
    }

    @Override
    protected Class<WorkbenchActivity> getActivityClass() {
        return WorkbenchActivity.class;
    }

    @Override
    protected Object[] getActivityParameters() {
        return new Object[]{place};
    }

    @Override
    protected ComponentProviderConfiguration configureComponentProvider() {


        // load the workbench specific configuration if existing
        final WorkbenchDefinition workbenchDefinition;
        try {
            workbenchDefinition = workbenchRegistry.get(place.getWorkbenchName());
        } catch (RegistrationException e) {
            throw new RuntimeException(e);
        }

        if (workbenchDefinition == null){
            throw new IllegalStateException("No definition could be found for workbench [" + place.getWorkbenchName() + "]");
        }

        ComponentProviderConfiguration componentProviderConfiguration = new ComponentProviderConfiguration();

        if (workbenchDefinition.getComponents() != null) {
            componentProviderConfiguration.combine(workbenchDefinition.getComponents());
        }

        //FIXME  workaround to provide MVPSubContainer with the right components correctly initialized.
        Map<String, Column<?>> columns = new LinkedHashMap<String, Column<?>>();
        for (AbstractColumnDefinition columnDefinition : workbenchDefinition.getColumns()) {
            Column<?> column = configuredJcrViewBuilder.createTreeColumn(columnDefinition);
            // only add if not null - null meaning there's no definitionToImplementationMapping defined for that column.
            if (column != null) {
                columns.put(columnDefinition.getName(), column);
            }
        }

        final TreeModel treeModel = new TreeModel(workbenchDefinition, columns);

        TreeActivity treeActivity = new TreeActivity(workbenchDefinition, jcrViewBuilderProvider, placeController, shell);
        ListActivity listActivity = new ListActivity(workbenchDefinition, jcrViewBuilderProvider, placeController, shell);

        componentProviderConfiguration.addComponent(InstanceConfiguration.valueOf(TreeModel.class, treeModel));
        componentProviderConfiguration.addComponent(InstanceConfiguration.valueOf(WorkbenchDefinition.class, workbenchDefinition));

        ItemListActivityMapper itemListActivityMapper = new ItemListActivityMapper(treeActivity, listActivity);
        componentProviderConfiguration.addComponent(InstanceConfiguration.valueOf(ItemListActivityMapper.class, itemListActivityMapper));

        return componentProviderConfiguration;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Place>[] getSupportedPlaces() {
        // Casts since generic array creation doesn't exist
        return new Class[] {ItemSelectedPlace.class};
    }

    @Override
    protected Place getDefaultPlace() {
        return new ItemSelectedPlace(place.getWorkbenchName(), "/", ViewType.TREE);
    }

}
