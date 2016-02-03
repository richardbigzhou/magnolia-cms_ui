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
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */
public final class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;
    private FavoritesGroup noGroup;
    private FavoritesForm favoriteForm;
    private Shell shell;
    private SplitFeed splitPanel = new SplitFeed();
    private Label emptyPlaceHolder = new Label();

    @Override
    public String getId() {
        return "favorite";
    }

    @Inject
    public FavoritesViewImpl(Shell shell) {
        super();
        this.shell = shell;
        construct();
    }

    @Override
    public void setListener(FavoritesView.Listener listener) {
        this.listener = listener;
    }

    private void construct() {
        layout.addStyleName("favorites");
        layout.setHeight("100%");
        layout.setWidth("900px");

        emptyPlaceHolder.addStyleName("emptyplaceholder");
        emptyPlaceHolder.setContentMode(ContentMode.HTML);
        emptyPlaceHolder.setValue(String.format("<span class=\"icon-favorites\"></span><div class=\"message\">%s</div>", MessagesUtil.get("favorites.empty")));

        splitPanel.setVisible(false);

        noGroup = new FavoritesGroup();

        layout.addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                Component clickedComponent = event.getClickedComponent();
                reset(clickedComponent);
            }
        });

        layout.addComponent(emptyPlaceHolder);
        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 0);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setFavoriteLocation(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup, Map<String, String> availableGroupsNames) {
        layout.removeComponent(favoriteForm);
        favoriteForm = new FavoritesForm(newFavorite, newGroup, availableGroupsNames, listener, shell);
        layout.addComponent(favoriteForm);
    }

    @Override
    public void init(AbstractJcrNodeAdapter favorites, JcrNewNodeAdapter favoriteSuggestion, JcrNewNodeAdapter groupSuggestion, Map<String, String> availableGroups) {

        final Map<String, AbstractJcrNodeAdapter> nodeAdapters = favorites.getChildren();

        if (nodeAdapters.isEmpty()) {
            emptyPlaceHolder.setVisible(true);
            splitPanel.setVisible(false);
            layout.setExpandRatio(splitPanel, 0);
            layout.setExpandRatio(emptyPlaceHolder, 1);
        } else {
            emptyPlaceHolder.setVisible(false);
            splitPanel.setVisible(true);
            layout.setExpandRatio(splitPanel, 1);
            layout.setExpandRatio(emptyPlaceHolder, 0);

            noGroup.removeAllComponents();
            splitPanel.getLeftContainer().removeAllComponents();
            splitPanel.getRightContainer().removeAllComponents();
            final SortedSet<String> keys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            keys.addAll(nodeAdapters.keySet());

            for (String key : keys) {
                final AbstractJcrNodeAdapter favoriteAdapter = nodeAdapters.get(key);
                if (AdmincentralNodeTypes.Favorite.NAME.equals(favoriteAdapter.getPrimaryNodeTypeName())) {
                    final FavoritesEntry favEntry = new FavoritesEntry(favoriteAdapter, listener, shell);
                    noGroup.addComponent(favEntry);
                } else {
                    FavoritesGroup group = new FavoritesGroup(favoriteAdapter, listener, shell);
                    splitPanel.getRightContainer().addComponent(group);
                }
            }
            splitPanel.getLeftContainer().addComponent(noGroup);
        }

        if (favoriteForm != null) {
            layout.removeComponent(favoriteForm);
        }
        favoriteForm = new FavoritesForm(favoriteSuggestion, groupSuggestion, availableGroups, listener, shell);
        layout.addComponent(favoriteForm);
    }

    /**
     * Clicking outside a group or favorite resets everything.
     */
    private void reset(Component clickedComponent) {
        if (!(clickedComponent instanceof SplitFeed.FeedSection)) {
            return;
        }
        favoriteForm.close();
        noGroup.reset();
        Iterator<Component> components = splitPanel.getRightContainer().getComponentIterator();

        while (components.hasNext()) {
            Component component = components.next();
            if (component instanceof FavoritesGroup) {
                ((FavoritesGroup) component).reset();
            }
        }
    }
}
