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
package info.magnolia.ui.admincentral.usermenu;

import info.magnolia.ui.vaadin.actionbar.ActionPopup;
import info.magnolia.ui.vaadin.usermenu.UserMenu;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuClosedEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuClosedListener;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.NativeButton;

/**
 * Implementation of {@link UserMenuView}. Basically a button, which opens the {@link info.magnolia.ui.vaadin.usermenu.UserMenu} when clicked.
 */
public class UserMenuViewImpl implements UserMenuView {

    private final NativeButton button = new NativeButton();
    private final UserMenu menu = new UserMenu();
    private UserMenuView.Listener listener;
    private String caption;

    public UserMenuViewImpl() {
        button.setStyleName("user-menu");
        button.setHtmlContentAllowed(true);

        menu.setAsContextMenuOf(button);

        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                menu.open();
                updateButton(caption, true);
            }
        });

        menu.addContextMenuCloseListener(new ContextMenuClosedListener() {

            @Override
            public void onContextMenuClosed(ContextMenuClosedEvent event) {
                updateButton(caption, false);
            }
        });
    }

    @Override
    public void setCaption(String caption) {
        updateButton(caption, false);
        this.caption = caption;
    }

    private void updateButton(String caption, boolean open) {
        String text = caption + ((open) ? "<span class=\"icon-arrow2_n\"></span>" : "<span class=\"icon-arrow2_s\"></span>");
        button.setCaption(text);
    }

    @Override
    public void setListener(UserMenuView.Listener listener) {
        this.listener = listener;
    }

    @Override
    @Deprecated
    public void addAction(String actionName, String label) {
        addAction(actionName, label, "");
    }

    @Override
    public void addAction(final String actionName, String label, String icon) {
        String iconFontCode = ActionPopup.ICON_FONT_CODE + icon;
        ExternalResource iconFontResource = new ExternalResource(iconFontCode);

        menu.addItem(label, iconFontResource).addItemClickListener(new ContextMenu.ContextMenuItemClickListener() {
            @Override
            public void contextMenuItemClicked(ContextMenu.ContextMenuItemClickEvent event) {
                listener.onAction(actionName);
            }
        });
    }

    @Override
    public Component asVaadinComponent() {
        return button;
    }
}
