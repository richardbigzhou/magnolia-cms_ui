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

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * Favorite group.
 */
public class FavoritesGroup extends CssLayout {

    private TextField titleField;
    private NativeButton editButton;
    private NativeButton removeButton;
    private String title;
    private String relPath;
    private boolean editable;
    private boolean selected;
    private CssLayout wrapper;

    /**
     * Creates an empty placeholder group.
     */
    public FavoritesGroup() {
    }

    public FavoritesGroup(final JcrItemNodeAdapter favoritesGroup, final FavoritesView.Listener listener) {
        addStyleName("favorites-group");

        construct(favoritesGroup, listener);

        for (JcrItemNodeAdapter fav : favoritesGroup.getChildren().values()) {
            final FavoritesEntry favEntry = new FavoritesEntry(fav, listener);
            addComponent(favEntry);

        }
    }

    public String getTitleValue() {
        return title;
    }

    public String getRelPath() {
        return relPath;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        titleField.setReadOnly(!editable);
        String icon = "";
        if (editable) {
            icon = "icon-tick";
            titleField.addStyleName("editable");
            titleField.focus();
        } else {
            icon = "icon-edit";
            titleField.removeStyleName("editable");
        }
        editButton.setCaption("<span class=\"" + icon + "\"></span>");
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        titleField.setReadOnly(true);
        editButton.setVisible(selected);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        if (selected) {
            wrapper.addStyleName("selected");
        } else {
            wrapper.removeStyleName("selected");
        }
        removeButton.setVisible(selected);
    }

    /**
     * @return true if this group is selected, meaning the available actions (edit, remove) are displayed next to the group title. Bear in mind that selected does not necessarily mean editable.
     * @see #isEditable()
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return true if this group is editable, meaning that its title can be changed in line.
     * @see #isSelected()
     */
    public boolean isEditable() {
        return editable;
    }

    private void construct(final JcrItemNodeAdapter favoritesGroup, final FavoritesView.Listener listener) {
        wrapper = new CssLayout();
        wrapper.addStyleName("favorites-group-title");

        this.relPath = favoritesGroup.getItemProperty(ModelConstants.JCR_NAME).getValue().toString();
        this.title = favoritesGroup.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString();

        titleField = new TextField();
        titleField.setValue(title);
        titleField.setReadOnly(true);

        wrapper.addComponent(titleField);

        editButton = new NativeButton();
        editButton.setHtmlContentAllowed(true);
        editButton.setCaption("<span class=\"icon-edit\"></span>");
        editButton.addStyleName("favorite-action");
        editButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (isSelected() && !isEditable()) {
                    setEditable(true);
                    return;
                }
                boolean titleHasChanged = !getTitleValue().equals(titleField.getValue());
                if (isEditable() && titleHasChanged) {
                    listener.editGroup(getRelPath(), titleField.getValue());
                }
                setEditable(false);
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
                listener.removeGroup(getRelPath());
            }
        });
        removeButton.setVisible(false);
        wrapper.addComponent(removeButton);

        addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {

                if (event.getClickedComponent() == titleField) {
                    if (!isEditable()) {
                        setSelected(!isSelected());
                    }
                }
            }
        });

        addComponent(wrapper);
    }
}
