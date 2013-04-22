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
package info.magnolia.ui.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.ui.statusbar.StatusBarView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import javax.inject.Inject;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;

/**
 * The browser features a status bar at the bottom with selected path and item count information.
 */
public class WorkbenchStatusBarPresenter {

    private final StatusBarView view;

    private EventBus eventBus;

    private int selectionCount;
    private int itemCount;

    // TODO externalize in properties file, leave door open to specialization as per app content type.
    private String countPattern = "%d item(s), %d selected";

    private final Label selectionLabel = new Label();
    private final Label countLabel = new Label();

    private JcrItemAdapter selectedItem;

    @Inject
    public WorkbenchStatusBarPresenter(StatusBarView view) {
        this.view = view;
        selectionLabel.setSizeUndefined();
        countLabel.setSizeUndefined();
    }

    private void bindHandlers() {
        eventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                setSelectedItem(event.getItem());
                setSelectionCount(event.getItem() != null ? 1 : 0);
            }
        });

        // TODO add subapp event handlers for selection and item count
    }

    public StatusBarView start(EventBus eventBus) {
        this.eventBus = eventBus;
        view.addComponent(selectionLabel, Alignment.MIDDLE_LEFT);
        view.addComponent(countLabel, Alignment.MIDDLE_RIGHT);
        bindHandlers();

        return view;
    }

    public void setSelectedItem(JcrItemAdapter item) {
        if (item != selectedItem) {
            if (item != null) {
                selectionLabel.setValue(item.getPath());
            } else {
                selectionLabel.setValue("");
            }
            this.selectedItem = item;
        }
    }

    public void setSelectionCount(int selectionCount) {
        countLabel.setValue(String.format(countPattern, itemCount, selectionCount));
        this.selectionCount = selectionCount;
    }

    public void setItemCount(int itemCount) {
        countLabel.setValue(String.format(countPattern, itemCount, selectionCount));
        this.itemCount = itemCount;
    }
}
