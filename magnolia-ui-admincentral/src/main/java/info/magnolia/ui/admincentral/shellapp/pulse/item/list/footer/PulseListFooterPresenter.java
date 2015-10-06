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
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;

import java.util.List;

import javax.inject.Inject;

import com.vaadin.server.ExternalResource;

/**
 * Footer presenter.
 */

public class PulseListFooterPresenter implements PulseListFooterView.Listener {

    /**
     * Listener interface for the footer action.
     */
    public interface Listener {

        void onBulkActionItemClicked(String itemName);
    }

    private final PulseListFooterView view;
    private Listener listener;

    @Inject
    public PulseListFooterPresenter(PulseListFooterView view, AvailabilityChecker availabilityChecker) {
        this.view = view;
        this.view.setListener(this);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public View start(List<ActionDefinition> bulkActions, long totalAmount) {
        for (ActionDefinition actionDefinition : bulkActions) {
            String label = actionDefinition.getLabel();
            String iconFontCode = ActionPopup.ICON_FONT_CODE + actionDefinition.getIcon();
            ExternalResource iconFontResource = new ExternalResource(iconFontCode);

            view.addActionItem(actionDefinition.getName(), label, iconFontResource);
        }
        view.updateStatus(totalAmount, 0);
        return view;
    }

    @Override
    public void onBulkActionItemClicked(String actionToken) {
        listener.onBulkActionItemClicked(actionToken);
    }

    public void toggleActionEnable(String actionName, boolean enabled) {
        if (view != null) {
            view.setActionEnabled(actionName, enabled);
        }
    }

    public void updateStatus(long totalAmount, int totalSelected) {
        if (view != null) {
            view.updateStatus(totalAmount, totalSelected);
        }
    }
}
