/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.data;

import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListContainer;

import javax.inject.Provider;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * Foundation {@link LazyQueryContainer}-based implementation of {@link PulseListContainer}.
 *
 * @param <ET> enumeration type of entities.
 * @param <DT> query definition type
 * @param <FT> query factory type
 */
public abstract class LazyPulseListContainer<ET, DT extends PulseQueryDefinition<ET>, FT extends QueryFactory> implements PulseListContainer {

    private LazyQueryContainer container;

    private final DT queryDefinition;

    private final Provider<FT> queryFactoryProvider;

    private final String userName;

    protected LazyPulseListContainer(DT queryDefinition, Provider<FT> queryFactoryProvider, String userName) {
        this.queryDefinition = queryDefinition;
        this.queryFactoryProvider = queryFactoryProvider;
        this.userName = userName;
        this.queryDefinition.setUserName(userName);
    }

    @SuppressWarnings("unchecked")
    protected PulseQueryDefinition<ET> getQueryDefinition() {
        return (PulseQueryDefinition<ET>) getVaadinContainer().getQueryView().getQueryDefinition();
    }

    @Override
    public void setGrouping(boolean isGrouping) {
        PulseQueryDefinition<?> definition = getQueryDefinition();
        if (definition.isGroupingByType() != isGrouping) {
            definition.setGroupingByType(isGrouping);
            getVaadinContainer().refresh();
        }
    }

    @Override
    public LazyQueryContainer getVaadinContainer() {
        if (this.container == null) {
            this.container = initLazyQueryContainer();
        }
        return this.container;
    }

    @Override
    public void refresh() {
        getVaadinContainer().refresh();
    }

    protected LazyQueryContainer initLazyQueryContainer() {
        return new LazyQueryContainer(queryDefinition, queryFactoryProvider.get());
    }

    protected final String getUserName() {
        return userName;
    }
}
