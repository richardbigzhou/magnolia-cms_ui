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
package info.magnolia.ui.contentapp.renderer;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.renderer.DefaultEditorActionRenderer;
import info.magnolia.ui.framework.overlay.ViewAdapter;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Lists;
import com.vaadin.ui.Component;

/**
 * {@link info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer} implementation that alters the 'enabled'-state of
 * the underlying Vaadin view based on {@link AvailabilityChecker} decision made against a current selection provided
 * by {@link SelectionChangedEvent} via {@link ChooseDialogEventBus}.
 */
public class SelectionSensitiveActionRenderer extends DefaultEditorActionRenderer {

    private final EventBus chooseDialogEventBus;
    private final AvailabilityChecker availabilityChecker;

    @Inject
    public SelectionSensitiveActionRenderer(@Named(ChooseDialogEventBus.NAME) EventBus chooseDialogEventBus, AvailabilityChecker availabilityChecker) {
        this.chooseDialogEventBus = chooseDialogEventBus;
        this.availabilityChecker = availabilityChecker;
    }

    @Override
    public View start(final ActionDefinition definition, ActionListener listener) {
        View view = super.start(definition, listener);
        final Component button = view.asVaadinComponent();
        chooseDialogEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                Set<Object> itemIds = event.getItemIds();
                if (itemIds == null || itemIds.isEmpty()) {
                    itemIds = Collections.emptySet();
                }

                AvailabilityDefinition availability = definition.getAvailability();
                button.setEnabled(availabilityChecker.isAvailable(availability, Lists.newLinkedList(itemIds)));
            }
        });
        return new ViewAdapter(button);
    }

}
