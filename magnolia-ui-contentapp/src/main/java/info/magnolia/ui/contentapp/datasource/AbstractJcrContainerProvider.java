/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.contentapp.datasource;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.datasource.ContainerConfiguration;
import info.magnolia.ui.vaadin.integration.datasource.ContainerProvider;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract implementation of {@link ContainerProvider} for JCR-based containers.
 * @param <T> Type of JCR-based Vaadin container.
 */
public class AbstractJcrContainerProvider<T extends AbstractJcrContainer> implements ContainerProvider<T, ContainerConfiguration> {

    private JcrDataSource dataSource;

    private Class<T> clazz;

    private ComponentProvider componentProvider;


    public AbstractJcrContainerProvider(JcrDataSource dataSource, Class<T> clazz, ComponentProvider componentProvider) {
        this.dataSource = dataSource;
        this.clazz = clazz;
        this.componentProvider = componentProvider;
    }

    @Override
    public T createContainer(ContainerConfiguration configuration) {
        T c = doCreateJcrContainer(dataSource.getWorkbenchDefinition());
        configureContainer(c, configuration);
        return c;
    }

    protected T doCreateJcrContainer(WorkbenchDefinition workbenchDefinition) {
        return componentProvider.newInstance(clazz, workbenchDefinition);
    }

    protected void configureContainer(AbstractJcrContainer c, ContainerConfiguration config) {
        Iterator<Map.Entry<Object, Object>> entryIt = config.getPropertyTypes().entrySet().iterator();
        while (entryIt.hasNext()) {
            Map.Entry<Object, Object> entry = entryIt.next();
            c.addContainerProperty(entry.getKey(), (Class)entry.getValue(), null);
        }

        for (Object sortableProperty : config.getSortableProperties()) {
            c.addSortableProperty(String.valueOf(sortableProperty));
        }
    }
}
