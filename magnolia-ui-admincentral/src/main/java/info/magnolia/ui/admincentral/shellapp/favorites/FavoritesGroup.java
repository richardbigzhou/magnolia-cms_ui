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

import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * Favorite group.
 */
public final class FavoritesGroup extends CssLayout {

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

    /**
     * Creates an empty placeholder group.
     */
    public FavoritesGroup() {
        addStyleName("no-group");
    }

    public FavoritesGroup(final JcrItemNodeAdapter favoritesGroup, final FavoritesView.Listener listener) {
        addStyleName("favorites-group");

        construct(favoritesGroup, listener);

        final Map<String, JcrItemNodeAdapter> nodeAdapters = favoritesGroup.getChildren();
        final SortedSet<String> keys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        keys.addAll(nodeAdapters.keySet());

        for (String key : keys) {
            final JcrItemNodeAdapter fav = nodeAdapters.get(key);
            final FavoritesEntry favEntry = new FavoritesEntry(fav, listener);
            addComponent(favEntry);
        }
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
        Iterator<Component> components = getComponentIterator();
        while (components.hasNext()) {
            Component component = components.next();
            if(component instanceof FavoritesEntry) {
                FavoritesEntry fav = (FavoritesEntry) component;
                fav.reset();
            }
        }
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
        } else {
            wrapper.removeStyleName("selected");
        }
        titleField.setReadOnly(true);
        editButton.setVisible(selected);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        removeButton.setVisible(selected);
    }

    private void construct(final JcrItemNodeAdapter favoritesGroup, final FavoritesView.Listener listener) {
        wrapper = new CssLayout();
        wrapper.addStyleName("favorites-group-title");

        this.enterKeyShortcutListener = new EnterKeyShortcutListener(listener);
        this.escapeKeyShortcutListener = new EscapeKeyShortcutListener();

        this.relPath = favoritesGroup.getItemProperty(ModelConstants.JCR_NAME).getValue().toString();
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
                listener.removeGroup(relPath);
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

        addComponent(wrapper);
    }

    private void doEditTitle(final FavoritesView.Listener listener) {
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
            doEditTitle(listener);
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
