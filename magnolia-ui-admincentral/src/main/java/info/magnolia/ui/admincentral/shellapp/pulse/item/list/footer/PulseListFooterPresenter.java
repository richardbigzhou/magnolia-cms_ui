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
package info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer;

import info.magnolia.ui.api.action.ActionDefinition;

import java.util.List;

import javax.inject.Inject;

/**
 * {@linkplain PulseListFooterPresenter} initialises the {@link PulseListFooterView} and binds it with other Pulse components
 * via {@link Listener} interface.
 */
public class PulseListFooterPresenter implements PulseListFooterView.Listener {

    private final PulseListFooterView view;
    private Listener listener;

    @Inject
    public PulseListFooterPresenter(final PulseListFooterView view) {
        this.view = view;
        this.view.setListener(this);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PulseListFooterView start(List<ActionDefinition> bulkActions, long totalAmount) {
        for (ActionDefinition actionDefinition : bulkActions) {
            String label = actionDefinition.getLabel();
            String icon = actionDefinition.getIcon();

            view.addAction(actionDefinition.getName(), label, icon);
        }
        view.updateStatus(totalAmount, 0);
        return view;
    }

    @Override
    public void onBulkActionTriggered(String actionId) {
        listener.onBulkActionTriggered(actionId);
    }

    public void setActionEnabled(String actionId, boolean isEnabled) {
        if (view != null) {
            view.setActionEnabled(actionId, isEnabled);
        }
    }

    public void updateStatus(long totalAmount, int totalSelected) {
        if (view != null) {
            view.updateStatus(totalAmount, totalSelected);
        }
    }

    /**
     * Bulk action triggering listener interface.
     */
    public interface Listener {

        void onBulkActionTriggered(String actionId);
    }
}
