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
package info.magnolia.ui.admincentral.shellapp.favorites;

import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * FavoritesForm.
 */
public class FavoritesForm extends CustomComponent {
    private FavoritesView.Listener listener;

    public FavoritesForm(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup, Map<String, String> availableGroups, FavoritesView.Listener listener) {
        this.listener = listener;
        final VerticalLayout favoriteForm = new VerticalLayout();
        favoriteForm.addStyleName("favorites-form");

        final FavoriteForm favoriteFormEntry = new FavoriteForm(newFavorite, availableGroups);
        final FavoriteGroupForm favoriteGroupForm = new FavoriteGroupForm(newGroup);
        final TabSheet tabsheet = new TabSheet();
        tabsheet.addStyleName("favorites-tabs");
        tabsheet.addTab(favoriteFormEntry, "Add a new favorite");
        tabsheet.addTab(favoriteGroupForm, "Add a new group");
        tabsheet.setVisible(false);
        final CssLayout header = new CssLayout();
        header.addStyleName("dialog-header");
        header.setSizeFull();
        header.addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                tabsheet.setVisible(!tabsheet.isVisible());
            }
        });
        final Label addNewIcon = new Label();
        addNewIcon.setSizeUndefined();
        addNewIcon.setContentMode(ContentMode.HTML);
        addNewIcon.setValue("<span class=\"icon-add-fav\"></span>");
        addNewIcon.addStyleName("icon");

        final Label addNewLabel = new Label("Add new");
        addNewLabel.setSizeUndefined();
        addNewLabel.addStyleName("title");

        header.addComponent(addNewIcon);
        header.addComponent(addNewLabel);
        favoriteForm.addComponent(header);
        favoriteForm.addComponent(tabsheet);
        setCompositionRoot(favoriteForm);
    }

    /**
     * A form component that allows editing an item.
     */
    private class FavoriteForm extends CustomComponent {

        private TextField url = new TextField("Location");

        private TextField title = new TextField("Title");

        private ComboBox group;

        public FavoriteForm(final JcrNewNodeAdapter newFavorite, Map<String, String> availableGroups) {
            addStyleName("favorites-form-content");
            FormLayout layout = new FormLayout();
            layout.setSizeUndefined();
            title.setRequired(true);
            url.setRequired(true);
            layout.addComponent(url);
            layout.addComponent(title);

            group = new ComboBox("Add to group");
            for (Entry<String, String> entry : availableGroups.entrySet()) {
                String id = entry.getKey();
                group.addItem(id);
                group.setItemCaption(id, entry.getValue());
            }
            layout.addComponent(group);

            // Now use a binder to bind the members
            final FieldGroup binder = new FieldGroup(newFavorite);
            binder.bindMemberFields(this);

            CssLayout buttons = new CssLayout();
            buttons.addStyleName("buttons");

            // A button to discard the buffer
            buttons.addComponent(new Button("Cancel", new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    binder.discard();
                    // TODO remove form
                }
            }));

            Button addButton = new Button("Add", new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        binder.commit();
                        listener.addFavorite(newFavorite);
                    } catch (CommitException e) {
                        // TODO how do we display validation errors?
                        Notification.show(e.getMessage());
                    }
                }
            });
            addButton.addStyleName("btn-dialog-commit");

            // A button to commit the buffer
            buttons.addComponent(addButton);

            layout.addComponent(buttons);

            setCompositionRoot(layout);
        }
    }

    /**
     * A form component that allows editing an item.
     */
    private class FavoriteGroupForm extends CustomComponent {

        private TextField title = new TextField("Title");

        public FavoriteGroupForm(final JcrNewNodeAdapter newGroup) {
            addStyleName("favorites-form-content");
            FormLayout layout = new FormLayout();
            title.setRequired(true);
            layout.addComponent(title);

            // Now use a binder to bind the members
            final FieldGroup binder = new FieldGroup(newGroup);
            binder.bindMemberFields(this);

            CssLayout buttons = new CssLayout();
            buttons.addStyleName("buttons");

            // A button to discard the buffer
            buttons.addComponent(new Button("Cancel", new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    binder.discard();
                    // TODO remove form
                }
            }));

            Button addButton = new Button("Add", new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        binder.commit();
                        listener.addGroup(newGroup);
                    } catch (CommitException e) {
                        // TODO how do we display validation errors?
                        Notification.show(e.getMessage());
                    }
                }
            });
            addButton.addStyleName("btn-dialog-commit");

            // A button to commit the buffer
            buttons.addComponent(addButton);

            layout.addComponent(buttons);

            setCompositionRoot(layout);
        }
    }
}
