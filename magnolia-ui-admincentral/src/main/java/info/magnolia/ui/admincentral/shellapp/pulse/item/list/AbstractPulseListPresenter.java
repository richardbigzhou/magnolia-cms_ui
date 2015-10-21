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
package info.magnolia.ui.admincentral.shellapp.pulse.item.list;

import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ConfiguredPulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.PulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer.PulseListFooterPresenter;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.view.View;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract presenter for items displayed in pulse.
 */
public abstract class AbstractPulseListPresenter implements PulseListPresenter, PulseDetailPresenter.Listener, PulseListView.Listener, PulseListFooterPresenter.Listener {

    private static final Logger log = LoggerFactory.getLogger(AbstractPulseListPresenter.class);

    protected final PulseListContainer container;
    protected final AvailabilityChecker availabilityChecker;
    protected final PulseListActionExecutor actionExecutor;
    protected final PulseListFooterPresenter pulseListFooterPresenter;
    protected PulseListDefinition definition;

    protected Listener listener;

    /**
     * @deprecated since 5.4.3, use {@link AbstractPulseListPresenter#AbstractPulseListPresenter(PulseListContainer, ConfiguredPulseListDefinition, AvailabilityChecker, PulseListActionExecutor, PulseListFooterPresenter)} instead.
     */
    @Deprecated
    protected AbstractPulseListPresenter(PulseListContainer container) {
        this.container = container;
        this.availabilityChecker = Components.getComponent(AvailabilityChecker.class);
        this.actionExecutor = Components.getComponent(PulseListActionExecutor.class);
        this.pulseListFooterPresenter = Components.getComponent(PulseListFooterPresenter.class);

        log.warn("You are using a deprecated not compatible constructor. Please update your implementation to use the new one!");
    }

    protected AbstractPulseListPresenter(PulseListContainer container, ConfiguredPulseListDefinition definition,
            AvailabilityChecker availabilityChecker, PulseListActionExecutor pulseListActionExecutor, PulseListFooterPresenter pulseListFooterPresenter) {
        this.container = container;
        this.availabilityChecker = availabilityChecker;
        this.actionExecutor = pulseListActionExecutor;
        this.pulseListFooterPresenter = pulseListFooterPresenter;
        this.definition = definition;

        this.actionExecutor.setActionsDefinition(definition.getBulkActions());
        this.pulseListFooterPresenter.setListener(this);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void showList() {
        container.refresh();
        listener.showList();
    }

    @Override
    public void setGrouping(boolean checked) {
        container.setGrouping(checked);
    }

    @Override
    public void filterByItemCategory(PulseItemCategory category) {
        container.filterByItemCategory(category);
    }

    @Override
    public abstract PulseItemCategory getCategory();

    @Override
    public abstract View openItem(String itemId) throws RegistrationException;

    @Override
    public abstract int getPendingItemCount();

    protected abstract List<Object> getSelectedItemIds();

    @Override
    public void onSelectionChanged(Set<String> itemIds) {
        pulseListFooterPresenter.updateStatus(getTotalEntriesAmount(), itemIds.size());
        // Evaluate availability of each action within the section
        for (ActionDefinition actionDefinition : definition.getBulkActions()) {
            AvailabilityDefinition availability = actionDefinition.getAvailability();
            pulseListFooterPresenter.toggleActionEnable(actionDefinition.getName(), availabilityChecker.isAvailable(availability, getSelectedItemIds()));
        }

    }

    @Override
    public void onItemSetChanged(long totalEntriesAmount) {
        if (pulseListFooterPresenter != null) {
            pulseListFooterPresenter.updateStatus(getTotalEntriesAmount(), 0);
        }
    }

    @Override
    public void onBulkActionItemClicked(String itemName) {
        executeAction(itemName);
    }

    protected void executeAction(String actionName) {
        try {
            AvailabilityDefinition availability = actionExecutor.getActionDefinition(actionName).getAvailability();
            if (availabilityChecker.isAvailable(availability, getSelectedItemIds())) {
                actionExecutor.execute(actionName, new Object[]{getSelectedItemIds()});
            }
        } catch (ActionExecutionException e) {
            log.error("An error occurred while executing action [{}]", actionName, e);
        }
    }

}
