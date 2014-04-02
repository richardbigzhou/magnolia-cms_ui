/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.formdialog.FormBuilder;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.util.BeanItem;

/**
 * The item detail presenter. A base abstract class for specific pulse item presenters.
 * 
 * @param <T> the type of pulse item, e.g. Message, Task, etc.
 */
public abstract class ItemPresenter<T> implements ItemView.Listener, ActionbarPresenter.Listener {

    private final ItemView view;
    private ItemActionExecutor itemActionExecutor;
    private ItemViewDefinitionRegistry itemViewDefinitionRegistry;
    private FormBuilder formbuilder;
    private ActionbarPresenter actionbarPresenter;
    private AvailabilityChecker availabilityChecker;
    private Listener listener;
    private T item;
    private I18nizer i18nizer;

    @Inject
    public ItemPresenter(ItemView view, ItemActionExecutor itemActionExecutor, AvailabilityChecker availabilityChecker, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer) {
        this.view = view;
        this.itemActionExecutor = itemActionExecutor;
        this.itemViewDefinitionRegistry = itemViewDefinitionRegistry;
        this.formbuilder = formbuilder;
        this.actionbarPresenter = actionbarPresenter;
        this.availabilityChecker = availabilityChecker;
        this.i18nizer = i18nizer;

        view.setListener(this);
        actionbarPresenter.setListener(this);
    }

    public final View start(String itemId) {
        this.item = getPulseItemById(itemId);
        if (this.item == null) {
            throw new RuntimeException("Could not retrieve pulse item with id [" + itemId + "]");
        }
        String itemView = "ui-admincentral:default";

        setItemViewTitle(item, view);
        try {
            final String specificItemView = getItemViewName(item);
            if (StringUtils.isNotEmpty(specificItemView)) {
                itemView = specificItemView;
            }
            ItemViewDefinition itemViewDefinition = itemViewDefinitionRegistry.get(itemView);
            itemViewDefinition = i18nizer.decorate(itemViewDefinition);

            itemActionExecutor.setMessageViewDefinition(itemViewDefinition);

            View mView = formbuilder.buildView(itemViewDefinition.getForm(), asBeanItem(item));
            view.setItemView(mView);
            for (Entry<String, ActionDefinition> entry : itemViewDefinition.getActions().entrySet()) {
                final String actionName = entry.getValue().getName();
                AvailabilityDefinition availability = itemActionExecutor.getActionDefinition(actionName).getAvailability();
                if (availabilityChecker.isAvailable(availability, Arrays.asList(new Object[] { itemId }))) {
                    actionbarPresenter.enable(actionName);
                } else {
                    actionbarPresenter.disable(actionName);
                }
            }

            view.setActionbarView(actionbarPresenter.start(itemViewDefinition.getActionbar(), itemViewDefinition.getActions()));
        } catch (RegistrationException e) {
            throw new RuntimeException("Could not retrieve itemView for " + itemView, e);
        }
        return view;
    }

    protected abstract String getItemViewName(T item);

    protected abstract void setItemViewTitle(T item, ItemView view);

    protected abstract T getPulseItemById(String itemId);

    protected abstract BeanItem<T> asBeanItem(T item);

    @Override
    public void onNavigateToList() {
        listener.showList();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        try {
            itemActionExecutor.execute(actionName, item, this, itemActionExecutor);

        } catch (ActionExecutionException e) {
            throw new RuntimeException("Could not execute action " + actionName, e);
        }
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        void showList();
    }
}
