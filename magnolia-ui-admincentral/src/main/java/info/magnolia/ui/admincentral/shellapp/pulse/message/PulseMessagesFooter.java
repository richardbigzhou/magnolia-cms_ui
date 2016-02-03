/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;

import java.util.Set;

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
import com.vaadin.ui.TreeTable;

/**
 * PulseMessagesFooter.
 */
public final class PulseMessagesFooter extends CustomComponent {

    private HorizontalLayout footer;
    private NativeButton actionPopupTrigger;
    private TreeTable messageTable;
    private Label status;
    private PulseMessagesView.Listener listener;

    public PulseMessagesFooter(final TreeTable messageTable) {
        super();
        this.messageTable = messageTable;
        construct();
        setCompositionRoot(footer);
    }

    private void construct() {
        footer = new HorizontalLayout();
        footer.setSizeUndefined();
        footer.addStyleName("footer");

        final Label actionLabel = new Label(MessagesUtil.get("pulse.footer.title"));

        actionPopupTrigger = new NativeButton();
        actionPopupTrigger.setHtmlContentAllowed(true);
        actionPopupTrigger.setCaption("<span class=\"icon-arrow2_e\"></span>");
        actionPopupTrigger.addStyleName("action-popup-trigger");

        final ActionPopup contextMenu = new ActionPopup();
        contextMenu.setOpenAutomatically(false);

        final String iconFontCode = ActionPopup.ICON_FONT_CODE + "icon-delete";
        final ExternalResource iconFontResource = new ExternalResource(iconFontCode);

        final ContextMenuItem menuItem = contextMenu.addItem(MessagesUtil.get("pulse.actionpop.delete.selected"), iconFontResource);
        menuItem.addItemClickListener(new ContextMenuItemClickListener() {

            @Override
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
                final Set<String> selectedItems = (Set<String>) messageTable.getValue();
                if (selectedItems == null || selectedItems.isEmpty()) {
                    // nothing to do here
                    return;
                }
                listener.deleteMessages(selectedItems);
            }
        });

        contextMenu.setAsContextMenuOf(actionPopupTrigger);

        actionPopupTrigger.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                contextMenu.open(event.getClientX(), event.getClientY());
            }
        });
        footer.addComponent(actionLabel);
        footer.addComponent(actionPopupTrigger);

        status = new Label();
        status.addStyleName("status");
        footer.addComponent(status);
    }


    public void updateStatus() {
        int totalMessages = 0;
        int totalSelected = 0;

        for (Object id : messageTable.getItemIds()) {
            // skip generated header rows when grouping messages
            if (((String) id).startsWith(PulseMessagesPresenter.GROUP_PLACEHOLDER_ITEMID)) {
                continue;
            }
            totalMessages++;
        }
        for (String id : (Set<String>) messageTable.getValue()) {
            // skip generated header rows when grouping messages
            if (id.startsWith(PulseMessagesPresenter.GROUP_PLACEHOLDER_ITEMID)) {
                continue;
            }
            totalSelected++;
        }
        final String totalMessagesAsString = totalMessages > 0 ? Integer.toString(totalMessages) : MessagesUtil.get("pulse.footer.status.no");
        final String selectedMessagesAsString = totalSelected > 0 ? Integer.toString(totalSelected) : MessagesUtil.get("pulse.footer.status.none");
        status.setValue(MessagesUtil.get("pulse.footer.status", new String[] { totalMessagesAsString, selectedMessagesAsString }));
    }

    public void setListener(final PulseMessagesView.Listener listener) {
        this.listener = listener;
    }

}
