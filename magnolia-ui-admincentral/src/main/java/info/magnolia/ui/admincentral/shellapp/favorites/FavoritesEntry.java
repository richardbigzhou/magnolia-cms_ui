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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
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
public final class FavoritesEntry extends CustomComponent {

    private HorizontalLayout root = new HorizontalLayout();
    private String location;
    private String title;
    private String group;
    private String relPath;
    private TextField titleField;
    private NativeButton editButton;
    private NativeButton removeButton;
    private boolean editable;
    private boolean selected;
    private EnterKeyShortcutListener enterKeyShortcutListener;
    private EscapeKeyShortcutListener escapeKeyShortcutListener;
    private Shell shell;


    public FavoritesEntry(final AbstractJcrNodeAdapter favorite, final FavoritesView.Listener listener, final Shell shell) {
        super();
        this.shell = shell;
        construct(favorite, listener);
    }

    /**
     * Sets this fav as unselected and non editable, that is at its initial state.
     */
    public void reset() {
        setEditable(false);
        setSelected(false);
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
            titleField.removeStyleName("editable");
            // pending changes are reverted
            titleField.setValue(title);
        }
        titleField.setReadOnly(!editable);
        editButton.setCaption("<span class=\"" + icon + "\"></span>");
    }

    private void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            addStyleName("selected");
        } else {
            removeStyleName("selected");
        }
        titleField.setReadOnly(true);
        editButton.setVisible(selected);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        removeButton.setVisible(selected);
    }

    private void construct(final AbstractJcrNodeAdapter favorite, final FavoritesView.Listener listener) {
        addStyleName("favorites-entry");
        setSizeUndefined();
        root.setSizeUndefined();

        this.enterKeyShortcutListener = new EnterKeyShortcutListener(listener);
        this.escapeKeyShortcutListener = new EscapeKeyShortcutListener();

        final String nodeName = favorite.getNodeName();
        this.location = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue().toString();
        this.title = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString();
        this.relPath = nodeName;
        try {
            this.group = MgnlContext.doInSystemContext(new MgnlContext.Op<String, Throwable>() {

                @Override
                public String exec() throws Throwable {
                    final Property<?> property = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP);
                    if (property != null) {
                        return property.getValue().toString();
                    }
                    return null;
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isNotBlank(this.group)) {
            this.relPath = this.group + "/" + nodeName;
        }


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
                if (selected && !editable) {
                    setEditable(true);
                    return;
                }
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
                shell.openConfirmation(MessageStyleTypeEnum.WARNING, MessagesUtil.get("confirmation.delete.title.generic"), MessagesUtil.get("confirmation.cannot.undo"), MessagesUtil.get("confirmation.delete.yes"), MessagesUtil.get("confirmation.no"), true, new ConfirmationCallback() {

                    @Override
                    public void onSuccess() {
                        listener.removeFavorite(relPath);
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
                        // setEditable(true);
                    } else {
                        listener.goToLocation(location);
                    }
                } else if (event.getClickedComponent() == iconLabel) {
                    setSelected(!selected);
                    setEditable(false);
                    if (selected) {
                        iconLabel.addShortcutListener(enterKeyShortcutListener);
                    }
                }
            }
        });

        setCompositionRoot(root);
    }

    private void doEditTitle(final FavoritesView.Listener listener) {
        if (StringUtils.isBlank(titleField.getValue())) {
            shell.openNotification(MessageStyleTypeEnum.ERROR, true, MessagesUtil.get("favorites.title.required"));
            return;
        }

        boolean titleHasChanged = !title.equals(titleField.getValue());
        if (editable && titleHasChanged) {
            listener.editFavorite(relPath, titleField.getValue());
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
