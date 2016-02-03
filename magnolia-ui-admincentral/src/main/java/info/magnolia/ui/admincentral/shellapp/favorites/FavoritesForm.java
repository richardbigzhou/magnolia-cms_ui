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
package info.magnolia.ui.admincentral.shellapp.favorites;

import info.magnolia.cms.core.Path;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * FavoritesForm.
 */
public final class FavoritesForm extends CustomComponent {

    private static final String EDIT_ACTION_STYLENAME = "favItemEdit";

    private FavoritesView.Listener listener;
    private Shell shell;
    private TabSheet tabsheet;
    private Label arrowIcon;
    private InternalFavoriteEntryForm favoriteEntryForm;
    private InternalFavoriteGroupForm favoriteGroupForm;
    private final SimpleTranslator i18n;

    private Label editIcon;
    private Label editLabel;

    public FavoritesForm(FavoritesView.Listener listener, Shell shell, SimpleTranslator i18n) {
        this.listener = listener;
        this.shell = shell;
        this.i18n = i18n;
        init();
    }

    /**
     * Since 5.3.5 — use {@link #FavoritesForm(info.magnolia.ui.admincentral.shellapp.favorites.FavoritesView.Listener, Shell, SimpleTranslator)} as a constructor and populate fav/group entries individually through the new setters.
     *
     * @see #setNewFavorite(Item)
     * @see #setNewGroup(Item)
     * @see #setAvailableGroups(Map)
     */
    @Deprecated
    public FavoritesForm(final JcrNewNodeAdapter newFavorite, final JcrNewNodeAdapter newGroup, final Map<String, String> availableGroups,
            final FavoritesView.Listener listener, final Shell shell, final SimpleTranslator i18n) {
        this(listener, shell, i18n);
    }

