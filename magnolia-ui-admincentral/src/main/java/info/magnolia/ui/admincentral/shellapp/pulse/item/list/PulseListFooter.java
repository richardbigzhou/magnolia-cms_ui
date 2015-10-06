/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseConstants;
import info.magnolia.ui.admincentral.shellapp.pulse.task.TasksListView;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;

/**
 * Footer view implementation displayed underneath the items list.
 * 
 * @deprecated since 5.4.3. Moved to {@link info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer.PulseListFooterPresenter}.
 */
@Deprecated
public final class PulseListFooter extends CustomComponent {

    private HorizontalLayout footer = new HorizontalLayout();
    private ContextMenu contextMenu = new ActionPopup();
    private Label status = new Label();
    private NativeButton actionPopupTrigger = new NativeButton();

    private Table itemsTable;
    private SimpleTranslator i18n;
    private PulseListView.Listener messagesListener;
    private TasksListView.Listener tasksListener;

    // can't get the menu items from ContextMenu
    private List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

    private long totalAmount;

    public PulseListFooter(final Table itemsTable, final SimpleTranslator i18n, boolean withTaskContextMenu) {
        super();
        this.itemsTable = itemsTable;
        this.i18n = i18n;
        if (withTaskContextMenu) {
            buildTaskContextMenu();
        } else {
            buildMessageContextMenu();
        }
        construct();
        setCompositionRoot(footer);
    }

    private void construct() {
        footer.setSizeUndefined();
        footer.addStyleName("footer");

        final Label actionLabel = new Label(i18n.translate("pulse.footer.title"));

        actionPopupTrigger.setHtmlContentAllowed(true);
        actionPopupTrigger.setCaption("<span class=\"icon-arrow2_e\"></span>");
        actionPopupTrigger.addStyleName("action-popup-trigger");

        actionPopupTrigger.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                contextMenu.open(event.getClientX(), event.getClientY());
            }
        });
        footer.addComponent(actionLabel);
        footer.addComponent(actionPopupTrigger);

        status.addStyleName("status");
        footer.addComponent(status);

        contextMenu.setAsContextMenuOf(actionPopupTrigger);
        contextMenu.setOpenAutomatically(false);
    }

    private void buildTaskContextMenu() {

        final ExternalResource iconAssignResource = new ExternalResource(ActionPopup.ICON_FONT_CODE + "icon-user-public");

        final ContextMenuItem claim = contextMenu.addItem(i18n.translate("publish.actions.claim"), iconAssignResource);

        claim.addItemClickListener(new ContextMenuItemClickListener() {

            @Override
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
                final Set<String> selectedItems = (Set<String>) itemsTable.getValue();
                if (selectedItems == null || selectedItems.isEmpty()) {
                    // nothing to do here
                    return;
                }

                tasksListener.claimTask(selectedItems);
            }
        });
        claim.setEnabled(false);
        menuItems.add(claim);

        User user = MgnlContext.getUser();
        // TODO ideally context menu action availability should use the same mechanism and rules defined in the messageView config
        // but as this is not straightforward, for the time being we hack it like this
        if (user.getAllRoles().contains("superuser")) {
            addRemoveMenuItem("publish.actions.archive");
        }
    }

    private void buildMessageContextMenu() {
        addRemoveMenuItem("publish.actions.delete");
    }

    private void addRemoveMenuItem(final String i18nKey) {
        final ExternalResource iconDeleteResource = new ExternalResource(ActionPopup.ICON_FONT_CODE + "icon-delete");

        final ContextMenuItem remove = contextMenu.addItem(i18n.translate(i18nKey), iconDeleteResource);

        remove.addItemClickListener(new ContextMenuItemClickListener() {

            @Override
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
                final Set<String> selectedItems = (Set<String>) itemsTable.getValue();
                if (selectedItems == null || selectedItems.isEmpty()) {
                    // nothing to do here
                    return;
                }
                messagesListener.deleteItems(selectedItems);
            }
        });

        remove.setEnabled(false);

        menuItems.add(remove);
    }

    public void updateStatus() {
        int totalSelected = 0;

        for (String id : (Set<String>) itemsTable.getValue()) {
            // skip generated header rows when grouping messages
            if (id.startsWith(PulseConstants.GROUP_PLACEHOLDER_ITEMID)) {
                continue;
            }
            totalSelected++;
        }
        // TODO ideally context menu action availability should use the same mechanism and rules defined in the messageView config
        // but as this is not straightforward, for the time being we hack it like this
        enableActions(totalSelected > 0);

        final String selectedMessagesAsString = totalSelected > 0 ? Integer.toString(totalSelected) : i18n.translate("pulse.footer.status.none");
        status.setValue(i18n.translate("pulse.footer.status", totalAmount, selectedMessagesAsString));
    }

    public void setMessagesListener(final PulseListView.Listener listener) {
        this.messagesListener = listener;
    }

    public void setTasksListener(final TasksListView.Listener listener) {
        this.tasksListener = listener;
    }

    private void enableActions(boolean enable) {
        for (ContextMenuItem item : menuItems) {
            item.setEnabled(enable);
        }
    }

    public void setTotalAmount(long amount) {
        this.totalAmount = amount;
        updateStatus();
    }
}
