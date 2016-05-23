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
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * Favorite group.
 */
public final class FavoritesGroup extends CssLayout implements EditableFavoriteItem, EditingEvent.EditingNotifier {

    private TextField titleField;
    private NativeButton editButton;
    private NativeButton removeButton;
    private String title;
    private String relPath;
    private boolean editable;
    private CssLayout wrapper;
    private EnterKeyShortcutListener enterKeyShortcutListener;
    private EscapeKeyShortcutListener escapeKeyShortcutListener;
    private Shell shell;
    private final SimpleTranslator i18n;
    private List<EditableFavoriteItem> editableFavoriteItems;
    private FavoritesView.Listener listener;
    private String itemId;
    private GroupDragAndDropWrapper dragAndDropWrapper;

    /**
     * Creates an empty placeholder group.
     */
    public FavoritesGroup(SimpleTranslator i18n) {
        this.i18n = i18n;
        addStyleName("no-group");
    }

    public FavoritesGroup(final AbstractJcrNodeAdapter favoritesGroup, final FavoritesView.Listener listener, final Shell shell, final FavoritesView view, final SimpleTranslator i18n, List<EditableFavoriteItem> editableFavoriteItems) {
        this.shell = shell;
        this.i18n = i18n;
        this.editableFavoriteItems = editableFavoriteItems;
        this.listener = listener;
        itemId = FavoritesEntry.createItemId(favoritesGroup);

        addStyleName("favorites-group");
        construct(favoritesGroup, listener);

        final Map<String, AbstractJcrNodeAdapter> nodeAdapters = favoritesGroup.getChildren();

        for (String key : nodeAdapters.keySet()) {
            final AbstractJcrNodeAdapter fav = nodeAdapters.get(key);
            final FavoritesEntry favEntry = new FavoritesEntry(fav, listener, shell, i18n);
            favEntry.setGroup(this.relPath);
            editableFavoriteItems.add(favEntry);
            final EntryDragAndDropWrapper wrapper = new EntryDragAndDropWrapper(favEntry, listener);
            favEntry.addEditingListener(new EditingEvent.EditingListener() {
                @Override
                public void onEdit(EditingEvent event) {
                    if (event.isEditing()) {
                        wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.NONE);
                    } else {
                        wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
                    }
                }
            });
            addComponent(wrapper);
        }
    }

    public String getRelPath() {
        return this.relPath;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void setToNonEditableState() {
        setEditable(false);
    }

    private void setEditable(boolean editable) {
        this.editable = editable;
        String icon = "icon-tick";
        if (editable) {
            listener.setCurrentEditedItemId(getItemId());
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
        fireEvent(new EditingEvent(this, editable));
    }

    @Override
    public void setIconsVisibility(boolean visible) {
        editButton.setVisible(visible);
        removeButton.setVisible(visible);
    }

    @Override
    public boolean iconsAreVisible() {
        return editButton.isVisible();
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
        dragAndDropWrapper = new GroupDragAndDropWrapper(wrapper, listener, this);
        addComponent(dragAndDropWrapper);
    }

    private void doEditTitle(final FavoritesView.Listener listener) {
        if (StringUtils.isBlank(titleField.getValue())) {
            shell.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("favorites.title.required"));
            titleField.focus();
            return;
        }
        if (editable) {
            boolean titleHasChanged = !title.equals(titleField.getValue());
            if (titleHasChanged) {
                listener.editGroup(relPath, titleField.getValue());
            }
            setEditable(false);
        } else {
            setEditable(true);
        }

    }

    @Override
    public void addEditingListener(EditingEvent.EditingListener listener) {
        addListener("onEdit", EditingEvent.class, listener, EditingEvent.EDITING_METHOD);
    }

    @Override
    public void removeEditingListener(EditingEvent.EditingListener listener) {
        removeListener(EditingEvent.class, listener, EditingEvent.EDITING_METHOD);
    }

    protected GroupDragAndDropWrapper getDragAndDropWrapper() {
        return dragAndDropWrapper;
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
                setIconsVisibility(true);
            }
        }
    }

    private class EscapeKeyShortcutListener extends ShortcutListener {

        public EscapeKeyShortcutListener() {
            super("", KeyCode.ESCAPE, null);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            setToNonEditableState();
        }
    }
}
