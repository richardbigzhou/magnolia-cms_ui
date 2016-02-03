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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.favorites.EditingEvent.EditingListener;
import info.magnolia.ui.admincentral.shellapp.favorites.SelectedEvent.SelectedListener;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * Favorite group.
 */
public final class FavoritesGroup extends CssLayout implements SelectedEvent.SelectedNotifier {

    private TextField titleField;
    private NativeButton editButton;
    private NativeButton removeButton;
    private String title;
    private String relPath;
    private boolean editable;
    private boolean selected;
    private CssLayout wrapper;
    private EnterKeyShortcutListener enterKeyShortcutListener;
    private EscapeKeyShortcutListener escapeKeyShortcutListener;
    private Shell shell;
    private FavoritesView view;
    private Component currentlySelectedFavEntry;
    private final SimpleTranslator i18n;

    /**
     * Creates an empty placeholder group.
     */
    public FavoritesGroup(SimpleTranslator i18n) {
        this.i18n = i18n;
        addStyleName("no-group");
    }

    public FavoritesGroup(final AbstractJcrNodeAdapter favoritesGroup, final FavoritesView.Listener listener, final Shell shell, final FavoritesView view, final SimpleTranslator i18n) {
        this.shell = shell;
        this.view = view;
        this.i18n = i18n;

        addStyleName("favorites-group");
        construct(favoritesGroup, listener);

        final Map<String, AbstractJcrNodeAdapter> nodeAdapters = favoritesGroup.getChildren();

        for (String key : nodeAdapters.keySet()) {
            final AbstractJcrNodeAdapter fav = nodeAdapters.get(key);
            final FavoritesEntry favEntry = new FavoritesEntry(fav, listener, shell, i18n);
            favEntry.addSelectedListener(new SelectedListener() {

                @Override
                public void onSelected(SelectedEvent event) {
                    currentlySelectedFavEntry = event.getComponent();
                    view.updateSelection(event.getComponent());
                }
            });
            favEntry.setGroup(this.relPath);
            final EntryDragAndDropWrapper wrapper = new EntryDragAndDropWrapper(favEntry, listener);
            favEntry.addEditingListener(new EditingListener() {

                @Override
                public void onEdit(EditingEvent event) {
                    if (event.isEditing()) {
                        wrapper.setDragStartMode(DragStartMode.NONE);
                    } else {
                        wrapper.setDragStartMode(DragStartMode.WRAPPER);
                    }
                }
            });
            addComponent(wrapper);
        }
    }

    public String getRelPath() {
        return this.relPath;
    }

    /**
     * Sets this group and all of its fav entries (if any) as unselected and non editable, that is at their initial state.
     */
    public void reset() {
        // skip it if this group is a placeholder for no group fav entries, as it has no title
        if (titleField != null) {
            setEditable(false);
            setSelected(false);
        }
        Iterator<Component> components = iterator();
        while (components.hasNext()) {
            Component component = components.next();
            if (component instanceof EntryDragAndDropWrapper) {
                component = ((EntryDragAndDropWrapper) component).getWrappedComponent();
            } else if (component instanceof FavoritesEntry) {
                if (component == currentlySelectedFavEntry) {
                    continue;
                }
                FavoritesEntry fav = (FavoritesEntry) component;
                fav.reset();
            }
        }
    }

    @Override
    public void addSelectedListener(SelectedListener listener) {
        addListener("onSelected", SelectedEvent.class, listener, SelectedEvent.SELECTED_METHOD);
    }

    @Override
    public void removeSelectedListener(SelectedListener listener) {
        removeListener(SelectedEvent.class, listener, SelectedEvent.SELECTED_METHOD);
    }

    private void setEditable(boolean editable) {
        this.editable = editable;
        String icon = "icon-tick";
        if (editable) {
            titleField.addStyleName("editable");
            titleField.focus();
            titleField.selectAll();
        } else {
            icon = "icon-edit";
            // discard pending changes
            titleField.setValue(title);
            titleField.removeStyleName("editable");
        }
        titleField.setReadOnly(!editable);
        editButton.setCaption("<span class=\"" + icon + "\"></span>");
    }

