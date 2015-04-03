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
package info.magnolia.ui.admincentral.shellapp.favorites;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * FavoritesEntry.
 */
public final class FavoritesEntry extends CustomComponent implements EditableFavoriteItem, EditingEvent.EditingNotifier {

    private static final Logger log = LoggerFactory.getLogger(FavoritesEntry.class);
    private static long uniqueIdCounter = 23;

    private HorizontalLayout root = new HorizontalLayout();
    private String location;
    private String title;
    private String group = null;
    private String nodename;
    private TextField titleField;
    private NativeButton editButton;
    private NativeButton removeButton;
    private boolean editable;
    private EnterKeyShortcutListener enterKeyShortcutListener;
    private EscapeKeyShortcutListener escapeKeyShortcutListener;
    private Shell shell;
    private final SimpleTranslator i18n;
    private String itemId;
    private FavoritesView.Listener listener;

    public FavoritesEntry(final AbstractJcrNodeAdapter favorite, final FavoritesView.Listener listener, final Shell shell, SimpleTranslator i18n) {
        super();
        this.listener = listener;
        this.shell = shell;
        this.i18n = i18n;
        itemId = createItemdId(favorite);
        construct(favorite, listener);
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    public String getRelPath() {
        return StringUtils.isBlank(group) ? this.nodename : this.group + "/" + this.nodename;
    }

    public String getNodename() {
        return this.nodename;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
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
            titleField.removeStyleName("editable");
            // pending changes are reverted
            titleField.setValue(title);
        }
        titleField.setReadOnly(!editable);
        editButton.setCaption("<span class=\"" + icon + "\"></span>");
        fireEvent(new EditingEvent(this, editable));
    }


    private void construct(final AbstractJcrNodeAdapter favorite, final FavoritesView.Listener listener) {
        addStyleName("favorites-entry");
        setSizeUndefined();
        root.setSizeUndefined();

        this.enterKeyShortcutListener = new EnterKeyShortcutListener(listener);
        this.escapeKeyShortcutListener = new EscapeKeyShortcutListener();

        this.nodename = favorite.getNodeName();
        this.location = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue().toString();
        this.title = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString();

        String icon = "icon-app";
        if (favorite.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue() != null) {
            icon = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue().toString();
        }

        final Label iconLabel = new Label();
        iconLabel.setValue("<span class=\"" + icon + "\"></span>");
        iconLabel.setStyleName("icon");
        iconLabel.setContentMode(ContentMode.HTML);
        root.addComponent(iconLabel);

        titleField = new TextField();
        titleField.setValue(title);
        titleField.setReadOnly(true);

        titleField.addFocusListener(new FocusListener() {

            @Override
            public void focus(FocusEvent event) {
                iconLabel.removeShortcutListener(enterKeyShortcutListener);
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

        root.addComponent(titleField);

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
        root.addComponent(editButton);

        removeButton = new NativeButton();
        removeButton.setHtmlContentAllowed(true);
        removeButton.setCaption("<span class=\"icon-trash\"></span>");
        removeButton.addStyleName("favorite-action");
        removeButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                shell.openConfirmation(MessageStyleTypeEnum.WARNING, i18n.translate("confirmation.delete.title.generic"), i18n.translate("confirmation.cannot.undo"), i18n.translate("confirmation.delete.yes"), i18n.translate("confirmation.no"), false, new ConfirmationCallback() {

                    @Override
                    public void onSuccess() {
                        listener.removeFavorite(getRelPath());
                    }

                    @Override
                    public void onCancel() {
                        // no op
                    }
                });
            }
        });
        removeButton.setVisible(false);
        root.addComponent(removeButton);

        root.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getClickedComponent() == titleField && !editable) {
                    if (event.isDoubleClick()) {
                        // TODO fgrilli commented out as, besides making the text editable, it also goes to the saved location
                        // See MGNLUI-1317
                    } else {
                        listener.goToLocation(location);
                    }
                }
            }
        });

        setCompositionRoot(root);
        setIconsVisibility(false);
    }

    @Override
    public void addEditingListener(EditingEvent.EditingListener listener) {
        addListener("onEdit", EditingEvent.class, listener, EditingEvent.EDITING_METHOD);
    }

    @Override
    public void removeEditingListener(EditingEvent.EditingListener listener) {
        removeListener(EditingEvent.class, listener, EditingEvent.EDITING_METHOD);
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

    private void doEditTitle(final FavoritesView.Listener listener) {
        if (StringUtils.isBlank(titleField.getValue())) {
            shell.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("favorites.title.required"));
            titleField.focus();
            return;
        }

        if (editable) {
            boolean titleHasChanged = !title.equals(titleField.getValue());
            if (titleHasChanged) {
                listener.editFavorite(getRelPath(), titleField.getValue());
            }
            setEditable(false);
        } else {
            setEditable(true);
        }
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

    /**
     * Creates a unique ID to use for {@code EditableFavoriteItem}s.
     */
    public static String createItemdId(AbstractJcrNodeAdapter nodeAdapter) {
        String id = null;
        try {
            id = nodeAdapter.getJcrItem().getIdentifier();
        } catch (Exception ex) {
            log.error("Failed to create an itemId from an AbstractJcrNodeAdapter", ex);
        } finally {
            if (id == null) {
                uniqueIdCounter++;
                id = String.valueOf((new Date()).getTime() + uniqueIdCounter);
            }
        }
        return id;
    }

}