    private void init() {
        addStyleName("favorites-form");
        final VerticalLayout favoriteForm = new VerticalLayout();
        favoriteEntryForm = new InternalFavoriteEntryForm();
        favoriteGroupForm = new InternalFavoriteGroupForm();

        tabsheet = new TabSheet();
        tabsheet.addStyleName("favorites-tabs");
        tabsheet.addTab(favoriteEntryForm, i18n.translate("favorites.form.favorite.add"));
        tabsheet.addTab(favoriteGroupForm, i18n.translate("favorites.form.group.add"));

        tabsheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                if (event.getTabSheet().getSelectedTab() instanceof InternalFavoriteEntryForm) {
                    favoriteGroupForm.removeEnterKeyShortcutListener();
                    favoriteEntryForm.addEnterKeyShortcutListener();
                } else {
                    favoriteEntryForm.removeEnterKeyShortcutListener();
                    favoriteGroupForm.addEnterKeyShortcutListener();
                }
            }
        });

        final CssLayout header = new CssLayout();
        header.addStyleName("dialog-header");
        header.setSizeFull();
        header.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                // change the visibility of the group- and favorite-items
                if (event.getClickedComponent() == editIcon || event.getChildComponent() == editLabel) {
                    if (!listener.hasItems() || listener.itemsAreEditable()) {
                        listener.setToInitialState();
                    } else {
                        listener.setItemsEditable(true);
                    }
                } else {
                    // just open || close the FavoritesForm
                    if (isOpen()) {
                        close();
                    } else {
                        open();
                    }
                }
            }
        });

        // add
        final Label addNewIcon = new Label();
        addNewIcon.setSizeUndefined();
        addNewIcon.addStyleName("icon");
        addNewIcon.addStyleName("icon-add-fav");
        final Label addNewLabel = new Label(i18n.translate("favorites.form.add"));
        addNewLabel.setSizeUndefined();
        addNewLabel.addStyleName("title");

        // edit
        editIcon = new Label();
        editIcon.setSizeUndefined();
        editIcon.addStyleName(EDIT_ACTION_STYLENAME);
        editIcon.addStyleName("icon");
        editIcon.addStyleName("icon-edit");

        editLabel = new Label(i18n.translate("favorites.form.favorite.edit"));
        editLabel.setSizeUndefined();
        editLabel.addStyleName("title");
        editLabel.addStyleName(EDIT_ACTION_STYLENAME);

        // arrow
        arrowIcon = new Label();
        arrowIcon.setSizeUndefined();
        arrowIcon.addStyleName("icon");
        arrowIcon.addStyleName("arrow");
        arrowIcon.addStyleName("icon-arrow2_n");

        // assemble
        header.addComponent(addNewIcon);
        header.addComponent(addNewLabel);
        header.addComponent(editIcon);
        header.addComponent(editLabel);
        header.addComponent(arrowIcon);
        favoriteForm.addComponent(header);
        favoriteForm.addComponent(tabsheet);

        // form is closed initially
        close();
        setCompositionRoot(favoriteForm);
    }

    public void setNewFavorite(Item newFavorite) {
        favoriteEntryForm.setDataSource(newFavorite);
    }

    public void setNewGroup(Item newGroup) {
        favoriteGroupForm.setDataSource(newGroup);
    }

    public void setAvailableGroups(Map<String, String> availableGroups) {
        favoriteEntryForm.setAvailableGroups(availableGroups);
    }

    public void setEditActionEnabled(boolean enabled) {
        if (!enabled) {
            editIcon.addStyleName("disabled");
            editLabel.addStyleName("disabled");
        } else {
            editIcon.removeStyleName("disabled");
            editLabel.removeStyleName("disabled");
        }
    }

    public void close() {
        tabsheet.setVisible(false);
        // reset form to its first tab when it is closed
        if (tabsheet.getSelectedTab() != favoriteEntryForm) {
            tabsheet.setSelectedTab(favoriteEntryForm);
        }
        arrowIcon.removeStyleName("icon-arrow2_s");
        arrowIcon.addStyleName("icon-arrow2_n");
        // remove key shortcut listener or this might compete with the next element getting the focus.
        favoriteEntryForm.removeEnterKeyShortcutListener();
        favoriteGroupForm.removeEnterKeyShortcutListener();
    }

    public void open() {
        tabsheet.setVisible(true);
        arrowIcon.removeStyleName("icon-arrow2_n");
        arrowIcon.addStyleName("icon-arrow2_s");
        favoriteEntryForm.addEnterKeyShortcutListener();
        // the group form will get the key shortcut listener attached on selecting it.
    }

    public boolean isOpen() {
        return tabsheet.isVisible();
    }

    /**
     * The form component displayed in the favorite tab.
     */
    private class InternalFavoriteEntryForm extends CustomComponent {

        private TextField url = new TextField(i18n.translate("favorites.form.location"));
        private TextField title = new TextField(i18n.translate("favorites.form.title"));
        private ComboBox group;

        private ShortcutListener enterShortcutListener;

        private final FieldGroup form;

        private Map<String, String> availableGroups;

        public InternalFavoriteEntryForm() {
            addStyleName("favorites-form-content");
            FormLayout layout = new FormLayout();

            title.setRequired(true);
            title.setDescription(i18n.translate("favorites.form.title")); // tooltip

            url.setRequired(true);
            url.setDescription(i18n.translate("favorites.form.location"));
            layout.addComponent(title);
            layout.addComponent(url);

            group = new ComboBox(i18n.translate("favorites.form.groups"));
            group.setNewItemsAllowed(true);
            group.setImmediate(false);
            group.setNewItemHandler(new AbstractSelect.NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    String newGroupId = Path.getValidatedLabel(newItemCaption);
                    group.addItem(newGroupId);
                    group.setItemCaption(newGroupId, newItemCaption);
                    group.setValue(newGroupId);
                }
            });
            // the blur-listener below ensures "appropriate" behavior when adding a new value and then clicking tab -> to blur -> to go to add-button
            // without the listener the new value is NOT selected and sometimes not added (with tab, enter)
            // String value = event.getSource().toString() leads to a warning in the log-file and is discourage from VAADIN
            group.addBlurListener(new FieldEvents.BlurListener() {
                @Override
                public void blur(FieldEvents.BlurEvent event) {
                    String value = (String) group.getValue();
                    group.setValue(value);
                }
            });

            group.setDescription(i18n.translate("favorites.form.groups"));
            layout.addComponent(group);

            // Bind fields individually — binding members doesn't seem to work with initial (null) datasource
            form = new FieldGroup();
            form.bind(title, "title");
            form.bind(url, "url");
            form.bind(group, "group");

            final CssLayout buttons = new CssLayout();
            buttons.addStyleName("buttons");

            final Button addButton = new Button(i18n.translate("favorites.button.add"), new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    addFavorite();
                }
            });
            addButton.addStyleName("commit");
            buttons.addComponent(addButton);
            layout.addComponent(buttons);

            this.enterShortcutListener = new ShortcutListener("", KeyCode.ENTER, null) {

                @Override
                public void handleAction(Object sender, Object target) {
                    addFavorite();
                }
            };

            setCompositionRoot(layout);
        }

        void setDataSource(Item newFavorite) {
            form.setItemDataSource(newFavorite);
        }

        void setAvailableGroups(Map<String, String> availableGroups) {
            group.removeAllItems();
            for (Entry<String, String> entry : availableGroups.entrySet()) {
                String id = entry.getKey();
                group.addItem(id);
                group.setItemCaption(id, entry.getValue());
            }
            this.availableGroups = availableGroups;
        }

        public void addEnterKeyShortcutListener() {
            addShortcutListener(enterShortcutListener);
            title.focus();
        }

        public void removeEnterKeyShortcutListener() {
            removeShortcutListener(enterShortcutListener);
        }

        private void addFavorite() {
            try {
                JcrNewNodeAdapter newFavorite = (JcrNewNodeAdapter) form.getItemDataSource();
                form.commit();

                // since MGNLUI-2599 it is possible to add a group and a favorite (which then goes into the group) at the same time
                if (group == null || group.getValue() == null || !selectedGroupIsNew()) {
                    listener.addFavorite(newFavorite);
                } else {
                    JcrNewNodeAdapter newGroup = ((FavoritesPresenter) listener).createNewGroupSuggestion();
                    String newGroupId = (String) group.getValue();
                    String newGroupLabel = group.getItemCaption(newGroupId);
                    // must set the properties for the group here manually; properties of newFavorite are set in binder
                    newGroup.addItemProperty("group", new ObjectProperty<String>(newGroupId));
                    newGroup.addItemProperty("title", new ObjectProperty<String>(newGroupLabel));
                    listener.addFavoriteAndGroup(newFavorite, newGroup);
                }
                close(); // close form explicitly upon successful submit
            } catch (CommitException e) {
                shell.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("favorites.fields.required"));
            }
        }

        private boolean selectedGroupIsNew() {
            if (availableGroups == null || availableGroups.size() == 0) {
                return true;
            } else {
                Object value = group.getValue();
                if (value != null && availableGroups != null && availableGroups.size() > 0) {
                    return !availableGroups.containsKey(value);
                }
            }
            return false;
        }

    }

    /**
     * The form component displayed in the group tab.
     */
    private class InternalFavoriteGroupForm extends CustomComponent {

        private TextField title = new TextField(i18n.translate("favorites.form.title"));

        private ShortcutListener enterShortcutListener;
        private final FieldGroup form;

        public InternalFavoriteGroupForm() {
            addStyleName("favorites-form-content");
            FormLayout layout = new FormLayout();

            title.setRequired(true);
            title.setDescription(i18n.translate("favorites.form.title"));// tooltip
            title.addStyleName("group-title");
            layout.addComponent(title);

            // Bind fields individually — binding members doesn't seem to work with initial (null) datasource
            form = new FieldGroup();
            form.bind(title, "title");

            final CssLayout buttons = new CssLayout();
            buttons.addStyleName("buttons");

            final Button addButton = new Button(i18n.translate("favorites.button.add"), new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    addGroup();
                }
            });
            addButton.addStyleName("v-button-commit");
            buttons.addComponent(addButton);
            layout.addComponent(buttons);

            this.enterShortcutListener = new ShortcutListener("", KeyCode.ENTER, null) {

                @Override
                public void handleAction(Object sender, Object target) {
                    addGroup();
                }
            };

            setCompositionRoot(layout);
        }

        void setDataSource(Item newGroup) {
            form.setItemDataSource(newGroup);
        }

        public void addEnterKeyShortcutListener() {
            addShortcutListener(enterShortcutListener);
            title.focus();
        }

        public void removeEnterKeyShortcutListener() {
            removeShortcutListener(enterShortcutListener);
        }

        private void addGroup() {
            try {
                JcrNewNodeAdapter newGroup = (JcrNewNodeAdapter) form.getItemDataSource();
                form.commit();
                listener.addGroup(newGroup);
                close(); // close form explicitly upon successful submit
            } catch (CommitException e) {
                shell.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("favorites.fields.required"));
            }
        }
    }
}