    private void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            wrapper.addStyleName("selected");
            fireEvent(new SelectedEvent(this));
        } else {
            wrapper.removeStyleName("selected");
        }
        titleField.setReadOnly(true);
        editButton.setVisible(selected);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        removeButton.setVisible(selected);
    }

    private void construct(final AbstractJcrNodeAdapter favoritesGroup, final FavoritesView.Listener listener) {
        wrapper = new CssLayout();
        wrapper.addStyleName("favorites-group-title");

        this.enterKeyShortcutListener = new EnterKeyShortcutListener(listener);
        this.escapeKeyShortcutListener = new EscapeKeyShortcutListener();

        this.relPath = favoritesGroup.getNodeName();
        this.title = favoritesGroup.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString();

        titleField = new TextField();
        titleField.setValue(title);
        titleField.setReadOnly(true);
        titleField.addFocusListener(new FocusListener() {

            @Override
            public void focus(FocusEvent event) {
                titleField.addShortcutListener(enterKeyShortcutListener);
                titleField.addShortcutListener(escapeKeyShortcutListener);
            }
        });

        titleField.addBlurListener(new BlurListener() {

            @Override
            public void blur(BlurEvent event) {
                titleField.removeShortcutListener(enterKeyShortcutListener);
                titleField.removeShortcutListener(escapeKeyShortcutListener);
            }
        });

        wrapper.addComponent(titleField);

        editButton = new NativeButton();
        editButton.setHtmlContentAllowed(true);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        editButton.addStyleName("favorite-action");
        editButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (selected && !editable) {
                    setEditable(true);
                    return;
                }
                doEditTitle(listener);
            }
        });
        editButton.setVisible(false);
        wrapper.addComponent(editButton);

        removeButton = new NativeButton();
        removeButton.setHtmlContentAllowed(true);
        removeButton.setCaption("<span class=\"icon-trash\"></span>");
        removeButton.addStyleName("favorite-action");
        removeButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                shell.openConfirmation(MessageStyleTypeEnum.WARNING, i18n.translate("favorites.group.confirmation.title"), i18n.translate("confirmation.cannot.undo"), i18n.translate("confirmation.delete.yes"), i18n.translate("confirmation.no"), false, new ConfirmationCallback() {

                    @Override
                    public void onSuccess() {
                        listener.removeGroup(relPath);
                    }

                    @Override
                    public void onCancel() {
                        // no op
                    }
                });
            }
        });
        removeButton.setVisible(false);
        wrapper.addComponent(removeButton);

        addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {

                if (event.getClickedComponent() == titleField) {
                    if (!editable) {
                        setSelected(!selected);
                    }
                }
            }
        });

        addComponent(new GroupDragAndDropWrapper(wrapper, listener, this));
    }

    private void doEditTitle(final FavoritesView.Listener listener) {
        if (StringUtils.isBlank(titleField.getValue())) {
            shell.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("favorites.title.required"));
            return;
        }
        boolean titleHasChanged = !title.equals(titleField.getValue());
        if (editable && titleHasChanged) {
            listener.editGroup(relPath, titleField.getValue());
        }
        setEditable(false);
    }

    private class EnterKeyShortcutListener extends ShortcutListener {
        private FavoritesView.Listener listener;

        public EnterKeyShortcutListener(final FavoritesView.Listener listener) {
            super("", KeyCode.ENTER, null);
            this.listener = listener;
        }

        @Override
        public void handleAction(Object sender, Object target) {
            if (editable) {
                doEditTitle(listener);
            } else {
                setEditable(true);
            }
        }
    }

    private class EscapeKeyShortcutListener extends ShortcutListener {

        public EscapeKeyShortcutListener() {
            super("", KeyCode.ESCAPE, null);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            reset();
        }
    }
}
