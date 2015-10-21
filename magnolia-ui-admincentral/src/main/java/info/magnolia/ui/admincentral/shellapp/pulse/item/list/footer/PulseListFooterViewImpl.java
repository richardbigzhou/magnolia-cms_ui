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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;

/**
 * Implementation for {@link PulseListFooterView}.
 */
public class PulseListFooterViewImpl extends CustomComponent implements PulseListFooterView {

    private final SimpleTranslator i18n;

    private HorizontalLayout footer = new HorizontalLayout();
    private ContextMenu contextMenu = new ActionPopup();
    private Label status = new Label();
    private NativeButton actionPopupTrigger = new NativeButton();
    private Label actionLabel;
    private PulseListFooterView.Listener listener;

    // can't get the menu items from ContextMenu
    private Map<String, ContextMenuItem> menuItems = new HashMap<>();

    @Inject
    public PulseListFooterViewImpl(final SimpleTranslator i18n) {
        this.i18n = i18n;
        initView();
        setCompositionRoot(footer);
    }

    @Override
    public void setListener(PulseListFooterView.Listener listener) {
        this.listener = listener;
    }

    private void initView() {
        footer.setSizeUndefined();
        footer.addStyleName("footer");

        actionPopupTrigger.setHtmlContentAllowed(true);
        actionPopupTrigger.setCaption("<span class=\"icon-arrow2_e\"></span>");
        actionPopupTrigger.addStyleName("action-popup-trigger");

        actionPopupTrigger.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                contextMenu.open(event.getClientX(), event.getClientY());
            }
        });

        actionLabel = new Label(i18n.translate("pulse.footer.title"));
        footer.addComponent(actionLabel);
        footer.addComponent(actionPopupTrigger);
        toggleActionTriggerVisibility(false);

        status.addStyleName("status");
        footer.addComponent(status);

        contextMenu.addItemClickListener(new ContextMenu.ContextMenuItemClickListener() {

            @Override
            public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
                ContextMenuItem obj = (ContextMenuItem) event.getSource();
                String eventActionName = (String) obj.getData();
                if (listener != null) {
                    listener.onBulkActionItemClicked(eventActionName);
                }
            }
        });

        contextMenu.setAsContextMenuOf(actionPopupTrigger);
        contextMenu.setOpenAutomatically(false);
    }

    private void toggleActionTriggerVisibility(boolean visible) {
        actionLabel.setVisible(visible);
        actionPopupTrigger.setVisible(visible);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void updateStatus(long totalAmount, int totalSelected) {
        toggleActionTriggerVisibility(totalSelected != 0);
        final String selectedMessages = totalSelected > 0 ? Integer.toString(totalSelected) : i18n.translate("pulse.footer.status.none");
        status.setValue(i18n.translate("pulse.footer.status", totalAmount, selectedMessages));
    }

    @Override
    public void setActionEnabled(String actionName, boolean enabled) {
        ContextMenuItem item = menuItems.get(actionName);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    @Override
    public void addActionItem(String name, String label, String icon) {
        ExternalResource iconFontResource = new ExternalResource(ActionPopup.ICON_FONT_CODE + icon);
        ContextMenu.ContextMenuItem menuItem = contextMenu.addItem(label, iconFontResource);
        // Set data variable so that the event handler can determine which action to launch.
        menuItem.setData(name);
        menuItems.put(name, menuItem);
    }

}
